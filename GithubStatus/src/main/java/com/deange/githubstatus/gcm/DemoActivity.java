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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.deange.githubstatus.R;

import java.util.Date;

/**
 * Main UI for the demo app.
 */
public class DemoActivity extends GCMBaseActivity {

    TextView mDisplay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gcm);
        mDisplay = (TextView) findViewById(R.id.display);

        // Listen for display messages only
        registerReceiver(mMessageReceiver,
                new IntentFilter(GCMUtils.ACTION_DISPLAY_MESSAGE));

        final String regId = GCMRegistrar.getRegistrationId(this);
        final boolean registered = GCMRegistrar.isRegisteredOnServer(this);
        if (!regId.isEmpty() && registered) {
            // Device is already registered on GCM, skips registration.
            appendNewMessage(getString(R.string.already_registered));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            /*
             * Typically, an application registers automatically, so options
             * below are disabled. Uncomment them if you want to manually
             * register or unregister the device (you will also need to
             * uncomment the equivalent options on options_menu.xml).
             */
            case R.id.options_register:
                GCMRegistrar.register(this, GCMUtils.SENDER_ID);
                return true;

            case R.id.options_unregister:
                GCMRegistrar.unregister(this);
                return true;

            case R.id.options_clear:
                mDisplay.setText(null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void appendNewMessage(final String message) {
        mDisplay.append(new Date() + " > " + message + "\n");
    }

    @Override
    public void onGcmMessageReceived(final Intent intent) {

        final StringBuilder sb = new StringBuilder("[");
        for (final String key : intent.getExtras().keySet()) {
            sb.append(key);
            sb.append(" = ");
            sb.append(intent.getExtras().get(key));
            sb.append(", ");
        }

        sb.append("]");


        appendNewMessage(sb.toString());
    }

    private final BroadcastReceiver mMessageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            appendNewMessage(intent.getExtras().getString(GCMUtils.EXTRA_MESSAGE));
        }
    };

}