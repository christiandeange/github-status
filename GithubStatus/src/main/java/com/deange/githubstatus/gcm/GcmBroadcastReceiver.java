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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GCMBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GCMBroadcastReceiver";

    @Override
    public final void onReceive(final Context context, final Intent intent) {
        Log.v(TAG, "onReceive(): " + intent.getAction());

        final String className = getGCMIntentServiceClassName(context);
        Log.v(TAG, "GCM IntentService class: " + className);
        // Delegates to the application-specific intent service.
        GCMBaseIntentService.runIntentInService(context, intent, className);
        setResult(Activity.RESULT_OK, null, null);
    }

    protected String getGCMIntentServiceClassName(final Context context) {
        return getDefaultIntentServiceClassName();
    }

    private static String getDefaultIntentServiceClassName() {
        return GCMConstants.DEFAULT_INTENT_SERVICE_CLASS_NAME;
    }
}
