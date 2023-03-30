package com.example.audioplayer_ver_dev03;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.drm.DrmStore;
import android.media.AudioDeviceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.AuxEffectInfo;
import androidx.media3.common.DeviceInfo;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.PriorityTaskManager;
import androidx.media3.common.Timeline;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.text.CueGroup;
import androidx.media3.common.util.Clock;
import androidx.media3.common.util.Size;
import androidx.media3.exoplayer.DecoderCounters;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.PlayerMessage;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.analytics.AnalyticsCollector;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ShuffleOrder;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.TrackSelectionArray;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.exoplayer.video.VideoFrameMetadataListener;
import androidx.media3.exoplayer.video.spherical.CameraMotionListener;
import androidx.media3.ui.PlayerView;

import java.util.List;

public class AudioPlayerService extends Service {

    // ======
    // START OF FIELDS
    // ======
    private final IBinder binderToReturnServiceInstance = new BinderToReturnServiceInstance();
    public class BinderToReturnServiceInstance extends Binder {
        AudioPlayerService getService() {
            // return a service instance so MainActivity.java can call public methods
            return AudioPlayerService.this;
        }
    }

    final int AUDIO_PLAYER_SERVICE_NOTIFICATION_ID = 123456;

    private ExoPlayer exoPlayer;
    // could implement these with shared preferences?
//    private boolean playWhenReady;
    private int currentItem;
//    private long playbackPosition;
    private final Player.Listener playbackStateListener = new PlaybackStateListener();
    // =============
    // END OF FIELDS
    // =============

    // invoked by calling startService()
    // when run, service is started and runs in background indefinitely
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, AudioPlayerService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        // how to update the notification
        // https://stackoverflow.com/questions/5528288/how-do-i-update-the-notification-text-for-a-foreground-service-in-android
        Notification notification = new Notification.Builder(this, "id_music")
                .setContentTitle(getString(R.string.audio_service_notif_title))
                .setContentText(getString(R.string.audio_service_notif_text))
                .setSmallIcon(R.drawable.ic_baseline_audiotrack_24)
                .setContentIntent(pendingIntent)
                .setTicker(getString(R.string.audio_service_notif_ticker))
                .build();
        startForeground(AUDIO_PLAYER_SERVICE_NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    // invoked by calling bindService()
    // clients communicate with service using IBinder, an interface
    // "return null;" indicates binding isn't implemented
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binderToReturnServiceInstance;
    }

    // perform one-time setup when service is initially created
    // called before onBind() and onStartCommand()
    @Override
    public void onCreate() {
//        playWhenReady = true;
        currentItem = 0;
//        playbackPosition = 0L;
        initializePlayer();
    }

    // called when service isn't needed anymore and is destroyed
    // clean up resources with this method
    @Override
    public void onDestroy() {
        releasePlayer();
        // removes service from foreground ONLY
        // boolean removeNotification (true indicates service's notification will be removed by this method)
        stopForeground(true);
        // stops service
        stopSelf();
    }

    private void initializePlayer() {
        exoPlayer = new ExoPlayer.Builder(this).build();
        MediaItem mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp3));
        exoPlayer.setMediaItem(mediaItem);
    }

    private void releasePlayer() {
//        playbackPosition = exoPlayer.getCurrentPosition();
        currentItem = exoPlayer.getCurrentMediaItemIndex();
//        playWhenReady = exoPlayer.getPlayWhenReady();
        exoPlayer.removeListener(playbackStateListener);
        exoPlayer.release();
        exoPlayer = null;
    }

    public ExoPlayer getExoPlayer() {
        return exoPlayer;
    }
}
class PlaybackStateListener implements Player.Listener {

    private final String TAG = "AudioPlayer_dev.0.2";
    String state;

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        switch (playbackState) {
            // has been instantiated, but ExoPlayer.prepare() hasn't been called
            case ExoPlayer.STATE_IDLE:
                state = "ExoPlayer.STATE_IDLE      -";
                break;
            // player's data buffer has run out, and must load more data
            case ExoPlayer.STATE_BUFFERING:
                state = "ExoPlayer.STATE_BUFFERING -";
                break;
            // media being played has finished
            case ExoPlayer.STATE_ENDED:
                state = "ExoPlayer.STATE_ENDED     -";
                break;
            // ready to play from current position, and will play automatically if playWhenReady = true
            case ExoPlayer.STATE_READY:
                state = "ExoPlayer.STATE_READY     -";
                break;
            default:
                state = "UNKNOWN_STATE             -";
                break;
        }
        Log.d(TAG, "changed state to " + state);
    }
}
