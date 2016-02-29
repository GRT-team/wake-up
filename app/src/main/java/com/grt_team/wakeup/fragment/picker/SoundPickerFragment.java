
package com.grt_team.wakeup.fragment.picker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.fragment.AlarmClockSettingListFragment;
import com.grt_team.wakeup.utils.SoundHelper;
import com.grt_team.wakeup.utils.SoundHelper.AudioSource;

public class SoundPickerFragment extends DialogFragment implements DialogInterface.OnClickListener,
        View.OnClickListener, OnItemClickListener, OnCompletionListener {

    public final static String ARG_SOUND = "ARG_SOUND";
    public final static String SOUND_SOURCE = "SOUND_SOURCE";
    public final static String CURRENT_AUDIO = "CURRENT_AUDIO";
    public final static String IS_PLAYING = "IS_PLAYING";

    private OnSoundChangeListener listener;

    private SimpleCursorAdapter audioAdapter;
    private ListView audioList;
    private Button audioButton;

    private MediaPlayer mp;
    private boolean isPlaying = false;

    private AudioSource audioSource = new AudioSource();
    private String currentAudio = null;
    private String selectedAudio;

    public interface OnSoundChangeListener {
        public void onSoundChange(String soundPath);
    }

    public void setOnSoundChangeListener(OnSoundChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (null == listener) {
            listener = (AlarmClockSettingListFragment) getFragmentManager().findFragmentById(
                    getArguments().getInt(AlarmClockSettingListFragment.FRAGMENT_ID));
        }
        selectedAudio = getArguments().getString(ARG_SOUND);
        if (null != savedInstanceState) {
            int source = savedInstanceState.getInt(SOUND_SOURCE, AudioSource.SOURCE_AUTO);
            audioSource.setSourceType(source);
            currentAudio = savedInstanceState.getString(CURRENT_AUDIO);
            isPlaying = savedInstanceState.getBoolean(IS_PLAYING);
        }
        return buildDialog(selectedAudio);
    }

    private Dialog buildDialog(String selectedAudio) {
        View view = View.inflate(getActivity(), R.layout.audio_list, null);
        audioButton = (Button) view.findViewById(R.id.audio_list_button);
        audioButton.setOnClickListener(this);

        String[] from = new String[] {
                MediaStore.Audio.Media.DISPLAY_NAME
        };
        int[] to = new int[] {
                android.R.id.text1
        };

        audioAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_single_choice, null, from, to, 0);
        audioList = (ListView) view.findViewById(R.id.audio_list);
        audioList.setAdapter(audioAdapter);
        audioList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        audioList.setOnItemClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton(android.R.string.cancel, this);
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setView(view);
        builder.setTitle(R.string.music_dialog_title);

        if (Build.VERSION.SDK_INT < 11) {
            builder.setInverseBackgroundForced(true);
        }

        return builder.create();
    }

    private void updateToggleButton() {
        if (audioSource.getSourceType() == AudioSource.SOURCE_RINGTONE) {
            audioButton.setText(R.string.music_type_music);
            Drawable img = getResources().getDrawable( R.drawable.music_volume);
            img.setBounds( 0, 0, img.getIntrinsicWidth(), img.getIntrinsicHeight());
            audioButton.setCompoundDrawablePadding( - img.getIntrinsicWidth());
            audioButton.setCompoundDrawables(img, null, null, null);
        } else {
            audioButton.setText(R.string.music_type_ringtone);
            Drawable img = getResources().getDrawable( R.drawable.ring_volume);
            img.setBounds( 0, 0, img.getIntrinsicWidth(), img.getIntrinsicHeight());
            audioButton.setCompoundDrawablePadding( - img.getIntrinsicWidth());
            audioButton.setCompoundDrawables(img, null, null, null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SOUND_SOURCE, audioSource.getSourceType());
        outState.putString(CURRENT_AUDIO, currentAudio);
        outState.putBoolean(IS_PLAYING, isPlaying);
        super.onSaveInstanceState(outState);
    }

    private void stopPlay() {
        if (mp != null && mp.isPlaying()) {
            mp.stop();
        }
    }

    private void startPlay() {
        if (currentAudio == null) {
            return;
        }

        isPlaying = true;
        if (mp == null) {
            mp = new MediaPlayer();
            mp.setOnCompletionListener(this);
        }

        try {
            if (SoundHelper.SILENT_URI.equals(currentAudio)) {
                stopPlay();
            } else {
                mp.reset();
                mp.setDataSource(currentAudio);
                mp.setAudioStreamType(AudioManager.STREAM_ALARM);
                mp.prepare();
                mp.start();
            }
        } catch (Exception e) {
            // Suppress exception if there is some errors
            // with music play
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mp != null) {
            mp.reset();
            mp.release();
            mp = null;
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (isPlaying) {
            startPlay();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (DialogInterface.BUTTON_POSITIVE == which) {
            audioAdapter.getCursor().moveToPosition(audioList.getCheckedItemPosition());
            String path = audioAdapter.getCursor().getString(
                    audioAdapter.getCursor().getColumnIndex(MediaStore.Audio.Media.DATA));
            if (listener != null) {
                listener.onSoundChange(path);
            }
        }
        dialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        audioSource.toggleSource();

        audioAdapter.getCursor().close();
        audioAdapter.changeCursor(SoundHelper.getAudio(getActivity(), audioSource, null));
        
        updateToggleButton();
        
        int selectedPosition = SoundHelper.getSelectedItemPosition(audioAdapter.getCursor(),
                (currentAudio == null) ? selectedAudio : currentAudio);
        selectedPosition = (selectedPosition > -1) ? selectedPosition : 0;
        audioList.setItemChecked(selectedPosition, true);
        audioList.setSelection(selectedPosition);
        stopPlay();
        isPlaying = false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        audioAdapter.getCursor().moveToPosition(position);
        currentAudio = audioAdapter.getCursor().getString(
                audioAdapter.getCursor().getColumnIndex(MediaStore.Audio.Media.DATA));
        startPlay();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        isPlaying = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        Cursor audioCursor = SoundHelper.getAudio(getActivity(), audioSource,
                (currentAudio == null) ? selectedAudio : currentAudio);
        audioAdapter.changeCursor(audioCursor);
        updateToggleButton();

        int selectedPosition = SoundHelper.getSelectedItemPosition(audioCursor,
                (currentAudio == null) ? selectedAudio : currentAudio);
        selectedPosition = (selectedPosition > -1) ? selectedPosition : 0;
        audioList.setItemChecked(selectedPosition, true);
        audioList.setSelection(selectedPosition);
    }

    @Override
    public void onStop() {
        super.onStop();
        audioAdapter.changeCursor(null);
    }

}
