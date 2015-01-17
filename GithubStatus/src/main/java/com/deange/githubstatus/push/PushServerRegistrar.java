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
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;

import com.deange.githubstatus.Utils;
import com.deange.githubstatus.http.HttpIOException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public final class PushServerRegistrar {

    private static final String TAG = PushServerRegistrar.class.getSimpleName();
    /**
     * Default lifespan (7 days) of the {@link PushServerRegistrar#isRegisteredOnServer(Context)}
     * flag until it is considered expired.
     */
    public static final long DEFAULT_ON_SERVER_LIFESPAN_MS = TimeUnit.DAYS.toMillis(7);

    private static final String PREFERENCES = Utils.buildPreferences("server.registrar");
    private static final String PROPERTY_ON_SERVER = "onServer";
    private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTime";

    private static final String RELEASE_SERVER_URL = "http://githubstatus.appspot.com";
    private static final String SERVER_URL = RELEASE_SERVER_URL; // BuildConfig.SERVER_URL;
    private static final int MAX_ATTEMPTS = 5;

    static void register(final Context context, final String regId) {
        register(context, regId, false);
    }

    static void register(final Context context, final String regId, final boolean async) {
        Log.i(TAG, "registering device (regId = " + regId + ")");

        final String serverUrl = SERVER_URL + "/register";
        final Map<String, String> params = new HashMap<>();
        params.put("id", regId);

        Log.v(TAG, "Registering at '" + serverUrl + "'");

        final BackoffHandler task = new BackoffHandler(MAX_ATTEMPTS, async) {
            @Override
            public boolean performAction() throws Throwable {
                post(serverUrl, params);
                return true;
            }

            @Override
            public void onActionCompleted(final boolean success) {
                setRegisteredOnServer(context, success);
            }
        };

        task.start();
    }

    static void unregister(final Context context, final String regId) {
        Log.i(TAG, "unregistering device (regId = " + regId + ")");

        final String serverUrl = SERVER_URL + "/unregister";
        final Map<String, String> params = new HashMap<>();
        params.put("id", regId);

        try {
            post(serverUrl, params);

        } catch (final IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
        } finally {
            setRegisteredOnServer(context, false);
        }
    }

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

    public static boolean isRegisteredOnServer(final Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        final boolean isRegistered = prefs.getBoolean(PROPERTY_ON_SERVER, false);
        Log.v(TAG, "Is registered on server: " + isRegistered);

        if (isRegistered) {
            // checks if the information is not stale
            final long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
            if (System.currentTimeMillis() > expirationTime) {
                Log.v(TAG, "flag expired on: " + new Timestamp(expirationTime));
                return false;
            }
        }

        return isRegistered;
    }

    public static void setRegisteredOnServer(final Context context, final boolean flag) {
        final SharedPreferences prefs = getGCMPreferences(context);
        final long lifespan = DEFAULT_ON_SERVER_LIFESPAN_MS;
        final long expirationTime = System.currentTimeMillis() + lifespan;
        Log.v(TAG, "Setting registeredOnServer status as " + flag + " until "
                + new Timestamp(expirationTime));

        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PROPERTY_ON_SERVER, flag);
        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
        editor.apply();
    }

    private static SharedPreferences getGCMPreferences(final Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
