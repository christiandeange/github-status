package com.deange.githubstatus.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.deange.githubstatus.model.Status;

public class StateController {

    private static final String TAG = StateController.class.getSimpleName();

    private static final String PREFERENCES_NAME = TAG + ".prefs";
    private static final String KEY_SAVED_STATUS = "last_status";

    private static final Object sLock = new Object();
    private static StateController sInstance;

    private SharedPreferences mPreferences;

    public static synchronized void createInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new StateController(context.getApplicationContext());
        }
    }

    public void setStatus(final Status status) {
        synchronized (sLock) {
            mPreferences.edit().putString(
                    KEY_SAVED_STATUS, GsonController.getInstance().toJson(status)).apply();
        }
    }

    public Status getStatus() {
        synchronized (sLock) {
            return GsonController.getInstance().fromJson(
                    mPreferences.getString(KEY_SAVED_STATUS, null), Status.class);
        }
    }

    public void clear() {
        synchronized (sLock) {
            mPreferences.edit().clear().apply();
        }
    }

    public static StateController getInstance() {
        synchronized (sLock) {
            if (sInstance == null) {
                throw new IllegalStateException("StateController has not been created");
            }
            return sInstance;
        }
    }

    private StateController(final Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}
