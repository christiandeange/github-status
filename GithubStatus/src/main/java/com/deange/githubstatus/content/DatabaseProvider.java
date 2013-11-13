package com.deange.githubstatus.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.deange.githubstatus.model.Status;
import com.j256.ormlite.support.ConnectionSource;

public class DatabaseProvider extends ContentProvider implements DatabaseHelper.Callback {

	@Override
	public boolean onCreate() {
		ContentHelper.getInstance(getContext());
		DatabaseHelper.getInstance(getContext());
		DatabaseHelper.setDatabaseCallback(this);

		return true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		// No upgrade use cases at the moment
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

    @Override
    public String getType(Uri uri) {
        return Status.class.getSimpleName();
    }
}