
package com.grt_team.wakeup.entity.puzzle;

import android.content.Context;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;
import com.grt_team.wakeup.WakeUpActivity;
import com.grt_team.wakeup.dialog.AlarmDialog;
import com.grt_team.wakeup.entity.puzzle.Puzzle.PuzzleResultCallback;
import com.grt_team.wakeup.services.AudioService;
import com.grt_team.wakeup.utils.AlarmHelper;

public class PuzzleActivity extends FragmentActivity implements PuzzleResultCallback {

    public static final String EXTRA_PUZZLE_NAME = "puzzleName";
    public static final String EXTRA_PREVIEW_MODE = "puzzlePreviewMode";
    public static final String EXTRA_FINISHED = "puzzleFinished";
    public static final String EXTRA_RESTORE_PUZZLE_DATA = "restorePuzzleData";
    public static final String EXTRA_PUZZLE_START_TIME = "puzzleStartTime";
    public static final String PUZZLE_DATA = "puzzleData";

    public static final int PUZZLE_END_DELAY = 1500; // milliseconds

    private String puzzleName;
    private Puzzle puzzle;
    private boolean preview;

    private View puzzleView;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        SettingsActivity.initSharedPreferences(this);

        Bundle extras = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        puzzleName = extras.getString(EXTRA_PUZZLE_NAME);
        preview = extras.getBoolean(EXTRA_PREVIEW_MODE, false);
        puzzle = PuzzleHelper.getPuzzleNewInstace(puzzleName, this);
        puzzle.setPreview(preview);
        puzzle.setCallback(this);

        if (!preview) {
            if (!AudioService.isBusy()) {
                Intent i = new Intent(this, WakeUpActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            } else {
                if (AudioService.isPuzzleFinished()) {
                    showDialog(this, getIntent());
                    finish();
                }
            }
        }

        if (savedInstanceState != null) {
            Bundle data = savedInstanceState.getBundle(PUZZLE_DATA);
            if (data != null) {
                puzzle.onRestore(data);
            }
        } else {
            Bundle data = getIntent().getBundleExtra(EXTRA_RESTORE_PUZZLE_DATA);
            if (data != null) {
                puzzle.onRestore(data);
                getIntent().removeExtra(EXTRA_RESTORE_PUZZLE_DATA);
            }
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        puzzleView = puzzle.runPuzzle(metrics);

        setContentView(puzzleView);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void savePuzzleToBundle(Bundle bundle) {
        puzzle.onSave(bundle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_PUZZLE_NAME, puzzleName);
        outState.putBoolean(EXTRA_PREVIEW_MODE, preview);

        if (puzzle != null) {
            Bundle data = new Bundle();
            savePuzzleToBundle(data);
            outState.putBundle(PUZZLE_DATA, data);
        }

        super.onSaveInstanceState(outState);
    }

    public static void showDialog(Context context, Intent activityIntent) {
        boolean isFullScreen = activityIntent
                .getBooleanExtra(AlarmDialog.FULL_SCREEN_DIALOG, false);
        AlarmHelper.showAlarmDialog(context, activityIntent.getData(), true, isFullScreen);
    }

    @Override
    public void onFinish() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(PUZZLE_END_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!preview) {
                    if (!AudioService.isPuzzleFinished()) {
                        AlarmHelper.updateSoundServiceNotification(PuzzleActivity.this, getIntent()
                                .getData());
                    }
                    showDialog(PuzzleActivity.this, getIntent());
                }
                finish();
            }
        }).start();
        Toast.makeText(this, R.string.puzzle_finish_hint, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("NewApi")
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(this, R.style.Dialog);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(getString(R.string.puzzle_close_dialog_title))
                .setMessage(getString(R.string.puzzle_close_dialog_msg))
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int w) {
                                finish();
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .show();
    }

}
