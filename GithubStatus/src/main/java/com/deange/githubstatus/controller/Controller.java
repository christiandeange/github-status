package com.deange.githubstatus.controller;

import android.os.Bundle;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Controller {

    public static final String TAG = Controller.class.getSimpleName();

    public static final int EVENT_RECEIVED_GCM = bit(1);

    private static Controller sInstance;
    private static final Object sLock = new Object();
    private final Set<Listener> mListeners = Collections.synchronizedSet(new HashSet<Listener>());

    private Controller() {
    }

    public synchronized static Controller getInstance() {
        if (sInstance == null) {
            sInstance = new Controller();
        }
        return sInstance;
    }

    public void register(final Listener listener) {
        Log.v(TAG, "register()");
        synchronized (sLock) {
            mListeners.add(listener);
        }
    }

    public void unregister(final Listener listener) {
        Log.v(TAG, "unregister()");
        synchronized (sLock) {
            mListeners.remove(listener);
        }
    }

    public void onGcmNotificationReceived(final Bundle data) {
        Log.v(TAG, "onGcmNotificationReceived()");
        notify(EVENT_RECEIVED_GCM, data);
    }

    public void notify(final int event, final Bundle data) {

        synchronized (sLock) {
            for (final Listener listener : mListeners) {
                if (listener != null && (listener.getEventFilter() & event) != 0) {
                    listener.onEvent(event, data);
                }
            }
        }

    }

    private static int bit(final int bit) {
        return 1 << bit;
    }

    public interface Listener {
        public int getEventFilter();

        public void onEvent(final int eventType, final Bundle data);
    }
}
