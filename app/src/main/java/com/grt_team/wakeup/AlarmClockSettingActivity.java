
package com.grt_team.wakeup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.grt_team.wakeup.R;
import com.grt_team.wakeup.entity.puzzle.PuzzleHelper;
import com.grt_team.wakeup.fragment.AlarmClockListFragment;
import com.grt_team.wakeup.fragment.AlarmClockSettingListFragment;
import com.grt_team.wakeup.utils.AlarmHelper;
import com.grt_team.wakeup.utils.AlarmHelper.OnClockDeleteListener;

public class AlarmClockSettingActivity extends SherlockFragmentActivity implements OnMenuItemClickListener, OnClockDeleteListener {

    long clockId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.has_two_panes)) {
            finish();
            return;
        }
        setContentView(R.layout.alarm_clock_setting);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflater.inflate(R.layout.alarm_clock_setting_action_bar,
                null);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        bar.setCustomView(customActionBarView);
        View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
        saveMenuItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmHelper.showNextAlarmTime(getApplicationContext(), clockId);
                finish();
                return;
            }
        });

        clockId = getIntent().getLongExtra(AlarmClockListFragment.CLOCK_ID, 0);
        if (null == savedInstanceState) {
            AlarmClockSettingListFragment clockSettingFragment = new AlarmClockSettingListFragment();
            Bundle arg = new Bundle();
            arg.putLong(AlarmClockListFragment.CLOCK_ID, clockId);
            clockSettingFragment.setArguments(arg);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.alarm_clock_setting_frame, clockSettingFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.clock_setting_menu, menu);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setOnMenuItemClickListener(this);
        }
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_puzzle_settings:
                PuzzleHelper.showPuzzleSettings(this, "");
                break;
            case R.id.menu_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.menu_delete:
                AlarmHelper.deleteAlarmClock(this, clockId, this);
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        AlarmHelper.showNextAlarmTime(this, clockId);
        super.onBackPressed();
    }

    @Override
    public void onClockDeleted() {
        finish();
    }

}
