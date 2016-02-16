
package com.grt_team.wakeup.preferences;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.utils.DisplayHelper;

public class MazeSizeBarPreferences extends DialogPreference implements
        OnSeekBarChangeListener {

    private static final int MIN_BLOCK_COUNT = 15;
    private static final int DEF_BLOCK_COUNT = 25;
    private static final int MAX_BLOCK_COUNT = 80;
    private static final int MAX_BLOCK_SIZE_DP = 10;

    private int maxBlockCount;

    private int currentBlockSize;
    private SeekBar seekBar;
    private TextView textView;

    public MazeSizeBarPreferences(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.pref_puzzle_maze_size_seek_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();

        int width = Math.max(DisplayHelper.getScreenWidth(getContext()),
                DisplayHelper.getScreenHeight(getContext()));

        maxBlockCount = (int) (width / (MAX_BLOCK_SIZE_DP * metrics.density));
        maxBlockCount = (maxBlockCount > MAX_BLOCK_COUNT) ? MAX_BLOCK_COUNT : maxBlockCount;
        maxBlockCount = (maxBlockCount < DEF_BLOCK_COUNT) ? DEF_BLOCK_COUNT : maxBlockCount;
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setInverseBackgroundForced(true);
    }

    private void updateText() {
        textView.setText(currentBlockSize + " X " + getMazeRows(currentBlockSize));
    }

    private void updateSummary() {
        int blockSize = getPersistedInt(DEF_BLOCK_COUNT);
        setSummary(blockSize + " X " + getMazeRows(blockSize));
    }

    private int getMazeRows(int cols) {
        int width = Math.max(DisplayHelper.getScreenHeight(getContext()),
                DisplayHelper.getScreenWidth(getContext()));
        int height = Math.min(DisplayHelper.getScreenHeight(getContext()),
                DisplayHelper.getScreenWidth(getContext()));

        float titleHeight = getContext().getResources().getDimension(R.dimen.puzzle_title_height);

        return (int) ((height - titleHeight) / (width / cols) / 2 * 2 + 1);
    }

    @Override
    protected void onBindView(View view) {
        updateSummary();
        super.onBindView(view);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        seekBar = (SeekBar) view.findViewById(R.id.pref_maze_seek_bar);
        seekBar.setMax((maxBlockCount - MIN_BLOCK_COUNT) / 2);
        seekBar.setProgress((currentBlockSize - MIN_BLOCK_COUNT) / 2);
        seekBar.setOnSeekBarChangeListener(this);
        textView = (TextView) view.findViewById(R.id.pref_maze_size_text);
        updateText();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int value = seekBar.getProgress() * 2 + MIN_BLOCK_COUNT;
            if (callChangeListener(value)) {
                currentBlockSize = value;
                persistInt(value);
                updateSummary();
            }
        } else {
            currentBlockSize = getPersistedInt(DEF_BLOCK_COUNT);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEF_BLOCK_COUNT);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
            Object defaultValue) {
        if (restorePersistedValue) {
            currentBlockSize = getPersistedInt(DEF_BLOCK_COUNT);
        } else {
            currentBlockSize = (Integer) defaultValue;
            persistInt(currentBlockSize);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = currentBlockSize;
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

        currentBlockSize = myState.value;
    }

    private static class SavedState extends BaseSavedState {
        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currentBlockSize = MIN_BLOCK_COUNT + seekBar.getProgress() * 2;
        updateText();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

}
