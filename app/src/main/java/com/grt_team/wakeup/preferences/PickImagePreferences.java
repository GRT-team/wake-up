
package com.grt_team.wakeup.preferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;
import com.grt_team.wakeup.entity.puzzle.mosaic.MosaicView;

public class PickImagePreferences extends Preference implements
        SettingsActivity.PreferencesOnActivityResult {

    private static final String TAG = "PickImagePreferences";

    public static final String DEF_FILE = "default";

    private String currentFile;

    public PickImagePreferences(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void updateSummary() {
        if (DEF_FILE.equals(currentFile)) {
            setSummary(getContext().getString(
                    R.string.pref_puzzle_mosaic_def_img_title));
        } else {
            File f = new File(currentFile);
            setSummary(f.getName());
        }
    }

    @Override
    protected void onBindView(View view) {
        updateSummary();
        super.onBindView(view);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
            Object defaultValue) {
        if (restorePersistedValue) {
            currentFile = getPersistedString(DEF_FILE);
        } else {
            currentFile = (String) defaultValue;
            persistString(currentFile);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = currentFile;
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

        currentFile = myState.value;
    }

    @Override
    protected void onClick() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        ((SettingsActivity) getContext()).startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                SettingsActivity.REQUEST_CODE_PICK_IMAGE);
        ((SettingsActivity) getContext()).setPreferencesOnActivityResult(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SettingsActivity.REQUEST_CODE_PICK_IMAGE
                && resultCode == SettingsActivity.RESULT_OK) {
            Uri file = Uri.parse(String.valueOf(data.getData()));
            currentFile = saveImageToFiles(file);
            persistString(currentFile);
        }
        updateSummary();
    }

    private String saveImageToFiles(Uri uri) {
        Bitmap b = MosaicView.resolveBitmap(getContext(), uri);

        if (currentFile != null && !DEF_FILE.equals(currentFile)) {
            File f = new File(currentFile);
            f.delete();
        }

        OutputStream os = null;
        String filePath = getContext().getFilesDir().getPath() + File.separator + uri.getLastPathSegment();
        try {
            File f = new File(filePath);
            os = new FileOutputStream(f);

            b.compress(CompressFormat.JPEG, 100, os);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Can't create file");
            return null;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
            }
        }

        return filePath;
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

}
