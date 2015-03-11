package com.deange.githubstatus.push;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.deange.githubstatus.Utils;
import com.deange.githubstatus.controller.NotificationController;
import com.deange.githubstatus.controller.StateController;
import com.deange.githubstatus.http.GithubApi;
import com.deange.githubstatus.model.Status;
import com.deange.githubstatus.ui.TrackedActivity;

import java.io.IOException;

public class PushIntentService extends IntentService {

    private static final String TAG = PushIntentService.class.getSimpleName();

    private static final String WAKELOCK_KEY = Utils.buildAction("wakelock.key");
    private static final Object sLock = new Object();

    private static PowerManager.WakeLock sWakeLock;

    public PushIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {

        try {
            final Context context = getApplicationContext();
            final String action = intent.getAction();

            if (action == null) {
                Log.v(TAG, "Empty action from intent: \'" + intent + "\'");

            } else if (action.equals(PushConstants.INTENT_REGISTRATION_CALLBACK)) {
                handleRegistration(context, intent);

            } else if (action.equals(PushConstants.INTENT_MESSAGE)) {
                final String type = intent.getStringExtra(PushConstants.EXTRA_SPECIAL_MESSAGE);
                onMessage(context, intent, type);
            }

        } finally {
            synchronized (sLock) {
                if (sWakeLock != null && sWakeLock.isHeld()) {
                    Log.v(TAG, "Releasing wakelock");
                    sWakeLock.release();
                }
            }
        }
    }

    static void runIntentInService(final Context context, final Intent intent,
                                          final String className) {
        synchronized (sLock) {
            if (sWakeLock == null) {
                sWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
                        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_KEY);
            }
            Log.v(TAG, "Acquiring wakelock");
            sWakeLock.acquire();
        }

        intent.setClassName(context, className);
        context.startService(intent);
    }

    private void handleRegistration(final Context context, Intent intent) {
        final String error = intent.getStringExtra(PushConstants.EXTRA_ERROR);
        final String registrationId = intent.getStringExtra(PushConstants.EXTRA_REGISTRATION_ID);
        final String unregistered = intent.getStringExtra(PushConstants.EXTRA_UNREGISTERED);
        Log.v(TAG, "handleRegistration: registrationId = " + registrationId +
                ", error = " + error + ", unregistered = " + unregistered);

        // registration succeeded
        if (registrationId != null) {
            PushRegistrar.setRegistrationId(context, registrationId);
            onRegistered(context, registrationId);
            return;
        }

        // unregistration succeeded
        if (unregistered != null) {
            final String oldRegistrationId = PushRegistrar.clearRegistrationId(context);
            onUnregistered(context, oldRegistrationId);
            return;
        }

        // last operation (registration or unregistration) returned an error;
        Log.v(TAG, "Registration error: " + error);

        // Registration failed
        onError(context, error);

    }

    public void onRegistered(final Context context, final String registrationId) {

    }

    public void onUnregistered(final Context context, final String oldRegistrationId) {

    }

    public void onMessage(final Context context, final Intent intent, final String type) {

        final Status newStatus;
        try {
            newStatus = GithubApi.getStatus(context);

        } catch (final IOException e) {
            // Cannot retrieve new status information
            return;
        }

        final Status oldStatus = StateController.getInstance().getStatus();
        final boolean alert = Status.shouldAlert(oldStatus, newStatus);

        // Save the new status
        StateController.getInstance().setStatus(newStatus);

        if (alert) {
            if (TrackedActivity.getVisibleActivities() == 0) {
                // Pop a notification that the status has now changed!
                NotificationController.getInstance().notificationForStatus(newStatus);
            }
        }

        // Let other active listeners know that we received a message
        final Intent broadcast = new Intent();
        broadcast.setAction(PushUtils.ACTION_GCM_MESSAGE_RECEIVED);
        context.sendBroadcast(broadcast);
    }

    public void onError(final Context context, final String error) {

    }

}
