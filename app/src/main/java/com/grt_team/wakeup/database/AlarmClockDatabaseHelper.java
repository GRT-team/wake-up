package com.grt_team.wakeup.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AlarmClockDatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "AlarmClockDatabaseHelper";

	private static final String DATABASE_NAME = "alarms.db";
	private static final int DATABASE_VERSION = 2;

	private static AlarmClockDatabaseHelper mHelper;

	private AlarmClockDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static AlarmClockDatabaseHelper getHelper(Context context) {
		if (mHelper == null) {
			mHelper = new AlarmClockDatabaseHelper(context);
		}
		return mHelper;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(AlarmClockTable.getCreateSchema());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL(AlarmClockTable.getDropSchema());
		onCreate(db);
	}

}