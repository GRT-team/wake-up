
package com.grt_team.wakeup.dialog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.broadcast.AlarmReceiver;
import com.grt_team.wakeup.database.AlarmClockDatasource;
import com.grt_team.wakeup.entity.puzzle.PuzzleActivity;
import com.grt_team.wakeup.entity.puzzle.PuzzleHelper;
import com.grt_team.wakeup.utils.AlarmHelper;

public class AlarmDialog extends Activity implements OnClickListener {

    public static final String FULL_SCREEN_DIALOG = "FULL_SCREEN_DIALOG";

    public static final String EXTRA_FINISH = "extra_finish";

    private long clockId;
    private String puzzleName;
    private boolean finished;

    private Button btnDismiss;
    private Button btnSnooze;
    private Button btnPuzzle;

    private void prepareDialog(boolean allowDissmis) {
        btnDismiss.setVisibility(allowDissmis ? View.VISIBLE : View.GONE);
        findViewById(R.id.alarm_dialog_button_divider1).setVisibility(
                allowDissmis ? View.VISIBLE : View.GONE);
        btnSnooze.setVisibility(allowDissmis ? View.VISIBLE : View.GONE);
        findViewById(R.id.alarm_dialog_button_divider2).setVisibility(
                allowDissmis ? View.VISIBLE : View.GONE);
        btnPuzzle.setVisibility(allowDissmis ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            finished = extras.getBoolean(PuzzleActivity.EXTRA_FINISHED, false);

            // If received intent contains finished extra then display dialog
            // that allow dismiss.
            if (finished) {
                prepareDialog(true);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clockId = Long.valueOf(getIntent().getData().getQueryParameter(
                AlarmHelper.CLOCK_ID));
        finished = getIntent().getBooleanExtra(PuzzleActivity.EXTRA_FINISHED, false);
        puzzleName = new AlarmClockDatasource(this).getPuzzleName(clockId);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(getLayoutId());

        btnDismiss = (Button) findViewById(R.id.alarm_button_dismiss);
        btnDismiss.setOnClickListener(this);

        btnSnooze = (Button) findViewById(R.id.alarm_button_snooze);
        btnSnooze.setOnClickListener(this);

        btnPuzzle = (Button) findViewById(R.id.alarm_button_puzzle);
        btnPuzzle.setOnClickListener(this);

        if (null != savedInstanceState)
            finished = savedInstanceState.getBoolean(PuzzleActivity.EXTRA_FINISHED);
        prepareDialog(finished);

        IntentFilter filter = new IntentFilter();
        filter.addAction(EXTRA_FINISH);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    protected int getLayoutId() {
        return R.layout.alarm_alert_dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alarm_button_dismiss:
                sendBroadcast(new Intent(AlarmReceiver.ACTION_DISSMISS, getIntent().getData(),
                        this, AlarmReceiver.class));
                finish();
                break;
            case R.id.alarm_button_snooze:
                AlarmHelper.snoozeAlarm(this, clockId);
                finish();
                break;
            case R.id.alarm_button_puzzle:
                PuzzleHelper.runPuzzle(this, puzzleName, false, clockId);
                AlarmHelper.unScheduleAutoTurnOff(this, clockId);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(PuzzleActivity.EXTRA_FINISHED, finished);
        super.onSaveInstanceState(outState);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(EXTRA_FINISH)) {
                finish();
            }
        }
    };

}
