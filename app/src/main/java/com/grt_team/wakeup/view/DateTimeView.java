package com.grt_team.wakeup.view;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grt_team.wakeup.R;

public class DateTimeView extends LinearLayout {

	TextView time, day;

	private final Handler mHandler = new Handler();
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mHandler.post(new Runnable() {
				public void run() {
					updateTime();
				}
			});
		}
	};

	public DateTimeView(Context context) {
		super(context);
	}

	public DateTimeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		time = (TextView) findViewById(R.id.time);
		day = (TextView) findViewById(R.id.day);
		updateTime();
	}

	private void updateTime() {
		Date d = new Date();
		time.setText(DateFormat.getTimeFormat(getContext()).format(d));
		day.setText(DateFormat.getDateFormat(getContext()).format(d));
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		getContext().registerReceiver(mIntentReceiver, filter);

		updateTime();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getContext().unregisterReceiver(mIntentReceiver);
	}

}
