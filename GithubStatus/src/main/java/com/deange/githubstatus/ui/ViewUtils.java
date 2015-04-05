package com.deange.githubstatus.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;

import com.deange.githubstatus.R;
import com.deange.githubstatus.http.GithubApi;
import com.deange.githubstatus.model.Status;

public final class ViewUtils {

    public static int resolveStatusColour(final Context context, final Status status) {

        if (context == null) {
            return Color.BLACK;
        }

        if (status == null || status.getStatus() == null) {
            return context.getResources().getColor(R.color.status_major);
        }

        final int colourResId;
        final Resources res = context.getResources();
        final String statusString = status.getStatus();

        if (GithubApi.STATUS_GOOD.equalsIgnoreCase(statusString)) {
            colourResId = R.color.status_good;

        } else if (GithubApi.STATUS_MINOR.equalsIgnoreCase(statusString)) {
            colourResId = R.color.status_minor;

        } else if (GithubApi.STATUS_MAJOR.equalsIgnoreCase(statusString)) {
            colourResId = R.color.status_major;

        } else {
            colourResId = android.R.color.black;
        }

        return res.getColor(colourResId);

    }

    public static void setVisibility(final View view, final boolean visibility) {
        view.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

}
