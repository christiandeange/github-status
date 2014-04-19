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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.deange.githubstatus.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public abstract class GCMBaseActivity
        extends FragmentActivity
        implements OnGCMMessageReceivedListener {

    private static final String TAG = GCMBaseActivity.class.getSimpleName();

    private AsyncTask<Void, Void, Void> mRegisterTask;
    private final GCMMessageReceiver mHandleGcmMessageReceiver = new GCMMessageReceiver(this);
    private boolean mNeedToCheckPlayServices = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkNotNull(GCMUtils.SERVER_URL, "SERVER_URL");
        checkNotNull(GCMUtils.SENDER_ID, "SENDER_ID");

        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);
        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this);

        // Listener for when a GCM message is received
        GCMUtils.listenForGcmMessages(this, mHandleGcmMessageReceiver);

        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.isEmpty()) {
            // Automatically registers application on startup.
            GCMRegistrar.register(this, GCMUtils.SENDER_ID);

        } else {
            // Device is already registered on GCM, check server.
            if (GCMRegistrar.isRegisteredOnServer(this)) {
                // Skips registration.
                Log.v(TAG, "Already registered!");

            } else {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                final Context context = this;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        boolean registered =
                                GCMServerUtilities.register(context, regId);
                        // At this point all attempts to register with the app
                        // server failed, so we need to unregister the device
                        // from GCM - the app will try to register again when
                        // it is restarted. Note that GCM will send an
                        // unregistered callback upon completion, but
                        // GCMIntentService.onUnregistered() will ignore it.
                        if (!registered) {
                            GCMRegistrar.unregister(context);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }

                };
                mRegisterTask.execute();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNeedToCheckPlayServices) {
            checkPlayServices();
        }
    }

    private boolean checkPlayServices() {

        mNeedToCheckPlayServices = false;
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (resultCode == ConnectionResult.SUCCESS) {
            return true;

        } else {
            Log.d(TAG, "isGooglePlayServicesAvailable = " + resultCode);

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                final Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
                dialog.setCancelable(false);

                // The dialog is dismissed when the user taps Update and launches the Play Store.
                // We need to look again to see if they actually installed Play Services.

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mNeedToCheckPlayServices = true;
                    }
                });
                dialog.show();

            } else {
                Log.e(TAG, "Unrecoverable error checking Google Play Services.");
                finish();
            }

            return false;
        }
    }

    @Override
    protected void onDestroy() {
        if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }

        GCMUtils.unregisterForGcmMessages(this, mHandleGcmMessageReceiver);
        GCMRegistrar.onDestroy(this);
        super.onDestroy();
    }

    private void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException(getString(R.string.error_config, name));
        }
    }

}