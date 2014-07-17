package com.deange.githubstatus.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.deange.githubstatus.R;
import com.deange.githubstatus.model.Level;
import com.deange.githubstatus.model.Status;
import com.deange.githubstatus.ui.MainActivity;

public class NotificationController {

    private static final int NOTIFICATION_ID = 0xCafeBabe;
    private static final Object sLock = new Object();
    private static NotificationController sInstance;

    private final Context mContext;
    private final NotificationManager mNotificationManager;

    public static synchronized NotificationController createInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new NotificationController(context.getApplicationContext());
        }
        return sInstance;
    }

    public static NotificationController getInstance() {
        synchronized (sLock) {
            if (sInstance == null) {
                throw new IllegalStateException("Notification controller instance invalid");
            }
            return sInstance;
        }
    }

    private NotificationController(final Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void notificationForStatus(final Status status) {

        if (status == null || status.isSpecialStatus()) {
            // Do not show status for a special or null status
            return;
        }

        final String title = mContext.getString(R.string.app_name);
        final String level = Status.getTranslatedStatus(mContext, status).toUpperCase();
        final String body = status.getBody();
        final String notifBody = level + " - " + body;

        final long when = System.currentTimeMillis();
        final int tickerIcon = R.drawable.ic_stat_octocat;
        final int icon = statusIconForLevel(status.getLevel());
        final Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), icon);

        final Intent notificationIntent = new Intent(mContext, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent intent = PendingIntent.getActivity(
                mContext, NOTIFICATION_ID, notificationIntent, 0);

        final NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle()
                .bigText(notifBody);

        final Notification notification = new NotificationCompat.Builder(mContext)
                .setContentTitle(title)
                .setContentText(notifBody)
                .setSmallIcon(tickerIcon)
                .setLargeIcon(largeIcon)
                .setTicker(notifBody)
                .setStyle(style)
                .setWhen(when)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build();

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    private int statusIconForLevel(final Level level) {

        if (level == null) {
            return R.drawable.octocat_notif_green;

        } else {
            switch (level) {
                case MAJOR:
                    return R.drawable.octocat_notif_red;

                case MINOR:
                    return R.drawable.octocat_notif_yellow;

                case GOOD:
                default:
                    return R.drawable.octocat_notif_green;
            }
        }

    }

}
