
package com.grt_team.wakeup.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsFragment extends WakeUpPreferenceFragment {
    public static final String EXTRA_SETTINGS = "settings";
    public static final String PREFERENCE_ALARM = "preference_alarm";
    public static final String PREFERENCE_PUZZLE = "preference_puzzle";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String settings = getArguments().getString(EXTRA_SETTINGS);
        if (PREFERENCE_ALARM.equals(settings)) {
            addPreferencesFromResource(R.xml.preferences_alarm);
        } else if (PREFERENCE_PUZZLE.equals(settings)) {
            addPreferencesFromResource(R.xml.preferences_puzzle);
        }
    }

    @Override
    public String getPreferenceAction() {
        String settings = getArguments().getString(EXTRA_SETTINGS);
        if (PREFERENCE_ALARM.equals(settings)) {
            return SettingsActivity.ACTION_PREFS_ALARM;
        } else if (PREFERENCE_PUZZLE.equals(settings)) {
            return SettingsActivity.ACTION_PREFS_PUZZLE;
        }
        return null;
    }

    public static class MazeSettingsFragment extends WakeUpPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_puzzle_maze);
        }

        @Override
        public String getPreferenceAction() {
            return SettingsActivity.ACTION_PREFS_PUZZLE_MAZE;
        }

    }

    public static class MosaicSettingsFragment extends WakeUpPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_puzzle_mosaic);
        }

        @Override
        public String getPreferenceAction() {
            return SettingsActivity.ACTION_PREFS_PUZZLE_MOSAIC;
        }

    }

    public static class CardsSettingsFragment extends WakeUpPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_puzzle_cards);
        }

        @Override
        public String getPreferenceAction() {
            return SettingsActivity.ACTION_PREFS_PUZZLE_CARDS;
        }
    }

    public static class CatcherSettingsFragment extends WakeUpPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_puzzle_catcher);
        }

        @Override
        public String getPreferenceAction() {
            return SettingsActivity.ACTION_PREFS_PUZZLE_CATCHER;
        }
    }

}
