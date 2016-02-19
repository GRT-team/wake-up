
package com.grt_team.wakeup.fragment;

import java.util.Calendar;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.grt_team.wakeup.R;
import com.grt_team.wakeup.database.AlarmClockDatasource;
import com.grt_team.wakeup.database.AlarmClockTable;
import com.grt_team.wakeup.entity.puzzle.PuzzleHelper;
import com.grt_team.wakeup.utils.AlarmHelper;
import com.grt_team.wakeup.utils.AlarmHelper.OnClockDeleteListener;
import com.grt_team.wakeup.utils.DayOfWeekHelper;
import com.grt_team.wakeup.utils.SoundHelper;

public class AlarmClockListFragment extends SherlockListFragment implements
        OnItemClickListener, OnClockDeleteListener, SimpleCursorAdapter.ViewBinder {

    public final static String CLOCK_ID = "CLOCK_ID";
    public final static String SELECTED_CLOCK_POS = "SELECTED_CLOCK_POS";

    AlarmClockDatasource data;
    SimpleCursorAdapter adapter;

    OnClockSelectedListener clockSelectedListener;

    int selectedClockPos = 0;

    private String[] columns = new String[] {
            AlarmClockTable.ENABLED,
            AlarmClockTable.HOUR,
            AlarmClockTable.MINUTES,
            AlarmClockTable.DAY_OF_WEEK,
            AlarmClockTable.PUZZLE_NAME,
            AlarmClockTable.SOUND
    };

    private int[] ids = new int[] {
            R.id.alarm_enable,
            R.id.alarm_time,
            R.id.alarm_time,
            R.id.alarm_day,
            R.id.alarm_puzzle,
            R.id.alarm_sound
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        data = new AlarmClockDatasource(this.getActivity());

        adapter = new SimpleCursorAdapter(this.getActivity(),
                R.layout.alarm_clock_list_item, null, columns, ids, 0);
        adapter.setViewBinder(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(R.string.alarm_clock_empty_mgs));
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
        getListView().setOnCreateContextMenuListener(this);

        if (getResources().getBoolean(R.bool.has_two_panes)) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            getListView().setBackgroundColor(
                    getResources().getColor(android.R.color.background_light));
        } else {
            int padding = (int) getResources().getDimension(R.dimen.list_padding);
            getListView().setPadding(padding, 0, padding, 0);
            getActivity().findViewById(android.R.id.content).findViewById(R.id.alarm_clock_header)
                    .setPadding(padding, padding, padding, padding / 2);
        }

        if (null != savedInstanceState)
            selectedClockPos = savedInstanceState.getInt(SELECTED_CLOCK_POS);
    }

    @Override
    public void onPause() {
        super.onPause();
        data.close();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.changeCursor(data.getAlarms());
        getListView().setItemChecked(selectedClockPos, true);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,
            int position, long id) {
        selectedClockPos = position;
        if (null != clockSelectedListener) {
            clockSelectedListener.onClockSelected(id);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.clock_contextual_menu, menu);
        menu.setHeaderIcon(android.R.drawable.ic_menu_recent_history);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(((TextView) info.targetView
                .findViewById(R.id.alarm_time)).getText());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
            case R.id.clock_menu_delete:
                AlarmHelper.deleteAlarmClock(getActivity(), info.id, this);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        TextView text;
        String columnName = cursor.getColumnName(columnIndex);
        boolean enabled = cursor.getInt(cursor.getColumnIndex(AlarmClockTable.ENABLED)) != 0;
        boolean istTwoPane = getResources().getBoolean(R.bool.has_two_panes);
        if (AlarmClockTable.ENABLED.equals(columnName)) {
            CompoundButton btn = (CompoundButton) view;
            if (istTwoPane) {
                btn.setVisibility(View.GONE);
            } else {
                btn.setOnCheckedChangeListener(null); // DON'T REMOVE
                btn.setChecked(enabled);
                final long id = cursor.getLong(cursor
                        .getColumnIndex(AlarmClockTable._ID));
                btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        updateClockState(id, isChecked);
                    }
                });
            }
        } else if (AlarmClockTable.HOUR.equals(columnName)) {
            text = (TextView) view;
            int colMin = cursor.getColumnIndex(AlarmClockTable.MINUTES);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, cursor.getInt(columnIndex));
            cal.set(Calendar.MINUTE, cursor.getInt(colMin));
            if (istTwoPane) {
                text.setTextColor(enabled ? Color.BLACK : Color.LTGRAY);
            }
            text.setText(DateFormat.getTimeFormat(getActivity()).format(cal.getTime()));
        } else if (AlarmClockTable.DAY_OF_WEEK.equals(columnName)) {
            text = (TextView) view;
            columnIndex = cursor.getColumnIndex(AlarmClockTable.DAY_OF_WEEK);
            String title = DayOfWeekHelper.toString(getActivity(),
                    cursor.getInt(columnIndex), true);
            if (istTwoPane) {
                text.setTextColor(enabled ? Color.BLACK : Color.LTGRAY);
            }
            text.setText(title);
        } else if (AlarmClockTable.PUZZLE_NAME.equals(columnName)) {
            text = (TextView) view;
            columnIndex = cursor.getColumnIndex(AlarmClockTable.PUZZLE_NAME);
            if (istTwoPane) {
                text.setTextColor(enabled ? Color.BLACK : Color.LTGRAY);
            }
            text.setText(PuzzleHelper.getPuzzleTitle(cursor.getString(columnIndex), getActivity()));
        } else if (AlarmClockTable.SOUND.equals(columnName)) {
            ImageView imageView = (ImageView) view;
            String soundName = SoundHelper.getFileName(cursor.getString(columnIndex), getActivity());
            if (getResources().getString(R.string.music_silent).equals(soundName)) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
        return true;
    }

    public void newAlarmClock() {
        long id = data.newAlarmClock();
        AlarmHelper.scheduleNextAlarmById(getActivity(), id, true);
        if (getResources().getBoolean(R.bool.has_two_panes)) {
            notifyDataSetChanged();
            selectedClockPos = getListView().getCount() - 1;
            getListView().setItemChecked(selectedClockPos, true);
            getListView().setSelection(selectedClockPos);
        }
        if (null != clockSelectedListener)
            clockSelectedListener.onClockSelected(id);
    }

    /**
     * Notify that data is changed and UI should be updated. Need to be called
     * when other fragment in two pane mode changed data and require update.
     */
    public void notifyDataSetChanged() {
        adapter.changeCursor(data.getAlarms());
        AlarmHelper.scheduleAllAlarms(getActivity());
    }

    /**
     * Interface to implement listener pattern to listen when clock is selected
     * in two pane mode
     */
    public interface OnClockSelectedListener {
        public void onClockSelected(long clockId);
    }

    /**
     * Set listener for event when clock is selected in two pane mode.
     * 
     * @param listener
     */
    public void setOnClockSelectedListener(OnClockSelectedListener listener) {
        clockSelectedListener = listener;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_CLOCK_POS, selectedClockPos);
        super.onSaveInstanceState(outState);
    }

    /**
     * Enable or disable clock by specified id
     * 
     * @param id - the clock id
     * @param enabled - enable or disable clock
     */
    private void updateClockState(long id, boolean enabled) {
        data.enableAlarm(id, enabled);
        notifyDataSetChanged();
        if (getResources().getBoolean(R.bool.has_two_panes) && clockSelectedListener != null) {
            clockSelectedListener.onClockSelected(getListView().getItemIdAtPosition(
                    selectedClockPos));
        }
        AlarmHelper.scheduleNextAlarmById(getActivity(), id, true);
        AlarmHelper.showNextAlarmTime(getActivity(), id);
    }

    @Override
    public void onClockDeleted() {
        notifyDataSetChanged();
        if (getResources().getBoolean(R.bool.has_two_panes)) {
            getListView().setItemChecked(selectedClockPos, true);
            getListView().setSelection(selectedClockPos);
            if (null != clockSelectedListener)
                clockSelectedListener.onClockSelected(getListView()
                        .getItemIdAtPosition(selectedClockPos));
        }
    }
}
