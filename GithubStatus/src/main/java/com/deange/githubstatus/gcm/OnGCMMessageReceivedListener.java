package com.deange.githubstatus.gcm;

import android.content.Intent;

public interface OnGCMMessageReceivedListener {
    public void onGcmMessageReceived(final Intent intent);
}
