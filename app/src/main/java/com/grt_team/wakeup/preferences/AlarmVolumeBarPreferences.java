
package com.grt_team.wakeup.preferences;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class AlarmVolumeBarPreferences extends DialogPreference implements OnSeekBarChangeListener,
        SettingsActivity.PreferencesOnStopListener {

    private AudioManager audioManager;
    private SeekBar seekBar;
    private int newVolumeIndex;
    private int currentVolumeIndex;
    private Ringtone ringtone;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        seekBar = (SeekBar) view.findViewById(R.id.pref_volume_seek_bar);
        seekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        seekBar.setProgress(newVolumeIndex);
        seekBar.setOnSeekBarChangeListener(this);

        Context context = getContext();
        if (context instanceof SettingsActivity) {
            SettingsActivity settingsActivity = (SettingsActivity) context;
            settingsActivity.setPreferencesOnStopListener(this);
        }

        if (ringtone == null) {
            ringtone = RingtoneManager.getRingtone(getContext(),
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
            if (ringtone != null) {
                ringtone.setStreamType(AudioManager.STREAM_ALARM);
            }
        }
    }

    public AlarmVolumeBarPreferences(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.pref_volume_seek_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setInverseBackgroundForced(true);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.newVolumeIndex = newVolumeIndex;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        seekBar.setProgress(myState.newVolumeIndex);

        super.onRestoreInstanceState(myState.getSuperState());
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Return 0 as default value to call onSetInitialValue where current
        // Alarm volume level will be read from system preferences
        return 0;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        currentVolumeIndex = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        newVolumeIndex = currentVolumeIndex;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            getContext().getContentResolver().notifyChange(
                    Settings.System.getUriFor(Settings.System.VOLUME_ALARM), null);
            currentVolumeIndex = newVolumeIndex;
        } else {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolumeIndex, 0);
            newVolumeIndex = currentVolumeIndex;
        }

        if (ringtone != null) {
            ringtone.stop();
        }

        Context context = getContext();
        if (context instanceof SettingsActivity) {
            SettingsActivity settingsActivity = (SettingsActivity) context;
            settingsActivity.setPreferencesOnStopListener(null);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        newVolumeIndex = progress;
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, newVolumeIndex, 0);
        if (ringtone != null) {
            ringtone.play();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        return;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        return;
    }

    private static class SavedState extends BaseSavedState {
        int newVolumeIndex;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            newVolumeIndex = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(newVolumeIndex);
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
    public void onStop() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

}
