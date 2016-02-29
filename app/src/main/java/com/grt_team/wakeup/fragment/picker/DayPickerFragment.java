package com.grt_team.wakeup.fragment.picker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.grt_team.wakeup.fragment.AlarmClockSettingListFragment;
import com.grt_team.wakeup.utils.DayOfWeekHelper;
import com.grt_team.wakeup.utils.DayOfWeekHelper.OnDayOfWeekChanged;

public class DayPickerFragment extends DialogFragment {
	
	public final static String ARG_DAYS = "ARG_DAYS";
	
	OnDayOfWeekChanged listener;
	AlertDialog dayPicker;
	int days;
	
	public void setOnDayOfWeekChangedListener(OnDayOfWeekChanged listener) {
		this.listener = listener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (null == listener)
			this.listener = (AlarmClockSettingListFragment) getFragmentManager()
					.findFragmentById(
							getArguments().getInt(
									AlarmClockSettingListFragment.FRAGMENT_ID));
		if(null != savedInstanceState) {
		    days = savedInstanceState.getInt(ARG_DAYS);
		} else {
		    days = getArguments().getInt(ARG_DAYS);
		}
		dayPicker = DayOfWeekHelper.getDayOfWeekPicker(getActivity(), days, listener);
		return dayPicker;
	}
	
	@Override
	public void onSaveInstanceState(Bundle arg0) {
	    arg0.putInt(ARG_DAYS, (Integer) dayPicker.getListView().getTag());
	    super.onSaveInstanceState(arg0);
	}

}
