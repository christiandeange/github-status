package com.deange.githubstatus.ui;

public class SettingsInfo {

    public final boolean gcmEnabled;

    public SettingsInfo(final boolean gcmEnabled) {
        this.gcmEnabled = gcmEnabled;
    }

    public static final class Builder {

        private boolean gcmEnabled;

        public Builder gcm(final boolean enabled) {
            gcmEnabled = gcmEnabled;
            return this;
        }

        public SettingsInfo build() {
            return new SettingsInfo(gcmEnabled);
        }
    }
}
