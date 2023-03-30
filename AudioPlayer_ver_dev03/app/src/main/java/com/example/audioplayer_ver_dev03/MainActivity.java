package com.example.audioplayer_ver_dev03;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements IBinder.DeathRecipient {

    // ======
    // START OF FIELDS
    // ======
    AudioPlayerService audioPlayerService;
    // used so activity cannot call service methods before binding completes
    boolean boundToService = false;
    Intent intent;
    ExoPlayer exoPlayer;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // MainActivity.java has bound to AudioPlayerService.java
            // cast IBinder argument iBinder to a BinderToReturnServiceInstance and get AudioPlayerService instance to call public methods
            AudioPlayerService.BinderToReturnServiceInstance binderToReturnServiceInstance = (AudioPlayerService.BinderToReturnServiceInstance) iBinder;
            audioPlayerService = binderToReturnServiceInstance.getService();
            boundToService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("ServiceConnectionCallback", "onServiceDisconnected");
            boundToService = false;
        }
    };
    // must be implemented here, but could be combined with service methods
    private PlayerView playerView;
    // =============
    // END OF FIELDS
    // =============

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // before requesting system run foreground service, start service itself
        intent = new Intent(this, AudioPlayerService.class);
        getApplicationContext().startForegroundService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // bind to AudioPlayerService.java
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        exoPlayer = audioPlayerService.getExoPlayer();
        playerView = findViewById(R.id.video_view);
        playerView.setPlayer(exoPlayer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }

    @Override
    public void binderDied() {

    }

    public void playPause(View view) {

    }
}