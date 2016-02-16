package com.grt_team.wakeup.preferences;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;

public class SummaryListPreference extends ListPreference {

	public SummaryListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private void updateSummary() {
		setSummary(getEntry());
	}

	@Override
	protected void onBindView(View view) {
		updateSummary();
		super.onBindView(view);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			updateSummary();
		}
	}
}