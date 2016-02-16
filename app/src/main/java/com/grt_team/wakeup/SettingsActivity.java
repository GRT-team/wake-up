
package com.grt_team.wakeup;

import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.grt_team.wakeup.R;
import com.grt_team.wakeup.fragment.SettingsFragment;

public class SettingsActivity extends SherlockPreferenceActivity {

    private static final String PACKAGE = "com.grt_team.wakeup";

    public static String ACTION_PREFS_ALARM = PACKAGE.concat(".PREFS_ALARM");
    public static String ACTION_PREFS_PUZZLE = PACKAGE.concat(".PREFS_PUZZLE");
    public static String ACTION_PREFS_PUZZLE_MAZE = PACKAGE.concat(".PREFS_PUZZLE_MAZE");
    public static String ACTION_PREFS_PUZZLE_MOSAIC = PACKAGE.concat(".PREFS_PUZZLE_MOSAIC");
    public static String ACTION_PREFS_PUZZLE_CARDS = PACKAGE.concat(".PREFS_PUZZLE_CARDS");

    /**
     * Indicate which header should be highlighted. Id must be long.
     */
    public static String EXTRA_HEADER_ID = "EXTRA_HEADER_ID";
    public static String EXTRA_FORCE_RECREATE_TASK = "EXTRA_FORCE_RECREATE_TASK";

    private HeaderHighlight headerHighlight;
    private PreferencesOnActivityResult preferencesOnActivityResult;
    private PreferencesOnStopListener preferencesOnStopListener;

    private boolean isOnePaneMode = true;

    public static final class PREF_PUZZLE {
        public static final String MAZE_SIZE = "pref_puzzle_maze_size";
        public static final String MOSAIC_SIZE = "pref_puzzle_mosaic_size";
        public static final String MOSAIC_IMAGE = "pref_puzzle_mosaic_image";
        public static final String MOSAIC_GRID = "pref_puzzle_mosaic_grid";
        public static final String MOSAIC_BG = "pref_puzzle_mosaic_bg";
        public static final String MOSAIC_USE_CUSTOM_IMG = "pref_puzzle_mosaic_use_custom_img";
        public static final String CARDS_USE_POKER_COMB = "pref_puzzle_cards_poker_comb";
        public static final String CARDS_TASK_NUMBER = "pref_puzzle_cards_task_number";
        public static final String CARDS_SHUFFLE = "pref_puzzle_cards_shuffle";
        public static final String CARDS_DECK_SIZE = "pref_puzzle_cards_deck_size";
    }

    public static final class PREF_ALARM {
        public static final String SNOOZE = "pref_alarm_snooz_time";
        public static final String RESCUE_TIME = "pref_rescue_time";
        public static final String AUTO_TURN_OFF_TIME = "pref_auto_turn_off_time";
    }

    public static final int REQUEST_CODE_PICK_IMAGE = 1;

    public static interface PreferencesOnActivityResult {
        public void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    public static interface PreferencesOnStopListener {
        public void onStop();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class HeaderHighlight {
        private List<Header> target;

        private int findHeaderPostionById(long id) {
            for (int i = 0; i < target.size(); i++) {
                if (id == target.get(i).id) {
                    return i;
                }
            }
            return -1;
        }

        public void highlight(Intent i) {

            if (i != null && target != null) {
                long headerId = i.getLongExtra(EXTRA_HEADER_ID, -1);
                int position = findHeaderPostionById(headerId);
                if (position != -1) {
                    getListView().setItemChecked(position, true);
                }
            }
        }

        public void setTarget(List<Header> target) {
            this.target = target;
        }
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        getIntent().putExtra(EXTRA_SHOW_FRAGMENT, "com.grt_team.wakeup.fragment.SettingsFragment");
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // If there is no any action then the headers need to be shown. So
        // remove the exiting show fragment value.
        if (null == getIntent().getAction()) {
            getIntent().removeExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT);
        }
        super.onCreate(savedInstanceState);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int resId = getPreferencesTitle(getIntent().getAction());
        if (resId != 0) {
            getSupportActionBar().setTitle(resId);
        }

        // Only for SDK level lower then HONEYCOMB (API level 11)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            Intent intent = getIntent();
            String action = null;

            if (intent != null) {
                action = intent.getAction();
            }

            if (action == null) {
                addPreferencesFromResource(R.xml.preferences_headers_legacy);
            } else if (ACTION_PREFS_ALARM.equals(action)) {
                addPreferencesFromResource(R.xml.preferences_alarm);
            } else if (ACTION_PREFS_PUZZLE.equals(action)) {
                addPreferencesFromResource(R.xml.preferences_puzzle);
            } else if (ACTION_PREFS_PUZZLE_MAZE.equals(action)) {
                addPreferencesFromResource(R.xml.preferences_puzzle_maze);
            } else if (ACTION_PREFS_PUZZLE_MOSAIC.equals(action)) {
                addPreferencesFromResource(R.xml.preferences_puzzle_mosaic);
            } else if (ACTION_PREFS_PUZZLE_CARDS.equals(action)) {
                addPreferencesFromResource(R.xml.preferences_puzzle_cards);
            }
        } else {
            if (!getResources().getBoolean(R.bool.has_two_panes)) {
                getActionBar().setDisplayHomeAsUpEnabled(true);
            }
            if (headerHighlight != null) {
                headerHighlight.highlight(getIntent());
            }
        }
    }

    public int getPreferencesTitle(String action) {
        if (ACTION_PREFS_ALARM.equals(action)) {
            return R.string.pref_header_alarm_title;
        } else if (ACTION_PREFS_PUZZLE.equals(action)) {
            return R.string.pref_header_puzzle_title;
        } else if (ACTION_PREFS_PUZZLE_MAZE.equals(action)) {
            return R.string.puzzle_maze_title;
        } else if (ACTION_PREFS_PUZZLE_MOSAIC.equals(action)) {
            return R.string.puzzle_mosaic_title;
        } else if (ACTION_PREFS_PUZZLE_CARDS.equals(action)) {
            return R.string.puzzle_card_title;
        }
        return 0;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(List<Header> target) {
        isOnePaneMode = (onIsHidingHeaders() || !onIsMultiPane());

        loadHeadersFromResource(R.xml.preferences_headers, target);
        headerHighlight = new HeaderHighlight();
        headerHighlight.setTarget(target);
    }

    public static void initSharedPreferences(Context context) {
        boolean readAgain;

        // Check if this is first access to shared preferences.
        final SharedPreferences defaultValueSp = context.getSharedPreferences(
                PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES,
                Context.MODE_PRIVATE);
        readAgain = !defaultValueSp.getBoolean(
                PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false);

        // Read all preferences that might be required
        PreferenceManager.setDefaultValues(context, R.xml.preferences_alarm, readAgain);
        PreferenceManager.setDefaultValues(context, R.xml.preferences_puzzle, readAgain);
        PreferenceManager.setDefaultValues(context, R.xml.preferences_puzzle_maze, readAgain);
        PreferenceManager.setDefaultValues(context, R.xml.preferences_puzzle_mosaic, readAgain);
        PreferenceManager.setDefaultValues(context, R.xml.preferences_puzzle_cards, readAgain);
    }

    public static SharedPreferences getPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            boolean forceRecreate = false;
            String action = getIntent().getAction();
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                forceRecreate = extras.getBoolean(EXTRA_FORCE_RECREATE_TASK, false);
            }

            // Perform UP navigation
            if ((action == null) || (!isOnePaneMode)) {
                // In this case we need to navigate to main window
                NavUtils.navigateUpTo(this, new Intent(this, WakeUpActivity.class));
            } else {
                if (!forceRecreate || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    NavUtils.navigateUpTo(this, buildOnePaneUpIntent(action));
                } else {
                    TaskStackBuilder builder = TaskStackBuilder
                            .create(this)
                            .addNextIntent(new Intent(this, WakeUpActivity.class))
                            .addNextIntent(new Intent("action", null, this, SettingsActivity.class));

                    if (ACTION_PREFS_PUZZLE_MAZE.equals(action)
                            || ACTION_PREFS_PUZZLE_MOSAIC.equals(action)
                            || ACTION_PREFS_PUZZLE_CARDS.equals(action)) {
                        builder.addNextIntent(buildOnePaneUpIntent(action));
                    }
                    builder.startActivities();
                    finish();
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Build appropriate upIntent for navigation.
     * 
     * @param currentAction
     * @return
     */
    private Intent buildOnePaneUpIntent(String currentAction) {
        Bundle fragmentArgs = new Bundle();
        Intent i = new Intent(this, SettingsActivity.class);

        if (ACTION_PREFS_ALARM.equals(currentAction)
                || ACTION_PREFS_PUZZLE.equals(currentAction)) {
            i.setAction(null);
        } else if (ACTION_PREFS_PUZZLE_CARDS.equals(currentAction)
                || ACTION_PREFS_PUZZLE_MAZE.equals(currentAction)
                || ACTION_PREFS_PUZZLE_MOSAIC.equals(currentAction)) {
            i.setAction(ACTION_PREFS_PUZZLE);
            i.putExtra(EXTRA_SHOW_FRAGMENT, "com.grt_team.wakeup.fragment.SettingsFragment");
            fragmentArgs.putString(SettingsFragment.EXTRA_SETTINGS,
                    SettingsFragment.PREFERENCE_PUZZLE);
        }

        if (fragmentArgs.size() > 0) {
            i.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, fragmentArgs);
        }
        return i;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (preferencesOnActivityResult != null) {
            preferencesOnActivityResult.onActivityResult(requestCode,
                    resultCode, data);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (preferencesOnStopListener != null) {
            preferencesOnStopListener.onStop();
        }
    }

    public PreferencesOnActivityResult getPreferencesOnActivityResult() {
        return preferencesOnActivityResult;
    }

    public void setPreferencesOnActivityResult(
            PreferencesOnActivityResult preferencesOnActivityResult) {
        this.preferencesOnActivityResult = preferencesOnActivityResult;
    }

    public PreferencesOnStopListener getPreferencesOnStopListener() {
        return preferencesOnStopListener;
    }

    public void setPreferencesOnStopListener(PreferencesOnStopListener preferencesOnStopListener) {
        this.preferencesOnStopListener = preferencesOnStopListener;
    }

}
