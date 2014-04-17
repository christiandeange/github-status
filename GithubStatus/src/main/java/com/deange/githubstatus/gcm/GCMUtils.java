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

import com.deange.githubstatus.BuildConfig;
import com.deange.githubstatus.MainApplication;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class GCMUtils {

    public static final String TAG = GCMUtils.class.getSimpleName();

    public static final String SENDER_ID = "196611706338";
    public static final String SERVER_URL;
    static {
        if (BuildConfig.DEBUG) {
            SERVER_URL = "http://10.0.0.122:8080";

        } else {
            SERVER_URL = "http://githubstatus.appspot.com";
        }
    }


    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_BUNDLE = "bundle";

    public static final String ACTION_DISPLAY_MESSAGE =
            MainApplication.buildAction("DISPLAY_MESSAGE");

    public static final String ACTION_GCM_MESSAGE_RECEIVED =
            MainApplication.buildAction("GCM_MESSAGE_RECEIVED");

    public static void displayMessage(final Context context, final String message) {
        Intent intent = new Intent(ACTION_DISPLAY_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }

    public static void onGcmMessageReceived(final Context context, final Bundle extras) {
        Intent intent = new Intent(ACTION_GCM_MESSAGE_RECEIVED);
        intent.putExtra(EXTRA_BUNDLE, extras);
        context.sendBroadcast(intent);
    }

    public static void listenForGcmMessages(final Context context, final BroadcastReceiver receiver) {
        context.registerReceiver(receiver, new IntentFilter(GCMUtils.ACTION_DISPLAY_MESSAGE));
    }

    public void unregisterForGcmMessages(final Context context, final BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }
}
