/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deange.githubstatus.gcm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import com.deange.githubstatus.BuildConfig;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utilities for device registration.
 * <p>
 * <strong>Note:</strong> this class uses a private {@link android.content.SharedPreferences}
 * object to keep track of the registration token.
 */
public final class GCMRegistrar {

    /**
     * Default lifespan (7 days) of the {@link #isRegisteredOnServer(android.content.Context)}
     * flag until it is considered expired.
     */
    // NOTE: cannot use TimeUnit.DAYS because it's not available on API Level 8
    public static final long DEFAULT_ON_SERVER_LIFESPAN_MS =
            1000 * 3600 * 24 * 7;

    private static final int DEFAULT_BACKOFF_MS = 3000;
    private static final String TAG = "GCMRegistrar";
    private static final String BACKOFF_MS = "backoff_ms";
    private static final String GSF_PACKAGE = "com.google.android.gsf";
    private static final String PREFERENCES = "com.google.android.gcm";
    private static final String PROPERTY_REG_ID = "regId";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_ON_SERVER = "onServer";
    private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTime";
    private static final String PROPERTY_ON_SERVER_LIFESPAN = "onServerLifeSpan";

    /**
     * {@link GCMBroadcastReceiver} instance used to handle the retry intent.
     *
     * <p>
     * This instance cannot be the same as the one defined in the manifest
     * because it needs a different permission.
     */
    private static GCMBroadcastReceiver sRetryReceiver;

    public static void checkDevice(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version < Build.VERSION_CODES.FROYO) {
            throw new UnsupportedOperationException("Device must be at least " + "API Level 8 (instead of " + version + ")");
        }

        try {
            context.getPackageManager().getPackageInfo(GSF_PACKAGE, 0);

        } catch (final NameNotFoundException e) {
            throw new UnsupportedOperationException("Device does not have package " + GSF_PACKAGE);
        }
    }

    public static void checkManifest(Context context) {

        final PackageManager packageManager = context.getPackageManager();
        final String packageName = context.getPackageName();
        final String permissionName = packageName + ".permission.C2D_MESSAGE";

        // check permission
        try {
            packageManager.getPermissionInfo(permissionName, PackageManager.GET_PERMISSIONS);

        } catch (NameNotFoundException e) {
            throw new IllegalStateException("Application does not define permission " + permissionName);
        }

        // check receivers
        PackageInfo receiversInfo;
        try {
            receiversInfo = packageManager.getPackageInfo( packageName, PackageManager.GET_RECEIVERS);

        } catch (NameNotFoundException e) {
            throw new IllegalStateException("Could not get receivers for package " + packageName);
        }

        final ActivityInfo[] receivers = receiversInfo.receivers;
        if (receivers == null || receivers.length == 0) {
            throw new IllegalStateException("No receiver for package " + packageName);
        }

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "number of receivers for " + packageName + ": " + receivers.length);
        }

        final Set<String> allowedReceivers = new HashSet<>();
        for (final ActivityInfo receiver : receivers) {
            if (GCMConstants.PERMISSION_GCM_INTENTS.equals(
                    receiver.permission)) {
                allowedReceivers.add(receiver.name);
            }
        }

        if (allowedReceivers.isEmpty()) {
            throw new IllegalStateException("No receiver allowed to receive " + GCMConstants.PERMISSION_GCM_INTENTS);
        }
        checkReceiver(context, allowedReceivers, GCMConstants.INTENT_FROM_GCM_REGISTRATION_CALLBACK);
        checkReceiver(context, allowedReceivers, GCMConstants.INTENT_FROM_GCM_MESSAGE);
    }

    private static void checkReceiver(final Context context, final Set<String> allowedReceivers,
                                      final String action) {

        final Intent intent = new Intent(action);
        intent.setPackage(context.getPackageName());

        final List<ResolveInfo> receivers = context.getPackageManager()
                .queryBroadcastReceivers(intent, PackageManager.GET_INTENT_FILTERS);
        if (receivers.isEmpty()) {
            throw new IllegalStateException("No receivers for action " + action);
        }

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Found " + receivers.size() + " receivers for action " + action);
        }

        // make sure receivers match
        for (final ResolveInfo receiver : receivers) {
            final String name = receiver.activityInfo.name;
            if (!allowedReceivers.contains(name)) {
                throw new IllegalStateException("Receiver " + name + " is not set with permission " +
                        GCMConstants.PERMISSION_GCM_INTENTS);
            }
        }

    }

    public static void register(final Context context, final String... senderIds) {
        GCMRegistrar.resetBackoff(context);
        internalRegister(context, senderIds);
    }

    static void internalRegister(final Context context, final String... senderIds) {
        final String flatSenderIds = getFlatSenderIds(senderIds);
        Log.v(TAG, "Registering app "  + context.getPackageName() + " of senders " + flatSenderIds);

        Intent intent = new Intent(GCMConstants.INTENT_TO_GCM_REGISTRATION);
        intent.setPackage(GSF_PACKAGE);
        intent.putExtra(GCMConstants.EXTRA_APPLICATION_PENDING_INTENT,
                PendingIntent.getBroadcast(context, 0, new Intent(), 0));
        intent.putExtra(GCMConstants.EXTRA_SENDER, flatSenderIds);

        context.startService(intent);
    }

    static String getFlatSenderIds(final String... senderIds) {

        if (senderIds == null || senderIds.length == 0) {
            throw new IllegalArgumentException("No senderIds");
        }

        final StringBuilder builder = new StringBuilder(senderIds[0]);
        for (int i = 1; i < senderIds.length; i++) {
            builder.append(',').append(senderIds[i]);
        }

        return builder.toString();
    }

    public static void unregister(Context context) {
        GCMRegistrar.resetBackoff(context);
        internalUnregister(context);
    }

    public static synchronized void onDestroy(final Context context) {
        if (sRetryReceiver != null) {
            Log.v(TAG, "Unregistering receiver");
            context.unregisterReceiver(sRetryReceiver);
            sRetryReceiver = null;
        }
    }

    static void internalUnregister(final Context context) {
        Log.v(TAG, "Unregistering app " + context.getPackageName());
        final Intent intent = new Intent(GCMConstants.INTENT_TO_GCM_UNREGISTRATION);
        intent.setPackage(GSF_PACKAGE);
        intent.putExtra(GCMConstants.EXTRA_APPLICATION_PENDING_INTENT,
                PendingIntent.getBroadcast(context, 0, new Intent(), 0));
        context.startService(intent);
    }

    static synchronized void setRetryBroadcastReceiver(final Context context) {
        if (sRetryReceiver == null) {
            sRetryReceiver = new GCMBroadcastReceiver();

            final String category = context.getPackageName();
            final IntentFilter filter = new IntentFilter(GCMConstants.INTENT_FROM_GCM_LIBRARY_RETRY);
            filter.addCategory(category);

            // must use a permission that is defined on manifest for sure
            final String permission = category + ".permission.C2D_MESSAGE";
            Log.v(TAG, "Registering receiver");
            context.registerReceiver(sRetryReceiver, filter, permission, null);
        }
    }

    public static String getRegistrationId(final Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        // check if app was updated; if so, it must clear registration id to
        // avoid a race condition if GCM sends a message
        final int oldVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        final int newVersion = BuildConfig.VERSION_CODE;
        if (oldVersion != Integer.MIN_VALUE && oldVersion != newVersion) {
            Log.v(TAG, "App version changed from " + oldVersion + " to " + newVersion
                    + "; resetting registration id");
            clearRegistrationId(context);
            registrationId = "";
        }

        return registrationId;
    }

    public static boolean isRegistered(final Context context) {
        return getRegistrationId(context).length() > 0;
    }

    static String clearRegistrationId(final Context context) {
        return setRegistrationId(context, "");
    }

    static String setRegistrationId(final Context context, final String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        final String oldRegistrationId = prefs.getString(PROPERTY_REG_ID, "");
        final int appVersion = BuildConfig.VERSION_CODE;

        Log.v(TAG, "Saving regId on app version " + appVersion);

        final Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();

        return oldRegistrationId;
    }

    public static void setRegisteredOnServer(final Context context, final boolean flag) {
        final SharedPreferences prefs = getGCMPreferences(context);
        final long lifespan = getRegisterOnServerLifespan(context);
        final long expirationTime = System.currentTimeMillis() + lifespan;
        Log.v(TAG, "Setting registeredOnServer status as " + flag + " until "
                + new Timestamp(expirationTime));

        final Editor editor = prefs.edit();
        editor.putBoolean(PROPERTY_ON_SERVER, flag);
        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
        editor.apply();
    }

    public static boolean isRegisteredOnServer(final Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        final boolean isRegistered = prefs.getBoolean(PROPERTY_ON_SERVER, false);
        Log.v(TAG, "Is registered on server: " + isRegistered);

        if (isRegistered) {
            // checks if the information is not stale
            final long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
            if (System.currentTimeMillis() > expirationTime) {
                Log.v(TAG, "flag expired on: " + new Timestamp(expirationTime));
                return false;
            }
        }

        return isRegistered;
    }

    public static long getRegisterOnServerLifespan(final Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        return prefs.getLong(PROPERTY_ON_SERVER_LIFESPAN, DEFAULT_ON_SERVER_LIFESPAN_MS);
    }

    public static void setRegisterOnServerLifespan(final Context context, final long lifespan) {
        final Editor editor = getGCMPreferences(context).edit();
        editor.putLong(PROPERTY_ON_SERVER_LIFESPAN, lifespan);
        editor.apply();
    }

    static void resetBackoff(final Context context) {
        Log.d(TAG, "resetting backoff for " + context.getPackageName());
        setBackoff(context, DEFAULT_BACKOFF_MS);
    }

    static int getBackoff(final Context context) {
        return getGCMPreferences(context).getInt(BACKOFF_MS, DEFAULT_BACKOFF_MS);
    }

    static void setBackoff(final Context context, final int backoff) {
        final Editor editor = getGCMPreferences(context).edit();
        editor.putInt(BACKOFF_MS, backoff);
        editor.commit();
    }

    private static SharedPreferences getGCMPreferences(final Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    private GCMRegistrar() {
        throw new UnsupportedOperationException();
    }
}
