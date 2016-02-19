
package com.grt_team.wakeup.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.grt_team.wakeup.R;

public class SoundHelper {

    public static final String TAG = "SoundHelper";

    public final static int SOUND_SOURCE_AUTO = 0;
    public final static int SOUND_SOURCE_SDCARD = 1;
    public final static int SOUND_SOURCE_LOCAL = 2;
    public final static int SOUND_SOURCE_SDCARD_RINGTONE = 3;
    public final static int SOUND_SOURCE_LOCAL_RINGTONE = 4;
    public final static int SOUND_SOURCE_EMPTY = 5;

    public static final String SILENT_URI = "silent";
    public static final String DEFAULT_URI = "default";

    public static final String DEFAULT_RINGTONE = "Cesium.ogg";

    public static final int EmptyCursorCount = 1; // involving silent element

    private static final Uri LOCATION_SDCARD = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final Uri LOCATION_LOCAL = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

    private static final String TYPE_MUSIC = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    private static final String TYPE_RINGTONE = MediaStore.Audio.Media.IS_RINGTONE + " != 0";
    
    private static final String MUSIC_TYPE_SDCARD = "media";
    private static final String MUSIC_TYPE_LOCAL = "local";
    private static final String MUSIC_TYPE_SDCARD_RINGTONE = "media ringtone";
    private static final String MUSIC_TYPE_LOCAL_RINGTONE = "local ringtone";

    private static final String[] COLUMNS = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME
    };

    private SoundHelper() {
    }

    public static class AudioSource {
        public static final int SOURCE_RINGTONE = 0;
        public static final int SOURCE_MUSIC = 1;
        public static final int SOURCE_AUTO = 2;

        private int sourceType;

        public AudioSource() {
            sourceType = SOURCE_AUTO;
        }

        public int getSourceType() {
            return sourceType;
        }

        public void setSourceType(int sourceType) {
            this.sourceType = sourceType;
        }

        public void toggleSource() {
            sourceType = (sourceType == AudioSource.SOURCE_RINGTONE) ? AudioSource.SOURCE_MUSIC
                    : AudioSource.SOURCE_RINGTONE;
        }

    }

    public static Cursor getAudio(Context context, AudioSource audioSource, String selectedAudio) {
        Cursor cursor;
        switch (audioSource.getSourceType()) {
            case AudioSource.SOURCE_AUTO:
            default:
                cursor = getAllRingtones(context);
                audioSource.setSourceType(AudioSource.SOURCE_RINGTONE);

                if (getSelectedItemPosition(cursor, selectedAudio) == -1
                        || SoundHelper.SILENT_URI.equals(selectedAudio)) {

                    Cursor musicCursor = getAllMusic(context);
                    if (musicCursor.getCount() > EmptyCursorCount) {
                        cursor.close();
                        cursor = musicCursor;
                        audioSource.setSourceType(AudioSource.SOURCE_MUSIC);
                    } else {
                        musicCursor.close();
                    }
                }
                break;
            case AudioSource.SOURCE_MUSIC:
                cursor = getAllMusic(context);
                break;
            case AudioSource.SOURCE_RINGTONE:
                cursor = getAllRingtones(context);
                break;
        }
        return cursor;
    }

    private static Cursor getSiletnElement(Context context) {
        final MatrixCursor mc = new MatrixCursor(COLUMNS);

        mc.addRow(new String[] {
                "0",
                SILENT_URI,
                context.getString(R.string.music_silent)
        });
        return mc;
    }

    private static MergeCursor getAllMusic(Context context) {
        return new MergeCursor(new Cursor[] {
                getSiletnElement(context),
                getMusicList(context, SoundHelper.SOUND_SOURCE_SDCARD)
        });
    }

    private static MergeCursor getAllRingtones(Context context) {

        return new MergeCursor(
                new Cursor[] {
                        getSiletnElement(context),
                        getMusicList(context, SoundHelper.SOUND_SOURCE_LOCAL_RINGTONE),
                        getMusicList(context, SoundHelper.SOUND_SOURCE_SDCARD_RINGTONE)
                });
    }

    private static boolean hasSound(Context context, int sourceType) {
        return getMusicList(context, sourceType).getCount() > 0;
    }

    private static Cursor getMusicList(Context context, int sourceType) {
        Uri musicLocation = null;
        String musicType = null;

        if (sourceType == SOUND_SOURCE_AUTO) {
            List<SourceItem> sourcesList = getSourcesList(context);
            sourceType = sourcesList.size() > 0 ? sourcesList.get(0).getSource()
                    : SoundHelper.SOUND_SOURCE_EMPTY;
        }

        switch (sourceType) {
            case SOUND_SOURCE_SDCARD_RINGTONE:
                musicLocation = SoundHelper.LOCATION_SDCARD;
                musicType = SoundHelper.TYPE_RINGTONE;
                break;
            case SOUND_SOURCE_SDCARD:
                musicLocation = SoundHelper.LOCATION_SDCARD;
                musicType = SoundHelper.TYPE_MUSIC;
                break;
            case SOUND_SOURCE_LOCAL:
                musicLocation = SoundHelper.LOCATION_LOCAL;
                musicType = SoundHelper.TYPE_MUSIC;
                break;
            case SOUND_SOURCE_LOCAL_RINGTONE:
                musicLocation = SoundHelper.LOCATION_LOCAL;
                musicType = SoundHelper.TYPE_RINGTONE;
                break;
        }
        return context.getContentResolver().query(musicLocation, COLUMNS, musicType, null, null);
    }

    public static int getSelectedItemPosition(Cursor c, String itemPath) {
        if (itemPath != null) {
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                if (c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA)).equals(itemPath))
                    return c.getPosition();
            }
        }
        return -1;
    }

    private static class SourceItem {

        private int source;
        private String displayedName;

        public SourceItem(int source, String displayedName) {
            this.source = source;
            this.displayedName = displayedName;
        }

        public int getSource() {
            return source;
        }

        @Override
        public String toString() {
            return displayedName;
        }
    }

    /**
     * check locations for sound files. If sounds present in the location, put
     * this location to the list
     */
    private static List<SourceItem> getSourcesList(Context context) {
        String status = Environment.getExternalStorageState();
        List<SourceItem> musicList = new ArrayList<SoundHelper.SourceItem>();
        musicList.clear();

        // Check if SD card is available
        if (!status.equals(Environment.MEDIA_SHARED) && status.equals(Environment.MEDIA_MOUNTED)) {

            if (hasSound(context, SOUND_SOURCE_SDCARD)) {
                musicList.add(new SourceItem(SoundHelper.SOUND_SOURCE_SDCARD, MUSIC_TYPE_SDCARD));
            }
            if (hasSound(context, SOUND_SOURCE_SDCARD_RINGTONE)) {
                musicList.add(new SourceItem(SoundHelper.SOUND_SOURCE_SDCARD_RINGTONE, MUSIC_TYPE_SDCARD_RINGTONE));
            }
        }
        if (hasSound(context, SOUND_SOURCE_LOCAL)) {
            musicList.add(new SourceItem(SoundHelper.SOUND_SOURCE_LOCAL, MUSIC_TYPE_LOCAL));
        }
        if (hasSound(context, SOUND_SOURCE_LOCAL_RINGTONE)) {
            musicList.add(new SourceItem(SoundHelper.SOUND_SOURCE_LOCAL_RINGTONE, MUSIC_TYPE_LOCAL_RINGTONE));
        }
        return musicList;
    }

    public static boolean soundExist(String uri) {
        if (uri == null) {
            return false;
        }

        if (SILENT_URI.equals(uri) || DEFAULT_URI.equals(uri)) {
            return true;
        }

        File file = new File(uri);
        if (file.exists()) {
            return true;
        } else {
            Log.w(TAG, "File not found: " + uri);
            return false;
        }
    }

    /**
     * Get file name by file path
     * 
     * @return file name by file path
     */
    public static String getFileName(String uri, Context context) {
        if (SILENT_URI.equals(uri)) {
            return context.getResources().getString(R.string.music_silent);
        } else {
            Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (defaultUri != null && defaultUri.toString().equals(uri)) {
                return context.getResources().getString(R.string.music_default);
            }
        }
        return Uri.parse(uri).getLastPathSegment();
    }

}
