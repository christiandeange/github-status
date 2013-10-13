package com.deange.githubstatus.ui;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import com.deange.githubstatus.R;
import com.deange.githubstatus.http.GithubApi;
import com.deange.githubstatus.http.Status;

public final class ViewUtils {

    public static int resolveStatusColour(final Context context, final Status status) {

        final Resources res = context.getResources();

        if (status == null || status.getStatus() == null) {
            return res.getColor(R.color.status_major);
        }

        final String statusString = status.getStatus();
        int colourResId = android.R.color.black;

        if (GithubApi.STATUS_GOOD.equalsIgnoreCase(statusString)) {
            colourResId = R.color.status_good;

        } else if (GithubApi.STATUS_MINOR.equalsIgnoreCase(statusString)) {
            colourResId = R.color.status_minor;

        } else if (GithubApi.STATUS_MAJOR.equalsIgnoreCase(statusString)) {
            colourResId = R.color.status_major;
        }

        return res.getColor(colourResId);

    }

    public static void setVisibility(final View view, final boolean visibility) {
        view.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

}
