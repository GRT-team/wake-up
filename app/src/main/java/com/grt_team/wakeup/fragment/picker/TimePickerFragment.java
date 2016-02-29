package com.grt_team.wakeup.fragment.picker;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.grt_team.wakeup.fragment.AlarmClockSettingListFragment;

public class TimePickerFragment extends DialogFragment {

	public final static String ARG_TIME = "TIME";

	int hour;
	int minutes;

	OnTimeSetListener listener;

	public void setOnTimeSetListener(OnTimeSetListener listener) {
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (null == listener)
			this.listener = (AlarmClockSettingListFragment) getFragmentManager()
					.findFragmentById(
							getArguments().getInt(
									AlarmClockSettingListFragment.FRAGMENT_ID));
		int time[] = getArguments().getIntArray(ARG_TIME);

		OnTimeSetListener mListener = new OnTimeSetListener() {

			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				hour = hourOfDay;
				minutes = minute;

			}
		};

		return new WakeUpTimePickerDialog(getActivity(), mListener, time[0], time[1],
				DateFormat.is24HourFormat(getActivity()));
	}

	public class WakeUpTimePickerDialog extends TimePickerDialog {

		public WakeUpTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay,
				int minute, boolean is24HourView) {
			super(context, callBack, hourOfDay, minute, is24HourView);
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			super.onClick(dialog, which);
			if (which == BUTTON_POSITIVE) {
				listener.onTimeSet(null, hour, minutes);
			}
		}

	}

}
