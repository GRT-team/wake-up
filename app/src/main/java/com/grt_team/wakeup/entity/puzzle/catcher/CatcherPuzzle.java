package com.grt_team.wakeup.entity.puzzle.catcher;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;
import com.grt_team.wakeup.entity.puzzle.Puzzle;

/**
 * Created by oleh on 2/16/16.
 */
public class CatcherPuzzle extends Puzzle implements CatcherView.OnAllApplesCaughtListener {
    private static final String CATCHED_KEY = "catched";

    private int toCatch;
    private int speed;
    private int bucketSize;
    private Integer catched;

    CatcherView view;

    public CatcherPuzzle() {
        this.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void initDefaults(Context context) {
        super.initDefaults(context);

        toCatch = SettingsActivity.getPref(getContext()).getInt(
                SettingsActivity.PREF_PUZZLE.CATCHER_TO_CATCH, 5);
        speed = SettingsActivity.getPref(getContext()).getInt(
                SettingsActivity.PREF_PUZZLE.CATCHER_SPEED, 1) + 3;
        bucketSize = Integer.parseInt(SettingsActivity.getPref(getContext()).getString(
                SettingsActivity.PREF_PUZZLE.CATCHER_BUCKET_SIZE, "3"));
    }

    @Override
    public View onPuzzleRun(DisplayMetrics metrics) {
        view = new CatcherView(getContext());

//        view.setOnCatcherCompletedListener(this);
        view.setOnUserActionPerformedListener(this);
        view.setToCatch(toCatch);
        view.setSpeed(speed);
        view.setBucketSize(bucketSize);
        view.setListener(this);
        if (catched != null) {
            view.setCaught(catched);
        } else {
            catched = view.getCaught();
        }
        view.initBitmaps();

        return inflateRescueButton(view);
    }

    @Override
    public void onSave(Bundle savedInstanceState) {
        catched = view.getCaught();
        savedInstanceState.putInt(CATCHED_KEY, catched);
    }

    @Override
    public void onRestore(Bundle savedInstanceState) {
        catched = savedInstanceState.getInt(CATCHED_KEY);
        if (view != null) {                 // when alarm is canceled view is null
            view.setCaught(catched);
        }
    }

    @Override
    public int getPuzzleResTitle() {
        return R.string.puzzle_catcher_title;
    }

    @Override
    public int getPuzzleResBigDescription() {
        return R.string.puzzle_catcher_aim;
    }

    @Override
    public void onAllApplesCaught() {
        endPuzzle();
    }
}
