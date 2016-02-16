
package com.grt_team.wakeup.services;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.WakeUpActivity;
import com.grt_team.wakeup.utils.AlarmHelper;
import com.grt_team.wakeup.utils.SoundHelper;

public class AudioService extends Service {
    private static boolean busy;
    private static boolean puzzleFinished;
    private static boolean isVibrating;
    private static long currentClockId;

    public static final String ACTION_UPDATE_NOTIFICATION = "ACTION_UPDATE_NOTIFICATION";
    public static final String ACTION_START_ALARM = "ACTION_START_ALARM";

    public static final String EXTRA_SOUND_URI = "soundUri";
    public static final String EXTRA_VOLUME = "volume";
    public static final String EXTRA_VIBRATE = "vibrate";

    public static final float MAX_VOLUME = 1;
    public static final float HALF_VOLUME = 0.5f;
    public static final float MIN_VOLUME = 0.1f;

    private static final long[] VIBRATOR_PATTERN = {
            0, 200, 500
    };

    private MediaPlayer mediaPlayer;
    private TelephonyManager telephonyManager;
    private Vibrator vibrator;
    private String soundUri = null;
    float volume = 0;

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                AlarmHelper.changeSoundVolume(getApplicationContext(), MAX_VOLUME, isVibrating());
            } else if (state == TelephonyManager.CALL_STATE_RINGING) {
                AlarmHelper.changeSoundVolume(getApplicationContext(), 0, false);
            }
        }
    };

    public static boolean isBusy() {
        return busy;
    }

    public static boolean isVibrating() {
        return isVibrating;
    }

    public static boolean isPuzzleFinished() {
        return puzzleFinished;
    }

    public static long getCurrentClockId() {
        return currentClockId;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        AudioService.busy = false;
        AudioService.puzzleFinished = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int retType = START_STICKY;
        if (null == intent)
            return retType;
        if (!isBusy() && !ACTION_START_ALARM.equals(intent.getAction())) {
            stopSelf();
            return retType;
        }
        if (ACTION_UPDATE_NOTIFICATION.equals(intent.getAction())) {
            puzzleFinished = true;
            showNotification(intent.getExtras().getLong(AlarmHelper.CLOCK_ID), true);
            return retType;
        }
        AudioService.busy = true;
        if (intent.getExtras().containsKey(EXTRA_SOUND_URI)) {
            String newUri = intent.getExtras().getString(EXTRA_SOUND_URI);
            if (newUri != null && !newUri.equals(soundUri)) {
                soundUri = newUri;
                mediaPlayer.reset();
            }
        }

        long clockId = intent.getExtras().getLong(AlarmHelper.CLOCK_ID);
        if (clockId != 0) {
            showNotification(clockId, false);
            AudioService.currentClockId = clockId;
        }

        if (!mediaPlayer.isPlaying() && !SoundHelper.SILENT_URI.equals(soundUri)) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            try {
                if (soundUri == null || SoundHelper.DEFAULT_URI.equals(soundUri)
                        || "".equals(soundUri.trim()) || !SoundHelper.soundExist(soundUri)) {
                    AssetFileDescriptor assetFileDescriptor = getAssets().openFd(
                            SoundHelper.DEFAULT_RINGTONE);
                    mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),
                            assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                } else {
                    mediaPlayer.setDataSource(soundUri);
                }
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        volume = intent.getExtras().getFloat(EXTRA_VOLUME, MAX_VOLUME);
        mediaPlayer.setVolume(volume, volume);

        boolean vibrate = intent.getExtras().getBoolean(EXTRA_VIBRATE);
        if (vibrator != null) {
            if (vibrate) {
                vibrator.vibrate(VIBRATOR_PATTERN, 0);
            } else {
                vibrator.cancel();
            }
            isVibrating = vibrate;
        }

        return retType;
    }

    @SuppressLint("InlinedApi")
    public void showNotification(long clockId, boolean finished) {
        Intent notify = AlarmHelper.getDialogIntent(this, AlarmHelper.getDataUri(clockId),
                finished, false);
        notify.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            notify.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            notify.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        PendingIntent pendingNotify = PendingIntent.getActivity(this, 0, notify,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm);
        builder.setContentText(getString(R.string.alarm_notification_msg));
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentIntent(pendingNotify);
        startForeground(WakeUpActivity.ALARM_NOTIFICATION, builder.build());
    }

    public void onDestroy() {
        AudioService.busy = false;
        AudioService.currentClockId = 0;

        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        if (vibrator != null) {
            vibrator.cancel();
        }
        telephonyManager.listen(phoneStateListener, 0);
    }
}
