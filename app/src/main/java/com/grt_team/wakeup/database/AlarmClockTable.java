package com.grt_team.wakeup.database;

public class AlarmClockTable {

	private AlarmClockTable() {
	}

	public static final String TABLE_NAME = "alarms";
	
	public static final String _ID = "_id";
	public static final String HOUR = "hour";
	public static final String MINUTES = "minutes";
	public static final String DAY_OF_WEEK = "dayofweek";
	public static final String PUZZLE_NAME = "puzzlename";
	public static final String SOUND = "sound";
	public static final String VIBRATE = "vibrate";
	public static final String ENABLED = "enabled";
	
	public static final String[] ALL_COLUMNS = new String[] { 
		_ID, 
		HOUR,
		MINUTES, 
		DAY_OF_WEEK, 
		PUZZLE_NAME, 
		SOUND, 
		VIBRATE, 
		ENABLED 
	};

	private static final String SCHEMA = "CREATE TABLE " + TABLE_NAME + " ("
			+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
			+ HOUR + " INTEGER," 
			+ MINUTES + " INTEGER,"
			+ DAY_OF_WEEK + " INTEGER,"
			+ PUZZLE_NAME + " TEXT,"
			+ SOUND + " TEXT, " 
			+ VIBRATE + " INTEGER," 
			+ ENABLED + " INTEGER" 
			+ ");";
	
	private static final String DROP_SCHEMA = "DROP TABLE IF EXISTS " + TABLE_NAME;

	public static String getCreateSchema() {
		return SCHEMA;
	}

	public static String getDropSchema() {
		return DROP_SCHEMA;
	}

}