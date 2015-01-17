package com.deange.githubstatus.ui;

public class SettingsInfo {

    private final boolean gcmEnabled;

    private SettingsInfo(final Builder builder) {
        this.gcmEnabled = builder.gcmEnabled;
    }

    public boolean isGCMEnabled() {
        return gcmEnabled;
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
