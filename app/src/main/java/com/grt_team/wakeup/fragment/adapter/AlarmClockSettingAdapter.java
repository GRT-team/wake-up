
package com.grt_team.wakeup.fragment.adapter;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.entity.puzzle.PuzzleHelper;
import com.grt_team.wakeup.fragment.AlarmClockSettingListFragment.ListItem;
import com.grt_team.wakeup.utils.DayOfWeekHelper;
import com.grt_team.wakeup.utils.SoundHelper;
import com.grt_team.wakeup.view.SwitchButton;

public class AlarmClockSettingAdapter extends ArrayAdapter<ListItem> {

    // Types of the clock setting list item
    public final static int TYPE_ENABLE = 0;
    public final static int TYPE_TIME = 1;
    public final static int TYPE_DAY = 2;
    public final static int TYPE_PUZZLE = 3;
    public final static int TYPE_SOUND = 4;
    public final static int TYPE_VIBRATE = 5;

    public static String VALUE_TAG = "VALUE_TAG";

    private List<ListItem> clockSettingItems; // list of icon resources, title
                                              // and values for clock setting
                                              // list item
    private int resourceId; // resource of list item

    public AlarmClockSettingAdapter(Context context, int resourceId,
            List<ListItem> clockSettingItems) {
        super(context, resourceId, clockSettingItems);
        this.clockSettingItems = clockSettingItems;
        this.resourceId = resourceId;
    }

    @Override
    public long getItemId(int position) {
        return clockSettingItems.get(position).getId();
    }

    /**
     * Create view for clock setting list item
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = createView(position, parent);
        setViewData(view, clockSettingItems.get(position));
        return view;
    }

    /**
     * Fill list item with icon, title and value.
     * 
     * @param view - View to set data
     * @param data - Data which need to be set to clock setting item
     */
    private void setViewData(final View view, ListItem data) {
        ImageView icon;
        TextView title;
        View value;
        icon = (ImageView) view.findViewById(R.id.alarm_setting_icon);
        title = (TextView) view.findViewById(R.id.alarm_setting_title);
        value = view.findViewWithTag(VALUE_TAG);

        icon.setImageResource(data.getIcon());
        title.setText(data.getTitle());
        switch (data.getId()) {
            case TYPE_ENABLE:
                ((CompoundButton) value).setChecked(((Integer) data.getValue()) == 1);
                break;
            case TYPE_TIME:
                int time[] = (int[]) data.getValue();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, time[0]);
                calendar.set(Calendar.MINUTE, time[1]);
                String timeValue = DateFormat.getTimeFormat(getContext())
                        .format(calendar.getTime());
                ((TextView) value).setText(timeValue);
                break;
            case TYPE_DAY:
                String days = DayOfWeekHelper.toString(getContext(),
                        Integer.valueOf(data.getValue().toString()), true);
                ((TextView) value).setText(days);
                break;
            case TYPE_PUZZLE:
                String puzzleName = PuzzleHelper.getPuzzleTitle(
                        (String) data.getValue().toString(), getContext());
                ((TextView) value).setText(puzzleName);
                break;
            case TYPE_SOUND:
                String soundName = SoundHelper
                        .getFileName(data.getValue().toString(), getContext());
                if (SoundHelper.DEFAULT_URI.equals(soundName)) {
                    soundName = getContext().getResources().getString(
                            R.string.music_default);
                }
                ((TextView) value).setText(soundName);
                break;
            case TYPE_VIBRATE:
                ((CheckBox) value).setChecked(((Integer) data.getValue()) == 1);
                break;
        }
    }

    /**
     * Create view for clock setting list item, and add checkboxes for "Enable"
     * or "Vibration" setting, or ImageButton for "Puzzle" setting
     * 
     * @param position - list element position
     * @param parent - parent view for list item
     * @return created view for clock setting list item
     */
    private View createView(final int position, final ViewGroup parent) {
        View view, settingValue;
        LayoutInflater inflater = ((LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE));
        view = inflater.inflate(resourceId, parent, false);
        FrameLayout settingValueLayout;
        view.findViewById(R.id.alarm_setting_title).setLayoutParams(
                getSettingTitleLayoutParams(position));
        settingValueLayout = (FrameLayout) view.findViewById(R.id.alarm_setting_value);
        if (TYPE_VIBRATE == getItemId(position)) {
            settingValue = new CheckBox(getContext());
            settingValue.setClickable(false);
            settingValue.setFocusable(false);
        } else if (TYPE_ENABLE == getItemId(position)) {
            // Inflate switch button from xml with merge tag to
            // settingValueLayout. Remove element from layout, later it will be
            // added in general code.
            settingValue = inflater.inflate(R.layout.alarm_clock_switch, null);
            settingValueLayout.removeView(settingValue);

            ListItem item = clockSettingItems.get(position);
            ((SwitchButton) settingValue).setChecked(item.getValue().equals(1));
            ((SwitchButton) settingValue).setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ListItem item = clockSettingItems.get(position);
                    if (parent instanceof ListView) {
                        // Perform parent ListView item click when switch state
                        // is changed
                        ((ListView) parent).performItemClick(parent, position, item.getId());
                    }
                }
            });
        } else {
            settingValue = new TextView(getContext());
        }
        settingValue.setTag(VALUE_TAG);
        settingValueLayout.addView(settingValue);
        settingValueLayout.setLayoutParams(getSettingValueLayoutParams(position));

        if (TYPE_PUZZLE == getItemId(position)) {
            LinearLayout puzzleSettingLayout = (LinearLayout) inflater.inflate(
                    R.layout.alarm_clock_setting_puzzle_bt, null);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            puzzleSettingLayout.setLayoutParams(params);
            ImageButton puzzleSettingButton = (ImageButton) puzzleSettingLayout
                    .findViewById(R.id.alarm_clock_setting_show_puzzle);
            puzzleSettingButton.setFocusable(false);
            puzzleSettingButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    PuzzleHelper.showPuzzleSettings(getContext(), getItem(position).getValue()
                            .toString());
                }
            });
            ((RelativeLayout) view).addView(puzzleSettingLayout);
        }
        return view;
    }

    /**
     * Get LayoutParams for clock setting value
     * 
     * @param position - list element position
     */
    private LayoutParams getSettingValueLayoutParams(int position) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (clockSettingItems.get(position).getId() == TYPE_VIBRATE
                || clockSettingItems.get(position).getId() == TYPE_ENABLE) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
        } else {
            params.addRule(RelativeLayout.BELOW, R.id.alarm_setting_title);
        }
        return params;
    }

    /**
     * Get LayoutParams for clock setting title
     * 
     * @param position - list element position
     */
    private LayoutParams getSettingTitleLayoutParams(int position) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (clockSettingItems.get(position).getId() == TYPE_VIBRATE
                || clockSettingItems.get(position).getId() == TYPE_ENABLE) {
            params.addRule(RelativeLayout.CENTER_VERTICAL);
        }
        return params;
    }
}
