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

/**
 * Constants used by the GCM library.
 */
public final class PushConstants {

    private static final String C2DM_PACKAGE = "com.google.android.c2dm";

    public static final String INTENT_REGISTRATION_CALLBACK = build("REGISTRATION");
    public static final String INTENT_MESSAGE = build("RECEIVE");

    public static final String EXTRA_SENDER = "sender";
    public static final String EXTRA_APPLICATION_PENDING_INTENT = "app";
    public static final String EXTRA_UNREGISTERED = "unregistered";
    public static final String EXTRA_ERROR = "error";
    public static final String EXTRA_REGISTRATION_ID = "registration_id";
    public static final String EXTRA_SPECIAL_MESSAGE = "message_type";

    public static final String PERMISSION_GCM_INTENTS = C2DM_PACKAGE + ".permission.SEND";

    public static final String ERROR_SERVICE_NOT_AVAILABLE =
            "SERVICE_NOT_AVAILABLE";

    private static String build(final String name) {
        return C2DM_PACKAGE + "." + "intent" + "." + name;
    }

    private PushConstants() {
        throw new UnsupportedOperationException();
    }
}
