package com.deange.githubstatus.content;

import android.content.Context;
import android.database.Cursor;

import com.deange.githubstatus.model.BaseModel;
import com.j256.ormlite.android.AndroidCompiledStatement;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.SQLException;

public final class ContentHelper {
	private static ContentHelper sInstance = null;

	private final Context mContext;
	private final DatabaseHelper mDatabaseHelper;

	public synchronized static ContentHelper getInstance(final Context context) {
		if (sInstance == null) {
			sInstance = new ContentHelper(context);
		}
		return sInstance;
	}

	public synchronized <T extends BaseModel> Dao<T, Long> getDao(final Class<T> clazz) {
		return mDatabaseHelper.getDaoEx(clazz);
	}

	public synchronized <T extends BaseModel> Cursor getCursor(final Class<T> clazz, final PreparedQuery<T> query)
			throws SQLException {
		final DatabaseConnection connection = (DatabaseConnection) mDatabaseHelper.getConnectionSource();
		final AndroidCompiledStatement stmt = (AndroidCompiledStatement) query.compile(connection, StatementType.SELECT);
		return stmt.getCursor();
	}

	public DatabaseHelper getHelper() {
		return DatabaseHelper.getInstance(mContext);
	}

	private ContentHelper(final Context context) {
		mContext = context;
		mDatabaseHelper = DatabaseHelper.getInstance(context);
	}
}