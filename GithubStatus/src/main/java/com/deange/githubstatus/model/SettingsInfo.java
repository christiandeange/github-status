package com.deange.githubstatus.model;

public class SettingsInfo {

    private final boolean mGcmEnabled;

    private SettingsInfo(final Builder builder) {
        mGcmEnabled = builder.gcmEnabled;
    }

    public boolean isGCMEnabled() {
        return mGcmEnabled;
    }

    public static final class Builder {

        private boolean gcmEnabled;

        public Builder gcm(final boolean enabled) {
            gcmEnabled = enabled;
            return this;
        }

        public SettingsInfo build() {
            return new SettingsInfo(this);
        }
    }
}
