package com.deange.githubstatus.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GCMMessageReceiver
    extends BroadcastReceiver {

    private static final String TAG = GCMMessageReceiver.class.getSimpleName();

    private final OnGCMMessageReceivedListener mListener;
    private final String mAction;

    public GCMMessageReceiver(final OnGCMMessageReceivedListener listener) {
        this(listener, GCMUtils.ACTION_GCM_MESSAGE_RECEIVED);
    }

    public GCMMessageReceiver(final OnGCMMessageReceivedListener listener, final String action) {
        mListener = listener;
        mAction = action;
    }

    public String getAction() {
        return mAction;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (!mAction.equals(intent.getAction())) {
            return;
        }

        if (mListener == null) {
            Log.v(TAG, "mListener is null!");

        } else {
            mListener.onGcmMessageReceived(intent);
        }
    }
}
