package com.grt_team.wakeup.dialog;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.WindowManager;

import com.grt_team.wakeup.R;

public class FullScreenAlarmDialog extends AlarmDialog {

	@Override
	public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.alarm_alert_fullscreen_dialog;
	}

	@Override
	public void onBackPressed() {
		return;
	}
}
