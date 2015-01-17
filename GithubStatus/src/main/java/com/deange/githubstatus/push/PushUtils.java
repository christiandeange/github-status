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
import android.content.IntentFilter;

import com.deange.githubstatus.Utils;

public final class PushUtils {

    public static final String TAG = PushUtils.class.getSimpleName();
    static final String ACTION_GCM_MESSAGE_RECEIVED = Utils.buildAction("GCM_MESSAGE_RECEIVED");

    public static void listenForGcmMessages(final Context context, final PushMessageReceiver receiver) {
        context.registerReceiver(receiver, new IntentFilter(receiver.getAction()));
    }

    public static void unregisterForGcmMessages(final Context context, final PushMessageReceiver receiver) {
        context.unregisterReceiver(receiver);
    }
}
