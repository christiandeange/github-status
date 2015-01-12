package com.deange.githubstatus.http;

import android.content.Context;
import android.util.Log;

import com.deange.githubstatus.Utils;
import com.deange.githubstatus.GsonController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class SimpleApi<T> extends BaseApi<T> {

    private static final String TAG = SimpleApi.class.getSimpleName();

    public SimpleApi(final Context context) {
        super(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(final Type clazz, final String url) throws IOException {

        // Create HTTP request
        final String apiUrl = normalizeUrl(url);
        final HttpURLConnection connection = getClient().open(new URL(apiUrl));

        // Retrieve GET response
        final InputStream in = connection.getInputStream();
        final String outputJson = Utils.streamToString(in);
        in.close();

        try {
            return (T) GsonController.getInstance().fromJson(outputJson, clazz);

        } catch (final Exception ex) {
            Log.w(TAG, "GET from " + apiUrl + " failed with " + connection.getResponseCode() + ".");
            ex.printStackTrace();
            return null;
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public T post(final T entity, final String url) throws IOException {

        // Create HTTP request
        final String apiUrl = normalizeUrl(url);
        final String json = GsonController.getInstance().toJson(entity);
        final HttpURLConnection connection = getClient().open(new URL(apiUrl));

        // Write POST request
        connection.setRequestMethod("POST");
        final OutputStream out = connection.getOutputStream();
        out.write(json.getBytes());
        out.close();

        // Retrieve POST response
        final InputStream in = connection.getInputStream();
        final String outputJson = Utils.streamToString(in);
        in.close();

        try {
            return GsonController.getInstance().fromJson(outputJson, (Class<T>) entity.getClass());

        } catch (final Exception ex) {
            Log.w(TAG, "POST " + json + " to " + apiUrl + " failed with " + connection.getResponseCode() + ".");
            ex.printStackTrace();
            return null;
        }

    }

    private String normalizeUrl(final String apiEndpoint) {

        String base = getBaseApiEndpoint();
        if (!base.isEmpty() && base.charAt(base.length() - 1) == '/') {
            // Trim trailing slash
            base = base.substring(0, base.length() - 1);
        }

        // Ensure API path has leading slash
        final String apiPath;
        if (apiEndpoint.startsWith("/")) {
            apiPath = apiEndpoint;

        } else {
            apiPath = "/" + apiEndpoint;
        }

        return base + apiPath;
    }

}
