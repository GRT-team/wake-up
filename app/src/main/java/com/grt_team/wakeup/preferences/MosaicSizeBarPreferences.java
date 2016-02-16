
package com.grt_team.wakeup.preferences;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.grt_team.wakeup.R;

public class MosaicSizeBarPreferences extends DialogPreference implements
        OnSeekBarChangeListener {

    private static final int MIN_WIDTH = 2;
    private static final int MIN_HEIGHT = 2;
    private static final int MAX_WIDTH = 9;
    private static final int MAX_HEIGHT = 9;
    private static final int DEF_HEIGHT = 3;
    private static final int DEF_WIDTH = 3;

    private static final String DELIMITER = "x";
    private static final String DEF_VALUE = valueToString(DEF_WIDTH, DEF_HEIGHT);

    private int currentWidth;
    private int currentHeight;

    private SeekBar seekBarWidth;
    private SeekBar seekBarHeight;
    private TextView textView;

    public static Point valueFromString(String value) {
        Point point = new Point();
        String[] parts = value.split(DELIMITER);
        point.set(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
        return point;
    }

    public static String valueToString(int width, int height) {
        return (width + DELIMITER + height);
    }

    private void readCurrentFromString(String value) {
        Point point = valueFromString(value);
        currentWidth = Math.max(point.x - MIN_WIDTH, 0);
        currentHeight = Math.max(point.y - MIN_HEIGHT, 0);
    }

    public MosaicSizeBarPreferences(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.pref_puzzle_mosaic_size_seek_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setInverseBackgroundForced(true);
    }

    private void updateText() {
        textView.setText((MIN_WIDTH + currentWidth) + " X " + (MIN_HEIGHT + currentHeight));
    }

    private void updateSummary() {
        Point point = valueFromString(getPersistedString(DEF_VALUE));
        setSummary(point.x + " X " + point.y);
    }

    @Override
    protected void onBindView(View view) {
        updateSummary();
        super.onBindView(view);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        seekBarWidth = (SeekBar) view
                .findViewById(R.id.pref_mosaic_width_seek_bar);
        seekBarWidth.setMax(MAX_WIDTH - MIN_WIDTH);
        seekBarWidth.setProgress(currentWidth);
        seekBarWidth.setOnSeekBarChangeListener(this);

        seekBarHeight = (SeekBar) view
                .findViewById(R.id.pref_mosaic_height_seek_bar);
        seekBarHeight.setMax(MAX_HEIGHT - MIN_HEIGHT);
        seekBarHeight.setProgress(currentHeight);
        seekBarHeight.setOnSeekBarChangeListener(this);

        textView = (TextView) view.findViewById(R.id.pref_mosaic_size_text);
        updateText();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int widthValue = MIN_WIDTH + seekBarWidth.getProgress();
            int heightValue = MIN_HEIGHT + seekBarHeight.getProgress();
            String value = valueToString(widthValue, heightValue);
            if (callChangeListener(value)) {
                readCurrentFromString(value);

                persistString(value);
                updateSummary();
            }
        } else {
            readCurrentFromString(getPersistedString(DEF_VALUE));
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
            Object defaultValue) {
        if (restorePersistedValue) {
            readCurrentFromString(getPersistedString(DEF_VALUE));
        } else {
            readCurrentFromString(DEF_VALUE);
            persistString(DEF_VALUE);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = valueToString(currentWidth, currentHeight);
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        Point point = valueFromString(myState.value);
        currentWidth = point.x;
        currentHeight = point.y;
    }

    private static class SavedState extends BaseSavedState {
        String value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(value);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        currentWidth = seekBarWidth.getProgress();
        currentHeight = seekBarHeight.getProgress();
        updateText();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

}
