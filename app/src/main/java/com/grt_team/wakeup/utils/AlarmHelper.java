
package com.grt_team.wakeup.utils;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;
import com.grt_team.wakeup.broadcast.AlarmReceiver;
import com.grt_team.wakeup.database.AlarmClockDatasource;
import com.grt_team.wakeup.database.AlarmClockTable;
import com.grt_team.wakeup.dialog.AlarmDialog;
import com.grt_team.wakeup.dialog.FullScreenAlarmDialog;
import com.grt_team.wakeup.entity.puzzle.PuzzleActivity;
import com.grt_team.wakeup.services.AudioService;

public class AlarmHelper {

    private static final String TAG = "AlarmHelper";
    private static final int SNOZE_NOTIFICATION = 11; // eleven

    public static final String CLOCK_ID = "clock_id";

    private static void runSoundService(Context context, long clockId) {
        AlarmClockDatasource data = new AlarmClockDatasource(context);
        Cursor c = data.getAlarmById(clockId);
        String sound = null;
        boolean vibrate = true;

        if (c.moveToFirst()) {
            sound = c.getString(c.getColumnIndex(AlarmClockTable.SOUND));
            vibrate = c.getInt(c.getColumnIndex(AlarmClockTable.VIBRATE)) != 0;
        } else {
            Log.e(TAG, "Data not found for clock id: " + clockId);
        }
        c.close();
        data.close();

        Intent i = new Intent(context, AudioService.class);
        i.setAction(AudioService.ACTION_START_ALARM);
        i.putExtra(CLOCK_ID, clockId);
        i.putExtra(AudioService.EXTRA_SOUND_URI, sound);
        i.putExtra(AudioService.EXTRA_VOLUME, AudioService.MAX_VOLUME);
        i.putExtra(AudioService.EXTRA_VIBRATE, vibrate);
        context.startService(i);
    }

    public static void changeSoundVolume(Context context, float volume, boolean vibrate) {
        Intent i = new Intent(context, AudioService.class);
        i.putExtra(AudioService.EXTRA_VOLUME, volume);
        i.putExtra(AudioService.EXTRA_VIBRATE, vibrate);
        context.startService(i);
    }

    private static void stopSoundService(Context context) {
        context.stopService(new Intent(context, AudioService.class));
    }

    public static void startAlarm(Context context, long clockId) {
        runSoundService(context, clockId);
        cancelAlarmSnoozeNotification(context, clockId);

        int autoTurnOffTime = Integer.valueOf(SettingsActivity.getPref(context).getString(
                SettingsActivity.PREF_ALARM.AUTO_TURN_OFF_TIME, "-1"));

        if (autoTurnOffTime != -1) {
            Calendar time = Calendar.getInstance();
            time.add(Calendar.MINUTE, autoTurnOffTime);

            scheduleAutoTurnOff(context, clockId, time);
        }
    }

    public static void scheduleAutoTurnOff(Context context, long clockId, Calendar time) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_AUTO_TURN_OFF);
        intent.setData(getDataUri(clockId));

        PendingIntent pi = PendingIntent.getBroadcast(context, (int) clockId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pi);
    }

    public static void unScheduleAutoTurnOff(Context context, long clockId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_AUTO_TURN_OFF);
        intent.setData(getDataUri(clockId));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) clockId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
    }

    public static void stopAlarm(Context context, long clockId,
            boolean scheduleNext) {
        if (scheduleNext) {
            scheduleNextAlarmById(context, clockId, false);
        }
        stopSoundService(context);
    }

    public static void snoozeAlarm(Context context, long clockId) {
        Calendar time = Calendar.getInstance();
        int snoozeTime = Integer.valueOf(SettingsActivity.getPref(context).getString(
                SettingsActivity.PREF_ALARM.SNOOZE, "0"));
        time.set(Calendar.SECOND, 0);
        time.add(Calendar.MINUTE, snoozeTime);

        // Stop playing music and schedule snooze
        scheduleAlarm(context, time, clockId);
        showAlarmNotificationIcon(context, true);
        stopAlarm(context, clockId, false);
        showAlarmSnoozeNotification(context, clockId, time);
        SharedPreferenceUtil.saveAlarmClockSnoozeTime(context, time.getTimeInMillis(), clockId);
    }

    private static void showAlarmSnoozeNotification(Context context, long clockId, Calendar calendar) {
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_DISSMISS);
        intent.setData(getDataUri(clockId));

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
        notification.setSmallIcon(android.R.drawable.ic_lock_idle_alarm);
        notification.setOngoing(true);
        notification.setWhen(0);
        notification.setContentTitle(context.getString(R.string.alarm_snooze_notification_title));
        notification.setContentText(context.getString(R.string.alarm_snooze_notification_msg)
                + DateFormat.getTimeFormat(context).format(calendar.getTime()));
        notification.setContentIntent(pi);
        manager.notify(String.valueOf(clockId), SNOZE_NOTIFICATION, notification.build());
    }

    private static void cancelAlarmSnoozeNotification(Context context, long clockId) {
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(String.valueOf(clockId), SNOZE_NOTIFICATION);
    }

    private static void scheduleNextAlarmByCursor(Context context, Cursor c,
            AlarmClockDatasource data, long clockId, boolean oneTimeAlarm) {
        boolean enabled = c.getInt(c.getColumnIndex(AlarmClockTable.ENABLED)) != 0;
        int dayOfWeek = c.getInt(c.getColumnIndex(AlarmClockTable.DAY_OF_WEEK));

        if (dayOfWeek == 0 && oneTimeAlarm) {
            // schedule one next alarm for any day
            dayOfWeek = 0x7f;
        }

        if (dayOfWeek == 0 && !oneTimeAlarm) {
            // disable current alarm in database, it was one time alarm
            enabled = false;
            data.enableAlarm(clockId, enabled);
        }

        cancelAlarmSnoozeNotification(context, clockId);

        if (enabled) {
            int hour = c.getInt(c.getColumnIndex(AlarmClockTable.HOUR));
            int minutes = c.getInt(c.getColumnIndex(AlarmClockTable.MINUTES));

            Calendar calendar = DayOfWeekHelper.getNextDayOfWeek(hour, minutes, dayOfWeek);
            scheduleAlarm(context, calendar, clockId);
        } else {
            unScheduleAlarm(context, clockId);
        }
    }

    public static boolean scheduleNextAlarmById(Context context, long clockId,
            boolean oneTimeAlarm) {
        boolean result = false;
        AlarmClockDatasource data = new AlarmClockDatasource(context);
        Cursor c = data.getAlarmById(clockId);
        if (c.moveToFirst()) {
            scheduleNextAlarmByCursor(context, c, data, clockId, oneTimeAlarm);
        }

        Cursor enabledAlarmsCursor = data.getEnabledAlarms();
        int num = enabledAlarmsCursor.getCount();
        enabledAlarmsCursor.close();

        showAlarmNotificationIcon(context, num != 0);
        c.close();
        data.close();
        return result;
    }

    public static void scheduleAlarm(Context context, Calendar time,
            long clockId) {
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM);
        intent.setData(getDataUri(clockId));

        PendingIntent pi = PendingIntent.getBroadcast(context, (int) clockId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pi);
        SharedPreferenceUtil.removeAlarmClockSnoozeTime(context, clockId);
        Log.i(TAG,
                "Next alarm will be started after: "
                        + String.valueOf((time.getTimeInMillis() - new Date()
                                .getTime()) / 1000) + " seconds. Time: "
                        + new Date(time.getTimeInMillis()).toString());
    }

    public static void unScheduleAlarm(Context context, long clockId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM);
        intent.setData(getDataUri(clockId));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                (int) clockId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
        SharedPreferenceUtil.removeAlarmClockSnoozeTime(context, clockId);
    }

    public static void scheduleAllAlarms(Context context) {
        AlarmClockDatasource data = new AlarmClockDatasource(context);
        Cursor c = data.getEnabledAlarms();
        int num = c.getCount();
        while (c.moveToNext()) {
            long clockId = c.getLong(c.getColumnIndex(AlarmClockTable._ID));
            if (SharedPreferenceUtil.getAlarmClockSnoozeTime(context, clockId) < System
                    .currentTimeMillis()) {
                scheduleNextAlarmByCursor(context, c, data, clockId, true);
            }
        }
        showAlarmNotificationIcon(context, num != 0);
        c.close();
        data.close();
    }

    public static Uri getDataUri(long clockId) {
        Uri.Builder builder = new Uri.Builder();
        builder.appendQueryParameter(CLOCK_ID, String.valueOf(clockId));
        return builder.build();
    }

    public static void showAlarmNotificationIcon(Context context, boolean show) {
        Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
        alarmChanged.putExtra("alarmSet", show);
        context.sendBroadcast(alarmChanged);
    }

    public static void updateSoundServiceNotification(Context context, Uri data) {
        Intent i = new Intent(context, AudioService.class);
        i.putExtra(AlarmHelper.CLOCK_ID,
                Long.valueOf(data.getQueryParameter(AlarmHelper.CLOCK_ID)));
        i.setAction(AudioService.ACTION_UPDATE_NOTIFICATION);
        context.startService(i);
    }

    public static Intent getDialogIntent(Context context, Uri data, boolean finished, boolean fullScreen) {
        Intent i = new Intent(context, (fullScreen) ? FullScreenAlarmDialog.class : AlarmDialog.class);
        i.setData(data);
        i.putExtra(PuzzleActivity.EXTRA_FINISHED, finished);
        return i;
    }

    public static void showAlarmDialog(Context context, Uri data,
            boolean finished, boolean fullScreen) {
        Intent i = getDialogIntent(context, data, finished, fullScreen);
        context.startActivity(i);
    }

    public static void showNextAlarmTime(Context context, long id) {
        AlarmClockDatasource clockDatasource = new AlarmClockDatasource(context);
        long time = clockDatasource.getAlarmTime(id);
        if (time == 0) {
            return;
        }
        time = (time - System.currentTimeMillis()) / 1000;

        long hours = time / (60 * 60);
        long minutes = time / 60 % 60;
        long days = hours / 24;
        hours = hours % 24;

        String daySeq = (days == 0) ? "" : (days == 1) ? context.getString(R.string.next_alarm_day)
                : context.getString(R.string.next_alarm_days);
        String minSeq = (minutes == 0) ? "" : (minutes == 1) ? context
                .getString(R.string.next_alarm_minute) : context
                .getString(R.string.next_alarm_minutes);
        String hourSeq = (hours == 0) ? "" : (hours == 1) ? context
                .getString(R.string.next_alarm_hour) : context.getString(R.string.next_alarm_hours);

        int index = (days > 0 ? 1 : 0) | (hours > 0 ? 2 : 0) | (minutes > 0 ? 4 : 0);

        String[] formats = context.getResources().getStringArray(R.array.next_alarm_set);
        String nextTime = String.format(formats[index], days, daySeq, hours, hourSeq, minutes,
                minSeq);

        ToastHelper.showToast(context, nextTime, Toast.LENGTH_LONG);
    }

    public static void deleteAlarmClock(final Context context, final long id,
            final OnClockDeleteListener listener) {
        if (AudioService.isBusy() && AudioService.getCurrentClockId() == id) {
            ToastHelper.showToast(context, context.getString(R.string.delete_alarm_busy_msg),
                    Toast.LENGTH_LONG);
        } else {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.clock_menu_delete_alarm_title))
                    .setMessage(context.getString(R.string.clock_menu_delete_alarm_message))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    AlarmClockDatasource data = new AlarmClockDatasource(context);
                                    cancelAlarmSnoozeNotification(context, id);
                                    AlarmHelper.unScheduleAlarm(context, id);
                                    data.deleteAlarm(id);
                                    data.close();
                                    AlarmHelper.scheduleAllAlarms(context);
                                    listener.onClockDeleted();
                                }
                            }).setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    public static interface OnClockDeleteListener {
        public void onClockDeleted();
    }
}
