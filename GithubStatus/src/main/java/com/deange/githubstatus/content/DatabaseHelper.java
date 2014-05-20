package com.deange.githubstatus.content;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.deange.githubstatus.model.BaseModel;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	private static final String TAG = DatabaseHelper.class.getSimpleName();

	private static DatabaseHelper sInstance = null;

    public static final String DATABASE_NAME = "gsapp.db";
    public static final int DATABASE_VERSION = 1;

	private static WeakReference<Callback> mCallbackReference = new WeakReference<>(Fallback.INSTANCE);

	private final Map<Class<? extends BaseModel>, Dao<? extends BaseModel, Long>> mDaoMap = new HashMap<>();

	public interface Callback {
        public void onUpgrade(final SQLiteDatabase db, final ConnectionSource connection, final int oldVersion, final int newVersion);
	}

	public static void setDatabaseCallback(final Callback databaseCallback) {
		mCallbackReference = new WeakReference<>(databaseCallback);
	}

	public static synchronized DatabaseHelper getInstance(final Context context) {
		if (sInstance == null) {
			sInstance = new DatabaseHelper(context, DATABASE_NAME, DATABASE_VERSION);
		}
		return sInstance;
	}

	private DatabaseHelper(final Context context, final String name, final int version) {
		super(context, name, null, version);
	}

	@Override
	public void onCreate(final SQLiteDatabase db, final ConnectionSource connection) {
        Log.v(TAG, "onCreate()");

		createTables(connection);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		Log.v(TAG, "onUpgrade()");

		final Callback databaseCallback = mCallbackReference.get();
		if (databaseCallback != null) {
			databaseCallback.onUpgrade(database, connectionSource, oldVersion, newVersion);
		}
	}

	@Override
	public void close() {
		mDaoMap.clear();

		sInstance = null;
		deleteTables(connectionSource);
		createTables(connectionSource);
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseModel> Dao<T, Long> getDaoEx(final Class<T> clazz) {
		Dao<T, Long> result;
		if (mDaoMap.containsKey(clazz)) {
			result = (Dao<T, Long>) mDaoMap.get(clazz);

		} else {
			try {
				result = getDao(clazz);
				mDaoMap.put(clazz, result);

			} catch (final java.sql.SQLException e) {
				throw new SQLException(e.getMessage());
			}
		}
		return result;
	}

	public void createTables(final ConnectionSource cs) {
		for (final Class clazz : ContentType.MODELS) {
			createTable(clazz, cs);
		}
	}

	public void deleteTables(final ConnectionSource cs) {
		for (final Class clazz : ContentType.MODELS) {
			dropTable(clazz, cs);
		}
	}

	public void createTable(final Class clazz, final ConnectionSource cs) {
		try {
			TableUtils.createTable(cs, clazz);
		} catch (final java.sql.SQLException e) {
			throw new SQLException(e.getMessage());
		}
	}

	public void dropTable(final Class clazz, final ConnectionSource cs) {
		try {
			TableUtils.dropTable(cs, clazz, false);
		} catch (final java.sql.SQLException e) {
			throw new SQLException(e.getMessage());
		}
	}

	private final static class Fallback implements Callback {
		public static final Callback INSTANCE = new Fallback();

		@Override
		public void onUpgrade(final SQLiteDatabase db, final ConnectionSource connection, final int oldVersion, final int newVersion) {
			Log.w(TAG, "Fallback: onUpgrade()");
		}
	}
}
