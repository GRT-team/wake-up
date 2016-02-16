
package com.grt_team.wakeup.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.grt_team.wakeup.R;

/**
 * Wrapper for standard PreferenceFragment. It enable two home button as up and
 * also provide mechanism to update Intent action in the parent activity. This
 * action is used during up button navigation.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class WakeUpPreferenceFragment extends PreferenceFragment {
    private String previousAction;
    private String preferenceAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getResources().getBoolean(R.bool.has_two_panes)) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        previousAction = activity.getIntent().getAction();
        preferenceAction = getPreferenceAction();
        activity.getIntent().setAction(preferenceAction);
    }

    @Override
    public void onDetach() {
        String currentAction = getActivity().getIntent().getAction();
        if (null != currentAction && currentAction.equals(preferenceAction)) {
            getActivity().getIntent().setAction(previousAction);
        }
        super.onDetach();
    }

    public abstract String getPreferenceAction();

}
