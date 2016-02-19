
package com.grt_team.wakeup.broadcast;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.grt_team.wakeup.dialog.AlarmDialog;
import com.grt_team.wakeup.services.AudioService;
import com.grt_team.wakeup.utils.AlarmHelper;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "AlarmReceiver";

    public static final String ACTION_ALARM = "com.grt_team.wakeup.ACTION_ALARM";
    public static final String ACTION_DISSMISS = "com.grt_team.wakeup.ACTION_DISSMISS";
    public static final String ACTION_AUTO_TURN_OFF = "com.grt_team.wakeup.ACTION_AUTO_TURN_OFF";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long clockId = Long.valueOf(intent.getData().getQueryParameter(AlarmHelper.CLOCK_ID));

        if (ACTION_ALARM.equals(action)) {

            // Alarm concurrency. Schedule next alarm by id from intent if there
            // is active alarm dialog.
            if (AudioService.isBusy()) {
                Log.i(TAG, "Another alarm is alredy showing.");
                AlarmHelper.scheduleNextAlarmById(context, clockId, false);
            } else {
                KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

                boolean fullScreen = km.inKeyguardRestrictedInputMode();
                Intent i = AlarmHelper.getDialogIntent(context, intent.getData(), false, fullScreen);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                AlarmHelper.startAlarm(context, clockId);
            }
        } else if (ACTION_DISSMISS.equals(action)) {
            AlarmHelper.stopAlarm(context, clockId, true);
        } else if (ACTION_AUTO_TURN_OFF.equals(action)) {
            AlarmHelper.snoozeAlarm(context, clockId);

            Intent i = new Intent();
            i.setAction(AlarmDialog.EXTRA_FINISH);
            LocalBroadcastManager.getInstance(context).sendBroadcast(i);
        }

    }
}
