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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public abstract class PushBaseActivity
        extends FragmentActivity
        implements OnPushMessageReceivedListener {

    private static final String TAG = PushBaseActivity.class.getSimpleName();

    private final PushMessageReceiver mHandleGcmMessageReceiver = new PushMessageReceiver(this);
    private boolean mNeedToCheckPlayServices = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Listener for when a GCM message is received
        PushUtils.listenForGcmMessages(this, mHandleGcmMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNeedToCheckPlayServices) {
            checkPlayServices();
        }
    }

    protected void registerIfNecessary() {
        final String regId = PushRegistrar.getRegistrationId(this);
        if (!PushRegistrar.isRegistered(this)) {
            PushRegistrar.register(this);

        } else if (!PushServerRegistrar.isRegisteredOnServer(this)) {
            PushServerRegistrar.register(PushBaseActivity.this, regId, true);
        }
    }

    protected void unregisterIfNecessary() {
        final String regId = PushRegistrar.getRegistrationId(this);
        if (!regId.isEmpty()) {
            // Device is already registered on GCM, check server.
            if (PushServerRegistrar.isRegisteredOnServer(this)) {
                // Device is registered on server, unregister them
                PushRegistrar.unregister(this);
            }
        }
    }

    protected boolean checkPlayServices() {

        mNeedToCheckPlayServices = false;
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            return true;

        } else {
            Log.d(TAG, "isGooglePlayServicesAvailable = " + resultCode);

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                final Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
                dialog.setCancelable(false);
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
        PushUtils.unregisterForGcmMessages(this, mHandleGcmMessageReceiver);
        super.onDestroy();
    }

}
