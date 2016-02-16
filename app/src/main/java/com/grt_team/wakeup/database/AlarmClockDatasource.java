
package com.grt_team.wakeup.database;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.grt_team.wakeup.entity.puzzle.PuzzleHelper;
import com.grt_team.wakeup.utils.DayOfWeekHelper;
import com.grt_team.wakeup.utils.SoundHelper;

public class AlarmClockDatasource {

    AlarmClockDatabaseHelper dbHelper;

    public AlarmClockDatasource(Context context) {
        dbHelper = AlarmClockDatabaseHelper.getHelper(context);
    }

    public Cursor getAlarms() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[] {

                AlarmClockTable._ID,
                AlarmClockTable.HOUR,
                AlarmClockTable.MINUTES,
                AlarmClockTable.DAY_OF_WEEK,
                AlarmClockTable.PUZZLE_NAME,
                AlarmClockTable.ENABLED,
                AlarmClockTable.SOUND

        };

        return db.query(AlarmClockTable.TABLE_NAME, columns, null, null, null,
                null, null);
    }

    public Cursor getAlarmById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(AlarmClockTable.TABLE_NAME,
                AlarmClockTable.ALL_COLUMNS, AlarmClockTable._ID + " = " + id,
                null, null, null, null);
    }

    public String getPuzzleName(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(AlarmClockTable.TABLE_NAME,
                new String[] {
                    AlarmClockTable.PUZZLE_NAME
                },
                AlarmClockTable._ID + " = " + id, null, null, null, null);
        if (cursor.moveToFirst()) {
            String puzzleName = cursor
                    .getString(cursor.getColumnIndex(AlarmClockTable.PUZZLE_NAME));
            cursor.close();
            return puzzleName;
        } else {
            cursor.close();
            throw new IllegalStateException("Clock with id = '" + id + "' not found.");
        }
    }

    public long newAlarmClock() {
        Calendar cal = Calendar.getInstance();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues clock = new ContentValues();
        clock.put(AlarmClockTable.HOUR, cal.get(Calendar.HOUR_OF_DAY));
        clock.put(AlarmClockTable.MINUTES, cal.get(Calendar.MINUTE));
        clock.put(AlarmClockTable.DAY_OF_WEEK, 0);
        clock.put(AlarmClockTable.PUZZLE_NAME, PuzzleHelper.PUZZLE_MAZE);
        clock.put(AlarmClockTable.SOUND, SoundHelper.DEFAULT_URI);
        clock.put(AlarmClockTable.VIBRATE, false);
        clock.put(AlarmClockTable.ENABLED, true);

        return db.insert(AlarmClockTable.TABLE_NAME, null, clock);
    }

    public int updateAlarm(long id, ContentValues clock) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(AlarmClockTable.TABLE_NAME, clock, AlarmClockTable._ID
                + " = " + id, null);
    }

    public int enableAlarm(long id, boolean enabled) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AlarmClockTable.ENABLED, (enabled) ? 1 : 0);
        return db.update(AlarmClockTable.TABLE_NAME, values,
                AlarmClockTable._ID + " = " + id, null);
    }

    public int deleteAlarm(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(AlarmClockTable.TABLE_NAME, AlarmClockTable._ID
                + " = " + id, null);
    }

    public Cursor getEnabledAlarms() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(AlarmClockTable.TABLE_NAME, null, AlarmClockTable.ENABLED + " = 1", null,
                null,
                null, null);
    }

    public long getAlarmTime(long id) {
        Calendar calendar;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(AlarmClockTable.TABLE_NAME, null, AlarmClockTable._ID + " = " + id,
                null, null, null, null);
        if (!c.moveToFirst()) {
            c.close();
            return 0;
        }
        int enabled = c.getInt(c.getColumnIndex(AlarmClockTable.ENABLED));
        if (enabled == 0) {
            c.close();
            return 0;
        }
        int hour = c.getInt(c.getColumnIndex(AlarmClockTable.HOUR));
        int minute = c.getInt(c.getColumnIndex(AlarmClockTable.MINUTES));
        int day = c.getInt(c.getColumnIndex(AlarmClockTable.DAY_OF_WEEK));
        if (day == 0) {
            day = 0x7f;
        }
        calendar = DayOfWeekHelper.getNextDayOfWeek(hour, minute, day);
        c.close();
        return calendar.getTimeInMillis();
    }

    public void close() {
        dbHelper.close();
    }

}
