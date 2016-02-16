package com.grt_team.wakeup.fragment.picker;

import android.app.Dialog;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.grt_team.wakeup.entity.puzzle.PuzzleHelper;
import com.grt_team.wakeup.entity.puzzle.PuzzleHelper.OnPuzzleSelectedListener;
import com.grt_team.wakeup.fragment.AlarmClockSettingListFragment;

public class PuzzlePickerFragment extends SherlockDialogFragment {
	
	public final static String ARG_PUZZLE_NAME = "ARG_PUZZLE_NAME";
	
	OnPuzzleSelectedListener listener;
	
	public void setOnPuzzleSelectedListener(OnPuzzleSelectedListener listener) {
		this.listener = listener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (null == listener)
			this.listener = (AlarmClockSettingListFragment) getFragmentManager()
					.findFragmentById(
							getArguments().getInt(
									AlarmClockSettingListFragment.FRAGMENT_ID));
		String puzzle = getArguments().getString(ARG_PUZZLE_NAME);
		return PuzzleHelper.getPuzzlePickerDialog(getActivity(), puzzle, listener);
	}

}
