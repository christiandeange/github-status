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

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deange.githubstatus.R;
import com.deange.githubstatus.controller.NotificationController;
import com.deange.githubstatus.controller.StateController;
import com.deange.githubstatus.http.GithubApi;
import com.deange.githubstatus.model.Status;

import java.io.IOException;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService
        extends GCMBaseIntentService
        implements OnGCMMessageReceivedListener {

    private static final String TAG = GCMIntentService.class.getSimpleName();

    public GCMIntentService() {
        super(GCMUtils.SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);

        GCMUtils.displayMessage(context, getString(R.string.gcm_registered));
        GCMServerUtilities.register(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");

        GCMUtils.displayMessage(context, getString(R.string.gcm_unregistered));

        if (GCMRegistrar.isRegisteredOnServer(context)) {
            GCMServerUtilities.unregister(context, registrationId);

        } else {
            // This callback results from the call to unregister made on
            // GCMServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");

        String message = getString(R.string.gcm_message);
        GCMUtils.displayMessage(context, message);

        // Notify other classes
        GCMUtils.onGcmMessageReceived(context, intent.getExtras());

        // Notify ourselves
        onGcmMessageReceived(intent);
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");

        GCMUtils.displayMessage(context, getString(R.string.gcm_deleted, total));
        super.onDeletedMessages(context, total);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);

        GCMUtils.displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.i(TAG, "Received recoverable error: " + errorId);

        GCMUtils.displayMessage(context, getString(R.string.gcm_recoverable_error, errorId));
        return super.onRecoverableError(context, errorId);
    }

    @Override
    public void onGcmMessageReceived(final Intent intent) {

        final Status newStatus;
        try {
            newStatus = GithubApi.getStatus(getApplicationContext());

        } catch (IOException e) {
            // Cannot retrieve new status information
            return;
        }

        final Status oldStatus = StateController.getInstance().getStatus();

        if (Status.shouldAlert(oldStatus, newStatus)) {
            // Pop a notification that the status has now changed!
            NotificationController.getInstance().notificationForStatus(newStatus);
        }

        // Save the new status
        StateController.getInstance().setStatus(newStatus);
    }
}
