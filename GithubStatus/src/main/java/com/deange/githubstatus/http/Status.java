package com.deange.githubstatus.http;

import android.content.Context;
import android.text.format.Time;

import com.deange.githubstatus.R;
import com.google.gson.annotations.SerializedName;

public class Status {

    public static final String STATUS = "status";
    public static final String BODY = "body";
    public static final String CREATED_ON = "created_on";

    @SerializedName(STATUS)
    private String mStatus;

    @SerializedName(BODY)
    private String mBody;

    @SerializedName(CREATED_ON)
    private String mCreatedOn;

    private static Status ERROR_STATUS;

    public static Status getErrorStatus(final Context context) {

        if (ERROR_STATUS == null) {
            final Time now = new Time();
            now.setToNow();

            ERROR_STATUS = new Status();
            ERROR_STATUS.mCreatedOn = now.format3339(false);
            ERROR_STATUS.mStatus = context.getString(R.string.error_server_unavailable_status);
            ERROR_STATUS.mBody = context.getString(R.string.error_server_unavailable_message);
        }

        return ERROR_STATUS;
    }

    public String getStatus() {
        return mStatus;
    }

    public String getBody() {
        return mBody;
    }

    public Time getCreatedOn() {
        final Time time = new Time();
        time.parse3339(mCreatedOn);
        return time;
    }

    private Status() {
        // Uninstantiable
    }

    @Override
    public String toString() {
        return "Status{" +
                "mStatus='" + mStatus + '\'' +
                ", mBody='" + mBody + '\'' +
                ", mCreatedOn='" + mCreatedOn + '\'' +
                '}';
    }
}
