package com.grt_team.wakeup.dialog;

import com.grt_team.wakeup.R;

public class FullScreenAlarmDialog extends AlarmDialog {

	@Override
	protected int getLayoutId() {
		return R.layout.alarm_alert_fullscreen_dialog;
	}

	@Override
	public void onBackPressed() {
		return;
	}
}
