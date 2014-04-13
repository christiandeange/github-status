package com.deange.githubstatus.model;

import com.deange.githubstatus.http.GithubApi;

public enum Level {
	GOOD(GithubApi.STATUS_GOOD),
	MINOR(GithubApi.STATUS_MINOR),
	MAJOR(GithubApi.STATUS_MAJOR);

	public String type;

	Level(final String t) {
		type = t;
	}

	@Override
	public String toString() {
		return type;
	}

	public static Level from(final String string) {
		for (final Level level : values()) {
			if (level.type.equalsIgnoreCase(string)) {
				return level;
			}
		}

		return null;
	}

	public boolean isLessThan(final Level level) {
		return level.isHigherThan(this);
	}

	public boolean isHigherThan(final Level level) {
		// Use an example as a reference to test against
		return Math.signum(compareTo(level)) == Math.signum(MAJOR.compareTo(GOOD));
	}
}
