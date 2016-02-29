
package com.grt_team.wakeup;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.grt_team.wakeup.fragment.AlarmClockListFragment;
import com.grt_team.wakeup.fragment.AlarmClockListFragment.OnClockSelectedListener;
import com.grt_team.wakeup.fragment.AlarmClockSettingListFragment;
import com.grt_team.wakeup.fragment.AlarmClockSettingListFragment.onClockSettingChangeListener;
import com.grt_team.wakeup.utils.AlarmHelper;
import com.grt_team.wakeup.utils.SharedPreferenceUtil;

import java.util.Calendar;

public class WakeUpActivity extends AppCompatActivity implements
        OnClockSelectedListener, onClockSettingChangeListener, MenuItem.OnMenuItemClickListener {

    public static final int ALARM_NOTIFICATION = 1;

    private static boolean scheduled = false;

    boolean isDualPane = false;

    private AlertDialog feedbackDialog;

    AlarmClockListFragment clockFragment;
    AlarmClockSettingListFragment clockSettingFragment;

    long clockId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsActivity.initSharedPreferences(this);
        setContentView(R.layout.wakeup);

        FragmentManager manager = getSupportFragmentManager();
        clockFragment = (AlarmClockListFragment) manager
                .findFragmentById(R.id.alarm_clock);
        clockSettingFragment = (AlarmClockSettingListFragment) manager
                .findFragmentById(R.id.alarm_clock_setting);

        isDualPane = getResources().getBoolean(R.bool.has_two_panes);

        clockFragment.setOnClockSelectedListener(this);
        if (isDualPane) {
            clockSettingFragment = (AlarmClockSettingListFragment) manager
                    .findFragmentById(R.id.alarm_clock_setting);
            clockSettingFragment.setOnClockSettetingChangeListener(this);
        }
        if (!scheduled) {
            AlarmHelper.scheduleAllAlarms(this);
            scheduled = true;
        }
        restoreSelection(savedInstanceState);

        long nextFeedbackTime = SharedPreferenceUtil.getNextFeedbackTime(this);
        if (nextFeedbackTime == 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 3); // show next dialog after 3
            // days
            SharedPreferenceUtil.saveNextFeedbackTime(getApplicationContext(),
                    calendar.getTimeInMillis());
        } else if (System.currentTimeMillis() >= SharedPreferenceUtil.getNextFeedbackTime(this)) {
            showFeedbackDialog();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (clockId == 0)
            clockId = clockFragment.getListView().getItemIdAtPosition(0);
        if (isDualPane) {
            clockSettingFragment.showSetting(clockId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.clock_menu, menu);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setOnMenuItemClickListener(this);
        }
        return true;
    }

    public void onClockSelected(long clockId) {
        this.clockId = clockId;
        if (isDualPane) {
            clockSettingFragment.showSetting(clockId);
        } else {
            Intent i = new Intent(this, AlarmClockSettingActivity.class);
            i.putExtra(AlarmClockListFragment.CLOCK_ID, clockId);
            startActivity(i);
        }
    }

    public void onClockSettingChangeed() {
        clockFragment.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(AlarmClockListFragment.CLOCK_ID, clockId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        restoreSelection(inState);
    }

    void restoreSelection(Bundle data) {
        if (null != data) {
            if (isDualPane) {
                clockId = data.getLong(AlarmClockListFragment.CLOCK_ID);
                onClockSelected(clockId);
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_clock:
                clockFragment.newAlarmClock();
                break;
            case R.id.menu_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }
        return false;
    }

    @SuppressLint("NewApi")
    private void showFeedbackDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(this, R.style.Dialog);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.feedback_dialog, null);
        final CheckBox checkBox = (CheckBox) layout.findViewById(R.id.feedback_never);
        builder.setView(layout);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(R.string.feedback_leave, new OnClickListener() {
            @SuppressWarnings("unused")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // don't show dialog again
                SharedPreferenceUtil.saveNextFeedbackTime(getApplicationContext(), Long.MAX_VALUE);
                String appUri, webUrl;
                appUri = getResources().getString(R.string.app_uri);
                webUrl = getResources().getString(R.string.web_url);
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appUri
                            + getPackageName())));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl
                            + getPackageName())));
                }
            }
        });
        builder.setNeutralButton(R.string.feedback_not_today, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long time = Long.MAX_VALUE;
                if (!checkBox.isChecked()) {
                    Calendar calendar = Calendar.getInstance();
                    // show next dialog after 3 days
                    calendar.add(Calendar.DAY_OF_YEAR, 3);
                    time = calendar.getTimeInMillis();
                }
                SharedPreferenceUtil.saveNextFeedbackTime(getApplicationContext(), time);
            }
        });
        feedbackDialog = builder.create();
        feedbackDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (feedbackDialog != null && feedbackDialog.isShowing()) {
            feedbackDialog.dismiss();
        }
    }

}
