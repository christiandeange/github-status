package com.deange.githubstatus.http;

import android.content.Context;
import android.os.AsyncTask;

import com.deange.githubstatus.Status;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class GithubApi {

    // URL ENDPOINTS
    public static final String BASE_URL = "https://status.github.com";
    public static final String BASE_API_URL = BASE_URL + "/api";
    public static final String JSON = ".json";

    public static final String STATUS = "/status" + JSON;
    public static final String LAST_MESSAGE = "/last-message" + JSON;
    public static final String LAST_MESSAGES = "/messages" + JSON;


    // STATUS CODES
    public static final String STATUS_GOOD = "good";
    public static final String STATUS_MINOR = "minor";
    public static final String STATUS_MAJOR = "major";

    // HTTP METHODS

    public static void getStatus(final Context context, final String url, final HttpTask.Listener<Status> listener) {
        doApiGet(new GithubStatusApi(context), Status.class, url, listener);
    }

    public static void getMessages(final Context context, final String url, final HttpTask.Listener<List<Status>> listener) {
        doApiGet(new GithubStatusMessagesApi(context), new TypeToken<List<Status>>(){}.getType(), url, listener);
    }

    private static <T> void doApiGet(final BaseApi<T> api, final Type clazz, final String url, final HttpTask.Listener<T> listener) {
        new AsyncTask<Void, Void, T>() {

            Exception ex = null;

            @Override
            protected T doInBackground(final Void... params) {
                try {
                    return api.get(clazz, url);
                } catch (IOException e) {

                    ex = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final T entity) {
                if (listener != null) {
                    listener.onGet(entity, ex);
                }
            }
        }.execute();
    }

    private GithubApi() {
        // Uninstantiable
    }

}
