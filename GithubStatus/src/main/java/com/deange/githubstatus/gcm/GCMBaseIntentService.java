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

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.deange.githubstatus.MainApplication;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Skeleton for application-specific {@link android.app.IntentService}s responsible for
 * handling communication from Google Cloud Messaging service.
 * <p>
 * The abstract methods in this class are called from its worker thread, and
 * hence should run in a limited amount of time. If they execute long
 * operations, they should spawn new threads, otherwise the worker thread will
 * be blocked.
 * <p>
 * Subclasses must provide a public no-arg constructor.
 */
public abstract class GCMBaseIntentService extends IntentService {

    public static final String TAG = GCMBaseIntentService.class.getSimpleName();

    private static final String WAKELOCK_KEY = MainApplication.buildAction("GCM_LIB");
    private static PowerManager.WakeLock sWakeLock;
    private static final Object sLock = GCMBaseIntentService.class;

    private final String[] mSenderIds;

    // Instance counter
    private static int sCounter = 0;

    private static final int MAX_BACKOFF_MS = (int) TimeUnit.SECONDS.toMillis(3600); // 1 hour

    // token used to check intent origin
    private static final Random sRandom = new Random();
    private static final String TOKEN = Long.toBinaryString(sRandom.nextLong());
    private static final String EXTRA_TOKEN = "token";

    /**
     * Constructor that does not set a sender id, useful when the sender id
     * is context-specific.
     * <p>
     * When using this constructor, the subclass <strong>must</strong>
     * override {@link #getSenderIds(android.content.Context)}, otherwise methods such as
     * {@link #onHandleIntent(android.content.Intent)} will throw an
     * {@link IllegalStateException} on runtime.
     */
    protected GCMBaseIntentService() {
        this(getName("DynamicSenderIds"), null);
    }

    /**
     * Constructor used when the sender id(s) is fixed.
     */
    protected GCMBaseIntentService(String... senderIds) {
        this(getName(senderIds), senderIds);
    }

    private GCMBaseIntentService(String name, String[] senderIds) {
        super(name);  // name is used as base name for threads, etc.
        mSenderIds = senderIds;
    }

    private static String getName(String senderId) {
        String name = "GCMIntentService-" + senderId + "-" + (++sCounter);
        Log.v(TAG, "Intent service name: " + name);
        return name;
    }

    private static String getName(String[] senderIds) {
        String flatSenderIds = GCMRegistrar.getFlatSenderIds(senderIds);
        return getName(flatSenderIds);
    }

    /**
     * Gets the sender ids.
     *
     * <p>By default, it returns the sender ids passed in the constructor, but
     * it could be overridden to provide a dynamic sender id.
     *
     * @throws IllegalStateException if sender id was not set on constructor.
     */
    protected String[] getSenderIds(Context context) {
        if (mSenderIds == null) {
            throw new IllegalStateException("sender id not set on constructor");
        }
        return mSenderIds;
    }

    /**
     * Called when a cloud message has been received.
     *
     * @param context application's context.
     * @param intent intent containing the message payload as extras.
     */
    protected abstract void onMessage(Context context, Intent intent);

    /**
     * Called when the GCM server tells pending messages have been deleted
     * because the device was idle.
     *
     * @param context application's context.
     * @param total total number of collapsed messages
     */
    protected void onDeletedMessages(Context context, int total) {
    }

    /**
     * Called on a registration error that could be retried.
     *
     * <p>By default, it does nothing and returns {@literal true}, but could be
     * overridden to change that behavior and/or display the error.
     *
     * @param context application's context.
     * @param errorId error id returned by the GCM service.
     *
     * @return if {@literal true}, failed operation will be retried (using
     *         exponential backoff).
     */
    protected boolean onRecoverableError(Context context, String errorId) {
        return true;
    }

    /**
     * Called on registration or unregistration error.
     *
     * @param context application's context.
     * @param errorId error id returned by the GCM service.
     */
    protected abstract void onError(Context context, String errorId);

    /**
     * Called after a device has been registered.
     *
     * @param context application's context.
     * @param registrationId the registration id returned by the GCM service.
     */
    protected abstract void onRegistered(Context context, String registrationId);

    /**
     * Called after a device has been unregistered.
     *
     * @param registrationId the registration id that was previously registered.
     * @param context application's context.
     */
    protected abstract void onUnregistered(Context context, String registrationId);

    @Override
    public final void onHandleIntent(Intent intent) {
        try {
            Context context = getApplicationContext();
            String action = intent.getAction();

            if (action == null) {
                // Nothing to do here

            } else if (action.equals(GCMConstants.INTENT_FROM_GCM_REGISTRATION_CALLBACK)) {
                GCMRegistrar.setRetryBroadcastReceiver(context);
                handleRegistration(context, intent);

            } else if (action.equals(GCMConstants.INTENT_FROM_GCM_MESSAGE)) {
                // checks for special messages
                String messageType = intent.getStringExtra(GCMConstants.EXTRA_SPECIAL_MESSAGE);
                if (messageType != null) {
                    if (messageType.equals(GCMConstants.VALUE_DELETED_MESSAGES)) {
                        String totalStr = intent.getStringExtra(GCMConstants.EXTRA_TOTAL_DELETED);
                        if (totalStr != null) {
                            try {
                                int total = Integer.parseInt(totalStr);
                                Log.v(TAG, "Received deleted messages " + "notification: " + total);
                                onDeletedMessages(context, total);

                            } catch (NumberFormatException e) {
                                Log.e(TAG, "GCM returned invalid number of " + "deleted messages: " + totalStr);
                            }
                        }

                    } else {
                        // application is not using the latest GCM library
                        Log.e(TAG, "Received unknown special message: " + messageType);
                    }

                } else {
                    onMessage(context, intent);
                }

            } else if (action.equals(GCMConstants.INTENT_FROM_GCM_LIBRARY_RETRY)) {
                String token = intent.getStringExtra(EXTRA_TOKEN);
                if (!TOKEN.equals(token)) {
                    // make sure intent was generated by this class, not by a
                    // malicious app.
                    Log.e(TAG, "Received invalid token: " + token);
                    return;
                }

                // retry last call
                if (GCMRegistrar.isRegistered(context)) {
                    GCMRegistrar.internalUnregister(context);

                } else {
                    String[] senderIds = getSenderIds(context);
                    GCMRegistrar.internalRegister(context, senderIds);
                }
            }

        } finally {
            // Release the power lock, so phone can get back to sleep.
            // The lock is reference-counted by default, so multiple
            // messages are ok.

            // If onMessage() needs to spawn a thread or do something else,
            // it should use its own lock.
            synchronized (sLock) {
                // sanity check for null as this is a public method
                if (sWakeLock != null) {
                    Log.v(TAG, "Releasing wakelock");
                    sWakeLock.release();

                } else {
                    // should never happen during normal workflow
                    Log.e(TAG, "Wakelock reference is null");
                }
            }

        }
    }

    /**
     * Called from the broadcast receiver.
     * <p>
     * Will process the received intent, call handleMessage(), registered(),
     * etc. in background threads, with a wake lock, while keeping the service
     * alive.
     */
    static void runIntentInService(Context context, Intent intent, String className) {

        synchronized (sLock) {
            if (sWakeLock == null) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                sWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_KEY);
            }
        }

        Log.v(TAG, "Acquiring wakelock");
        sWakeLock.acquire();
        intent.setClassName(context, className);
        context.startService(intent);
    }

    private void handleRegistration(final Context context, Intent intent) {
        String registrationId = intent.getStringExtra(GCMConstants.EXTRA_REGISTRATION_ID);
        String error = intent.getStringExtra(GCMConstants.EXTRA_ERROR);
        String unregistered = intent.getStringExtra(GCMConstants.EXTRA_UNREGISTERED);
        Log.d(TAG, "handleRegistration: registrationId = " + registrationId +
                ", error = " + error + ", unregistered = " + unregistered);

        // registration succeeded
        if (registrationId != null) {
            GCMRegistrar.resetBackoff(context);
            GCMRegistrar.setRegistrationId(context, registrationId);
            onRegistered(context, registrationId);
            return;
        }

        // unregistration succeeded
        if (unregistered != null) {
            // Remember we are unregistered
            GCMRegistrar.resetBackoff(context);
            String oldRegistrationId = GCMRegistrar.clearRegistrationId(context);
            onUnregistered(context, oldRegistrationId);
            return;
        }

        // last operation (registration or unregistration) returned an error;
        Log.d(TAG, "Registration error: " + error);

        // Registration failed
        if (GCMConstants.ERROR_SERVICE_NOT_AVAILABLE.equals(error)) {
            boolean retry = onRecoverableError(context, error);
            if (retry) {

                int backoffTimeMs = GCMRegistrar.getBackoff(context);
                int nextAttempt = backoffTimeMs / 2 + sRandom.nextInt(backoffTimeMs);
                Log.d(TAG, "Scheduling registration retry, backoff = " + nextAttempt + " (" + backoffTimeMs + ")");

                Intent retryIntent = new Intent(GCMConstants.INTENT_FROM_GCM_LIBRARY_RETRY);
                retryIntent.putExtra(EXTRA_TOKEN, TOKEN);
                PendingIntent retryPendingIntent = PendingIntent.getBroadcast(context, 0, retryIntent, 0);

                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + nextAttempt, retryPendingIntent);

                // Next retry should wait longer.
                if (backoffTimeMs < MAX_BACKOFF_MS) {
                  GCMRegistrar.setBackoff(context, backoffTimeMs * 2);
                }

            } else {
                Log.d(TAG, "Not retrying failed operation");
            }

        } else {
            // Unrecoverable error, notify app
            onError(context, error);
        }

    }

}
