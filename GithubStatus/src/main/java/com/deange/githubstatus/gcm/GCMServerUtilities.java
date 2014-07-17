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
import android.util.Log;

import com.deange.githubstatus.R;
import com.deange.githubstatus.http.HttpIOException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Helper class used to communicate with the demo server.
 */
public final class GCMServerUtilities {

    private static final String TAG = GCMServerUtilities.class.getSimpleName();

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 500;
    private static final Random sRandom = new Random();

    /**
     * Register this account/device pair within the server.
     *
     * @return whether the registration succeeded or not.
     */
    static boolean register(final Context context, final String regId) {
        Log.i(TAG, "registering device (regId = " + regId + ")");

        final String serverUrl = GCMUtils.SERVER_URL + "/register";
        final Map<String, String> params = new HashMap<>();
        params.put("regId", regId);

        Log.v(TAG, "Registering at '" + serverUrl + "'");

        long backoff = BACKOFF_MILLI_SECONDS + sRandom.nextInt(1000);

        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");

            try {

                GCMUtils.displayMessage(context, context.getString(
                        R.string.server_registering, i, MAX_ATTEMPTS));
                post(serverUrl, params);
                GCMRegistrar.setRegisteredOnServer(context, true);

                final String message = context.getString(R.string.server_registered);
                GCMUtils.displayMessage(context, message);

                return true;

            } catch (final HttpIOException e) {

                if ((e.isServerError()) && (i != MAX_ATTEMPTS)) {
                    try {
                        Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                        Thread.sleep(backoff);

                    } catch (final InterruptedException e1) {
                        // Activity finished before we complete - exit.
                        Log.d(TAG, "Thread interrupted: abort remaining retries!");
                        Thread.currentThread().interrupt();
                        return false;
                    }

                    // increase backoff exponentially
                    backoff *= 2;
                }

            } catch (final IOException e) {
                Log.e(TAG, "Failed to register on attempt " + i, e);
            }

        }

        final String message = context.getString(R.string.server_register_error, MAX_ATTEMPTS);
        GCMUtils.displayMessage(context, message);

        return false;
    }

    /**
     * Unregister this account/device pair within the server.
     */
    static void unregister(final Context context, final String regId) {
        Log.i(TAG, "unregistering device (regId = " + regId + ")");

        final String serverUrl = GCMUtils.SERVER_URL + "/unregister";
        final Map<String, String> params = new HashMap<>();
        params.put("regId", regId);

        try {
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
            GCMUtils.displayMessage(context, context.getString(R.string.server_unregistered));

        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            final String message = context.getString(R.string.server_unregister_error,
                    e.getMessage());
            GCMUtils.displayMessage(context, message);
        }

    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     *
     * @throws java.io.IOException propagated from POST.
     */
    private static void post(final String endpoint, final Map<String, String> params)
            throws IOException {

        final URL url;
        try {
            url = new URL(endpoint);

        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }

        final StringBuilder bodyBuilder = new StringBuilder();
        final Iterator<Entry<String, String>> iterator = params.entrySet().iterator();

        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            final Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }

        final String body = bodyBuilder.toString();
        final byte[] bytes = body.getBytes();
        Log.v(TAG, "Posting '" + body + "' to " + url);
        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            // post the request
            final OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            // handle the response
            final int status = conn.getResponseCode();
            if (status < 200 || status > 299) {
                throw new HttpIOException("Post failed with error code " + status, status);
            }

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }
}
