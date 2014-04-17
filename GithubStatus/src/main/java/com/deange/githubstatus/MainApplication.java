package com.deange.githubstatus;

import android.app.Application;
import android.util.Log;

import com.deange.githubstatus.controller.GsonController;
import com.deange.githubstatus.controller.StateController;

public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();

    private static String sPackagePrefix;

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate()");
        super.onCreate();

        sPackagePrefix = getPackageName();

        try {
            // Initialize the Gson singleton
            GsonController.getInstance();

            // Initialize the SharedPreferences wrapper
            StateController.createInstance(getApplicationContext());

        } catch (final Exception e) {
            Log.wtf(TAG, "Fatal error occured!", e);
        }
    }

    public static String buildAction(final String action) {
        return sPackagePrefix + "." + action;
    }

}
