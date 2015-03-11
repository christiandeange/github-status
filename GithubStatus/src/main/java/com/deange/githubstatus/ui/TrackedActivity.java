package com.deange.githubstatus.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class TrackedActivity
        extends ActionBarActivity {

    private static int sActiveActivities = 0;
    private static int sVisibleActivities = 0;

    public static int getActiveActivities() {
        return sActiveActivities;
    }

    public static int getVisibleActivities() {
        return sVisibleActivities;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        sActiveActivities++;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        sVisibleActivities++;
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sVisibleActivities--;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sActiveActivities--;
    }

}
