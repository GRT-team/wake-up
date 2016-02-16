
package com.grt_team.wakeup.entity.puzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;
import com.grt_team.wakeup.services.AudioService;
import com.grt_team.wakeup.utils.DisplayHelper;
import com.grt_team.wakeup.utils.ToastHelper;

public class PuzzleRescueActivity extends Activity {

    private boolean preview;
    private final static String AD_ID = "a1511e4beeb59fa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.puzzle_rescue_activity);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            preview = extras.getBoolean(PuzzleActivity.EXTRA_PREVIEW_MODE, false);
        }

        String msg = getResources().getString(R.string.rescue_activity_msg);
        String stopBtn = getResources().getString(R.string.rescue_stop);
        String backBtn = getResources().getString(R.string.rescue_back);
        ((TextView) findViewById(R.id.rescue_msg)).setText(String.format(msg, stopBtn, backBtn));

        findViewById(R.id.rescue_btn_close).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                restorePuzzle();
                finish();
            }
        });
        findViewById(R.id.rescue_btn_disable).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String message = null;
                if (preview || !AudioService.isBusy()) {
                    message = getResources().getString(R.string.rescue_toast_already_stopped);
                } else {
                    int secondsRemain = 0;
                    int rescueSettingsTimeSeconds = Integer.valueOf(SettingsActivity.getPref(
                            PuzzleRescueActivity.this).getString(
                            SettingsActivity.PREF_ALARM.RESCUE_TIME, "-1"));

                    if (rescueSettingsTimeSeconds > 0) {
                        long startTime = getIntent().getLongExtra(
                                PuzzleActivity.EXTRA_PUZZLE_START_TIME, 0);
                        if (startTime != 0) {
                            secondsRemain = rescueSettingsTimeSeconds
                                    - (int) (System.currentTimeMillis() - startTime) / 1000;
                        }

                        if (secondsRemain < 0) {
                            secondsRemain = 0;
                        }
                    }

                    if (secondsRemain > 0) {
                        int index = secondsRemain < 60 ? 0 : 1;
                        int min = secondsRemain / 60;
                        int seconds = secondsRemain - min * 60;
                        String[] title = getResources()
                                .getStringArray(R.array.rescue_toast_time_remain);
                        String secondsText = String.valueOf(seconds) + " "
                                + getResources().getString(
                                        (seconds != 1) ? R.string.rescue_toast_seconds
                                                : R.string.rescue_toast_second);
                        String minutesText = String.valueOf(min) + " "
                                + getResources().getString(
                                        (min != 1) ? R.string.rescue_toast_minutes
                                                : R.string.rescue_toast_minute);

                        message = String.format(title[index], minutesText, secondsText);
                    } else if (rescueSettingsTimeSeconds == -1) {
                        // Rescue button is disabled. And need to enabled in
                        // settings.
                        message = getString(R.string.rescue_toast_time_disabled);
                    } else {
                        PuzzleActivity.showDialog(PuzzleRescueActivity.this, getIntent());
                        return; // do not show toast message.
                    }
                }

                ToastHelper.showToast(PuzzleRescueActivity.this, message, Toast.LENGTH_LONG);
            }
        });

        AdView adView = new AdView(this, getAppropriateSize(), AD_ID);
        AdRequest adRequest = new AdRequest();
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.ads_goes_here);
        viewGroup.addView(adView);
        adView.loadAd(adRequest);
    }

    private AdSize getAppropriateSize() {
        AdSize adSize = AdSize.BANNER;
        int width = DisplayHelper.getScreenWidth(this);
        if (AdSize.IAB_LEADERBOARD.getWidthInPixels(this) <= width) {
            adSize = AdSize.IAB_LEADERBOARD;
        } else if (AdSize.IAB_BANNER.getWidthInPixels(this) <= width) {
            adSize = AdSize.IAB_BANNER;
        }
        return adSize;
    }

    private void restorePuzzle() {
        Intent i = new Intent(getIntent());
        i.setClass(PuzzleRescueActivity.this, (preview) ? PuzzleActivityPreview.class
                : PuzzleActivity.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        restorePuzzle();
    }
}
