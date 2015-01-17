package com.deange.githubstatus.push;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class PushBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = PushBroadcastReceiver.class.getSimpleName();

    @Override
    public final void onReceive(final Context context, final Intent intent) {
        Log.v(TAG, "onReceive(): " + intent.getAction());

        PushIntentService.runIntentInService(context, intent, PushIntentService.class.getName());
        setResult(Activity.RESULT_OK, null, null);
    }
}
