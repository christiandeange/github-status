package com.deange.githubstatus.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PushMessageReceiver
    extends BroadcastReceiver {

    private static final String TAG = PushMessageReceiver.class.getSimpleName();

    private final OnPushMessageReceivedListener mListener;
    private final String mAction;

    public PushMessageReceiver(final OnPushMessageReceivedListener listener) {
        mListener = listener;
        mAction = PushUtils.ACTION_GCM_MESSAGE_RECEIVED;
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
            mListener.onPushMessageReceived(intent);
        }
    }
}
