package com.deange.githubstatus.model;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;

import com.deange.githubstatus.R;
import com.deange.githubstatus.Utils;
import com.deange.githubstatus.http.GithubApi;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.HashMap;
import java.util.Map;

@DatabaseTable(tableName = "status")
public class Status extends BaseModel {

    public static final String STATUS = "status";
    public static final String BODY = "body";
    public static final String CREATED_ON = "created_on";
    public static final String VERSION = "version";

    private static final Map<String, Integer> mStatusMap = new HashMap<String, Integer>();
    static {
        mStatusMap.put(GithubApi.STATUS_UNAVAILABLE, R.string.error_server_unavailable_status);
        mStatusMap.put(GithubApi.STATUS_GOOD, R.string.status_good);
        mStatusMap.put(GithubApi.STATUS_MINOR, R.string.status_minor);
        mStatusMap.put(GithubApi.STATUS_MAJOR, R.string.status_major);
    }

    public enum SpecialType {
        ERROR, LOADING
    }

    @SerializedName(STATUS)
    private String mStatus;

    @SerializedName(BODY)
    private String mBody;

    @SerializedName(CREATED_ON)
    private String mCreatedOn;

    @DatabaseField(columnName = VERSION)
    private String mVersion;

    private boolean mSpecial = false;

    public static Status getSpecialStatus(final Context context, final SpecialType type) {

        final Time now = new Time();
        now.setToNow();

        final Status specialStatus = new Status();
        specialStatus.mCreatedOn = now.format3339(false);
        specialStatus.mSpecial = true;

        switch (type) {

            case ERROR:
                specialStatus.mStatus = context.getString(R.string.error_server_unavailable_status);
                specialStatus.mBody = context.getString(R.string.error_server_unavailable_message);
                break;

            case LOADING:
                specialStatus.mStatus = context.getString(R.string.loading_status);
                specialStatus.mBody = context.getString(R.string.loading_message);
                break;

        }

        return specialStatus;
    }

    public boolean isSpecialStatus() {
        return mSpecial;
    }

    public String getStatus() {
        return mStatus == null ? "" : mStatus;
    }

    public String getBody() {
        return mBody == null ? "" : mBody;
    }

    public Level getLevel() {
        return Level.from(getStatus());
    }

    public static String getTranslatedStatus(final Context context, final Status status) {

        final String translatedStatus;

        if (status != null && status.getStatus() != null) {
            final String key = status.getStatus().toLowerCase();
            if (!mStatusMap.containsKey(key)) {
                // Fallback to default string
                translatedStatus = key;

            } else {
                final Integer statusResId = mStatusMap.get(key);
                translatedStatus = context.getString(statusResId);
            }

        } else {

            if (context != null) {
                translatedStatus = context.getString(R.string.error_server_unavailable_status);

            } else {
                translatedStatus = "Unavailable";
            }

        }

        return translatedStatus;
    }

    public Time getCreatedOn() {
        if (TextUtils.isEmpty(mCreatedOn)) {
            return null;

        } else {
            final Time time = new Time(Time.TIMEZONE_UTC);
            time.parse3339(mCreatedOn);
            time.switchTimezone(Time.getCurrentTimezone());
            return time;
        }
    }

    public String getVersion() {
        if (mVersion == null) calculateVersion();
        return mVersion;
    }

    private void calculateVersion() {
        final String unhashedVersion = mId + mStatus + mBody + mCreatedOn;
        mVersion = Utils.hash(unhashedVersion);
    }

    public static boolean shouldAlert(final Status oldStatus, final Status newStatus) {

        if (oldStatus == null || newStatus == null) {
            return true;
        }

        // Alert on any message in MINOR/MAJOR status
        return newStatus.getLevel().isHigherThan(Level.GOOD) || oldStatus.getLevel() != newStatus.getLevel();
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
                ", mVersion='" + mVersion + '\'' +
                '}';
    }
}
