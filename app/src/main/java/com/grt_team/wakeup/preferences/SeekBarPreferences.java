
package com.grt_team.wakeup.preferences;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.grt_team.wakeup.R;

public class SeekBarPreferences extends DialogPreference implements
        OnSeekBarChangeListener {

    private int min;
    private int max;
    private int dialogIcon;
    private int textIcon;
    private String summaryFormat;

    private int currentValue;
    private SeekBar seekBar;
    private TextView textView;

    public SeekBarPreferences(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SeekBarPreferences);

        try {
            min = a.getInt(R.styleable.SeekBarPreferences_min, 0);
            max = a.getInt(R.styleable.SeekBarPreferences_max, 10);
            summaryFormat = a
                    .getString(R.styleable.SeekBarPreferences_summaryFormat);
            dialogIcon = a.getResourceId(R.styleable.SeekBarPreferences_dialogIcon,
                    0);
            textIcon = a.getResourceId(R.styleable.SeekBarPreferences_textIcon, 0);
        } finally {
            a.recycle();
        }
        setDialogLayoutResource(R.layout.pref_seek_bar_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        if (dialogIcon != 0) {
            setDialogIcon(dialogIcon);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setInverseBackgroundForced(true);
    }

    private String getFormatedText(boolean usePersisted) {
        int value = (usePersisted) ? getPersistedInt(min) : currentValue;
        return String.format(summaryFormat, String.valueOf(value), min, max);
    }

    private void updateText() {
        textView.setText(getFormatedText(false));
    }

    private void updateSummary() {
        setSummary(getFormatedText(true));
    }

    @Override
    protected void onBindView(View view) {
        updateSummary();
        super.onBindView(view);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        seekBar = (SeekBar) view
                .findViewById(R.id.pref_seek_bar_dialog_seek_bar);
        seekBar.setMax(max - min);
        seekBar.setProgress(currentValue - min);
        seekBar.setOnSeekBarChangeListener(this);
        textView = (TextView) view.findViewById(R.id.pref_seek_bar_dialog_text);
        if (textIcon != 0) {
            textView.setCompoundDrawablesWithIntrinsicBounds(textIcon, 0, 0, 0);
        }
        updateText();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int value = seekBar.getProgress() + min;
            if (callChangeListener(value)) {
                currentValue = value;
                persistInt(value);
                updateSummary();
            }
        } else {
            currentValue = getPersistedInt(min);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, min);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
            Object defaultValue) {
        if (restorePersistedValue) {
            currentValue = getPersistedInt(min);
        } else {
            currentValue = (Integer) defaultValue;
            persistInt(currentValue);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = currentValue;
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

        currentValue = myState.value;
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
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        currentValue = min + seekBar.getProgress();
        updateText();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
