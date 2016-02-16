package com.grt_team.wakeup.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil {
    
    private static final String PREF_NAME_SNOOZE = "alarm_snooze_time";
    private static final String PREF_NAME_FEEDBACK = "pref_next_feedback";
    
    private static final String ALARM_CLOCK_TIME_PREFIX = "alarm_clock_time_";
    private static final String FEEDBACK_NEXT_TIME = "feedback_next_time";
    
    private SharedPreferenceUtil() {}
    
    private static SharedPreferences getSharedPreferences(Context context, String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }
    
    private static String getClockSnoozeTimeKey(long clockId) {
        return ALARM_CLOCK_TIME_PREFIX.concat(Long.toString(clockId));
    }
    
    public static void saveAlarmClockSnoozeTime(Context context, long time, long clockId) {
        SharedPreferences preferences = getSharedPreferences(context, PREF_NAME_SNOOZE);
        preferences.edit().putLong(getClockSnoozeTimeKey(clockId), time).commit();
    }
    
    public static long getAlarmClockSnoozeTime(Context context, long clockId) {
        SharedPreferences preferences = getSharedPreferences(context, PREF_NAME_SNOOZE);
        return preferences.getLong(getClockSnoozeTimeKey(clockId), 0);
    }
    
    public static void removeAlarmClockSnoozeTime(Context context, long clockId) {
        SharedPreferences preferences = getSharedPreferences(context, PREF_NAME_SNOOZE);
        preferences.edit().remove(getClockSnoozeTimeKey(clockId)).commit();
    }
    
    public static void saveNextFeedbackTime(Context context, long time) {
        SharedPreferences preferences = getSharedPreferences(context, PREF_NAME_FEEDBACK);
        preferences.edit().putLong(FEEDBACK_NEXT_TIME, time).commit();
    }
    
    public static long getNextFeedbackTime(Context context) {
        SharedPreferences preferences = getSharedPreferences(context, PREF_NAME_FEEDBACK);
        return preferences.getLong(FEEDBACK_NEXT_TIME, 0);
    }

}
