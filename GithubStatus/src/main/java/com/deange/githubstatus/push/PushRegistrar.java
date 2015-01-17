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

package com.deange.githubstatus.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.deange.githubstatus.BuildConfig;
import com.deange.githubstatus.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Utilities for device registration.
 * <p>
 * <strong>Note:</strong> this class uses a private {@link SharedPreferences}
 * object to keep track of the registration token.
 */
public final class PushRegistrar {

    private static final String TAG = PushRegistrar.class.getSimpleName();

    private static final int DEFAULT_MAX_ATTEMPTS = 10;
    private static final String PREFERENCES = Utils.buildPreferences("registrar");
    private static final String PROPERTY_REG_ID = "regId";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private static PushRegisterTask sRegisterTask;
    private static PushUnregisterTask sUnregisterTask;

    public static void register(final Context context) {
        if (!isRegistered(context)) {
            Log.v(TAG, "Registering app " + context.getPackageName());

            if (sRegisterTask != null) {
                sRegisterTask.cancel();
            }

            sRegisterTask = new PushRegisterTask(context, DEFAULT_MAX_ATTEMPTS);
            sRegisterTask.start();
        }
    }

    public static void unregister(final Context context) {
        if (isRegistered(context)) {
            Log.v(TAG, "Unregistering app " + context.getPackageName());

            if (sUnregisterTask != null) {
                sUnregisterTask.cancel();
            }

            sUnregisterTask = new PushUnregisterTask(context, DEFAULT_MAX_ATTEMPTS);
            sUnregisterTask.start();
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
        return !TextUtils.isEmpty(getRegistrationId(context));
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

    private static SharedPreferences getGCMPreferences(final Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    private PushRegistrar() {
        throw new UnsupportedOperationException();
    }

    public static class PushRegisterTask extends BackoffHandler {

        private final Context mContext;
        private final GoogleCloudMessaging mGcm;
        private String mRegistrationId = null;

        public PushRegisterTask(final Context context, final int maxTries) {
            super(maxTries, true);
            mContext = context.getApplicationContext();
            mGcm = GoogleCloudMessaging.getInstance(context);
        }

        @Override
        public boolean performAction() throws Throwable {
            mRegistrationId = mGcm.register(BuildConfig.SENDER_ID);
            PushServerRegistrar.register(mContext, mRegistrationId);
            return !TextUtils.isEmpty(mRegistrationId);
        }

        @Override
        public void onActionCompleted(final boolean success) {
            if (success) {
                setRegistrationId(mContext, mRegistrationId);
            }
        }
    }

    public static class PushUnregisterTask extends BackoffHandler {

        private final Context mContext;
        private final GoogleCloudMessaging mGcm;

        public PushUnregisterTask(final Context context, final int maxTries) {
            super(maxTries, true);
            mContext = context.getApplicationContext();
            mGcm = GoogleCloudMessaging.getInstance(context);
        }

        @Override
        public boolean performAction() throws Throwable {
            mGcm.unregister();
            PushServerRegistrar.unregister(mContext, getRegistrationId(mContext));
            return true;
        }

        @Override
        public void onActionCompleted(final boolean success) {
            if (success) {
                clearRegistrationId(mContext);
            }
        }
    }
}
