package com.grt_team.wakeup.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.grt_team.wakeup.utils.AlarmHelper;

public class InitReceiver extends BroadcastReceiver {

	private static String TAG = "InitReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			AlarmHelper.scheduleAllAlarms(context);
		} else {
			Log.w(TAG, "Can't handle intent action: " + intent.getAction());
		}
	}

}