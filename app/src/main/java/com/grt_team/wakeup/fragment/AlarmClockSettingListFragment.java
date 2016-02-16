
package com.grt_team.wakeup.fragment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockListFragment;
import com.grt_team.wakeup.R;
import com.grt_team.wakeup.database.AlarmClockDatasource;
import com.grt_team.wakeup.database.AlarmClockTable;
import com.grt_team.wakeup.entity.puzzle.PuzzleHelper.OnPuzzleSelectedListener;
import com.grt_team.wakeup.fragment.adapter.AlarmClockSettingAdapter;
import com.grt_team.wakeup.fragment.picker.DayPickerFragment;
import com.grt_team.wakeup.fragment.picker.PuzzlePickerFragment;
import com.grt_team.wakeup.fragment.picker.SoundPickerFragment;
import com.grt_team.wakeup.fragment.picker.TimePickerFragment;
import com.grt_team.wakeup.fragment.picker.SoundPickerFragment.OnSoundChangeListener;
import com.grt_team.wakeup.utils.AlarmHelper;
import com.grt_team.wakeup.utils.DayOfWeekHelper.OnDayOfWeekChanged;

public class AlarmClockSettingListFragment extends SherlockListFragment implements
        OnItemClickListener, OnTimeSetListener, OnDayOfWeekChanged,
        OnPuzzleSelectedListener, OnSoundChangeListener {

    public final static String FRAGMENT_ID = "FRAGMENT_ID";

    final String TIME_PICKER = "TIME_PICKER";
    final String PUZZLE_PICKER = "PUZZLE_PICKER";
    final String DAY_PICKER = "DAY_PICKER";
    final String SOUND_PICKER = "SOUND_PICKER";

    List<ListItem> alarmSetting;
    AlarmClockSettingAdapter alarmSettingAdapter;

    onClockSettingChangeListener clockChangeListener;

    AlarmClockDatasource data;

    long clockId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmSetting = new ArrayList<ListItem>();
        alarmSettingAdapter = new AlarmClockSettingAdapter(getActivity(),
                R.layout.alarm_clock_setting_list_item, alarmSetting);
        data = new AlarmClockDatasource(getActivity());
    }

    private ListItem createItem(int id, int icon, int title, Object value) {
        ListItem item = new ListItem();
        item.setId(id);
        item.setIcon(icon);
        item.setTitle(getResources().getString(title));
        item.setValue(value);
        return item;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(alarmSettingAdapter);
        setEmptyText(getString(R.string.alarm_setting_empty_msg));
        getListView().setOnItemClickListener(this);
        if (null != getArguments()) {
            clockId = getArguments().getLong(AlarmClockListFragment.CLOCK_ID);
            showSetting(clockId);
        }
        int padding = (int) getResources().getDimension(R.dimen.list_padding);
        if (!getResources().getBoolean(R.bool.has_two_panes)) {
            getListView().setPadding(padding, 0, padding, 0);
            getActivity().findViewById(android.R.id.content)
                    .findViewById(R.id.alarm_clock_setting_header)
                    .setPadding(padding, padding, padding, padding / 2);
        }
    }

    public void showSetting(long id) {
        clockId = id;
        alarmSetting.clear();
        alarmSettingAdapter.clear();
        Cursor c = data.getAlarmById(clockId);
        ListItem item;
        if (c.moveToFirst()) {
            Object value = c.getInt(c.getColumnIndex(AlarmClockTable.ENABLED));
            item = createItem(AlarmClockSettingAdapter.TYPE_ENABLE, R.drawable.music_volume,
                    R.string.alarm_setting_enable_title, value);
            alarmSetting.add(item);
            value = new int[] {
                    c.getInt(c.getColumnIndex(AlarmClockTable.HOUR)),
                    c.getInt(c.getColumnIndex(AlarmClockTable.MINUTES))
            };
            item = createItem(AlarmClockSettingAdapter.TYPE_TIME,
                    R.drawable.ic_time, R.string.alarm_setting_time_title,
                    value);
            alarmSetting.add(item);
            value = c.getInt(c.getColumnIndex(AlarmClockTable.DAY_OF_WEEK));
            item = createItem(AlarmClockSettingAdapter.TYPE_DAY,
                    R.drawable.ic_day, R.string.alarm_setting_day_title, value);
            alarmSetting.add(item);
            value = c.getString(c.getColumnIndex(AlarmClockTable.PUZZLE_NAME));
            item = createItem(AlarmClockSettingAdapter.TYPE_PUZZLE,
                    R.drawable.ic_puzzle, R.string.alarm_setting_puzzle_title,
                    value);
            alarmSetting.add(item);
            value = c.getString(c.getColumnIndex(AlarmClockTable.SOUND));
            item = createItem(AlarmClockSettingAdapter.TYPE_SOUND,
                    R.drawable.ic_sound, R.string.alarm_setting_sound_title,
                    value);
            alarmSetting.add(item);
            if (new VibratorHelper().hasVibrator(getActivity())) {
                value = c.getInt(c.getColumnIndex(AlarmClockTable.VIBRATE));
                item = createItem(AlarmClockSettingAdapter.TYPE_VIBRATE,
                        R.drawable.ic_vibrate,
                        R.string.alarm_setting_vibrate_title, value);
                alarmSetting.add(item);
            }
            alarmSettingAdapter.notifyDataSetChanged();
        }
        c.close();
        data.close();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view,
            int position, long id) {
        Bundle data = new Bundle();
        data.putInt(FRAGMENT_ID, getId());
        switch ((int) id) {
            case AlarmClockSettingAdapter.TYPE_TIME:
                data.putIntArray(TimePickerFragment.ARG_TIME, (int[]) alarmSetting
                        .get((int) id).getValue());
                TimePickerFragment timePicker = new TimePickerFragment();
                timePicker.setArguments(data);
                timePicker.setOnTimeSetListener(this);
                timePicker.show(getFragmentManager(), TIME_PICKER);
                break;
            case AlarmClockSettingAdapter.TYPE_DAY:
                data.putInt(DayPickerFragment.ARG_DAYS,
                        (Integer) alarmSetting.get((int) id).getValue());
                DayPickerFragment dayPicker = new DayPickerFragment();
                dayPicker.setArguments(data);
                dayPicker.setOnDayOfWeekChangedListener(this);
                dayPicker.show(getFragmentManager(), DAY_PICKER);
                break;
            case AlarmClockSettingAdapter.TYPE_PUZZLE:
                data.putString(PuzzlePickerFragment.ARG_PUZZLE_NAME, alarmSetting
                        .get((int) id).getValue().toString());
                PuzzlePickerFragment puzzlePicker = new PuzzlePickerFragment();
                puzzlePicker.setArguments(data);
                puzzlePicker.setOnPuzzleSelectedListener(this);
                puzzlePicker.show(getFragmentManager(), PUZZLE_PICKER);
                break;
            case AlarmClockSettingAdapter.TYPE_SOUND:
                data.putString(SoundPickerFragment.ARG_SOUND,
                        alarmSetting.get((int) id).getValue().toString());
                SoundPickerFragment soundPicker = new SoundPickerFragment();
                soundPicker.setArguments(data);
                soundPicker.setOnSoundChangeListener(this);
                soundPicker.show(getFragmentManager(), SOUND_PICKER);
                break;
            case AlarmClockSettingAdapter.TYPE_VIBRATE:
                ListItem item = alarmSetting
                        .get(AlarmClockSettingAdapter.TYPE_VIBRATE);
                item.setValue(((Integer) item.getValue()) == 1 ? 0 : 1);
                ContentValues clock = new ContentValues();
                clock.put(AlarmClockTable.VIBRATE, (Integer) item.getValue());
                this.data.updateAlarm(clockId, clock);

                notifyClockSettingChanged(true);
                break;
            case AlarmClockSettingAdapter.TYPE_ENABLE:
                ListItem enable = alarmSetting.get(AlarmClockSettingAdapter.TYPE_ENABLE);
                enable.setValue(((Integer) enable.getValue()) == 1 ? 0 : 1);
                ContentValues enableClock = new ContentValues();
                enableClock.put(AlarmClockTable.ENABLED, (Integer) enable.getValue());
                this.data.updateAlarm(clockId, enableClock);

                notifyClockSettingChanged(false);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        data.close();
    }

    public class ListItem {

        private int id;
        private int icon;
        private String title;
        private Object value;

        public ListItem() {
        }

        public ListItem(int id, int icon, String title, Object value) {
            setId(id);
            setIcon(icon);
            setTitle(title);
            setValue(value);
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

    }

    public interface onClockSettingChangeListener {
        public void onClockSettingChangeed();
    }

    public void setOnClockSettetingChangeListener(
            onClockSettingChangeListener listener) {
        this.clockChangeListener = listener;
    }

    private void notifyClockSettingChanged(boolean enableAlarm) {
        if (enableAlarm) {
            data.enableAlarm(clockId, true);
            ListItem enable = alarmSetting.get(AlarmClockSettingAdapter.TYPE_ENABLE);
            enable.setValue(1);
        }
        alarmSettingAdapter.notifyDataSetChanged();
        if (null != clockChangeListener) {
            clockChangeListener.onClockSettingChangeed();
        }
        AlarmHelper.scheduleNextAlarmById(getActivity(), clockId, true);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        ContentValues clock = new ContentValues();
        clock.put(AlarmClockTable.HOUR, hourOfDay);
        clock.put(AlarmClockTable.MINUTES, minute);
        data.updateAlarm(clockId, clock);

        ListItem item = alarmSetting.get(AlarmClockSettingAdapter.TYPE_TIME);
        item.setValue(new int[] {
                hourOfDay, minute
        });

        notifyClockSettingChanged(true);
    }

    @Override
    public void onChanged(int newDayOfWeekSet) {
        ContentValues clock = new ContentValues();
        clock.put(AlarmClockTable.DAY_OF_WEEK, newDayOfWeekSet);
        data.updateAlarm(clockId, clock);

        ListItem item = alarmSetting.get(AlarmClockSettingAdapter.TYPE_DAY);
        item.setValue(newDayOfWeekSet);

        notifyClockSettingChanged(true);
    }

    @Override
    public void onPuzzleSelected(String puzzleName) {
        ContentValues clock = new ContentValues();
        clock.put(AlarmClockTable.PUZZLE_NAME, puzzleName);
        data.updateAlarm(clockId, clock);

        ListItem item = alarmSetting.get(AlarmClockSettingAdapter.TYPE_PUZZLE);
        item.setValue(puzzleName);

        notifyClockSettingChanged(true);
    }

    @Override
    public void onSoundChange(String soundPath) {
        ContentValues clock = new ContentValues();
        clock.put(AlarmClockTable.SOUND, soundPath);
        data.updateAlarm(clockId, clock);

        ListItem item = alarmSetting.get(AlarmClockSettingAdapter.TYPE_SOUND);
        item.setValue(soundPath);

        notifyClockSettingChanged(true);
    }

    private static class VibratorHelper {

        public boolean hasVibrator(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                try {
                    Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    Class<? extends Vibrator> vibratorClass = v.getClass();
                    Method hasVibrate = vibratorClass.getMethod("hasVibrator");
                    if (!(Boolean) hasVibrate.invoke(v)) {
                        return false;
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }

}
