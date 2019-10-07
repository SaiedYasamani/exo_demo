package com.banico.exo_demo;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {

    ProgressBar voiceProgress;
    AudioManager audioManager;
    FrameLayout exoLayer;
    ImageView speaker;
    boolean isControllerHidden = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final PlayerView playerView = findViewById(R.id.exo_view);
        exoLayer = findViewById(R.id.exo_overlay);
        speaker = findViewById(R.id.speaker);
        ImageButton setting = findViewById(R.id.setting);
        voiceProgress = findViewById(R.id.voice_progress);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        ImageView brightness = findViewById(R.id.brightness);
        brightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean settingsCanWrite = hasPermission(MainActivity.this);
                if(!settingsCanWrite) {
                    changePermission(MainActivity.this);
                }else {
//                    WindowManager.LayoutParams attributes = getWindow().getAttributes();
//                    attributes.screenBrightness += 10;
//                    getWindow().setAttributes(attributes);
//                    getWindow().addFlags(WindowManager.LayoutParams.FLAGS_CHANGED);

                    // Change the screen brightness change mode to manual.
                    Settings.System.putInt(MainActivity.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    // Apply the screen brightness value to the system, this will change the value in Settings ---> Display ---> Brightness level.
                    // It will also change the screen brightness for the device.
                    Settings.System.putInt(MainActivity.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
                }
            }
        });

        final DefaultTrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory());
        final SimpleExoPlayer exoPlayer = ExoPlayerFactory
                .newSimpleInstance(this, new DefaultRenderersFactory(this), trackSelector);
        DefaultHttpDataSourceFactory baseFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(this, getPackageName()), DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS, DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, false);
        String url = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8";
        HlsMediaSource mediaSource = new HlsMediaSource.Factory(new DefaultDataSourceFactory(this, null, baseFactory)).createMediaSource(Uri.parse(url));
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        playerView.setPlayer(exoPlayer);
        playerView.showController();
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        playerView.setKeepScreenOn(true);
        exoLayer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN && isControllerHidden){
                    playerView.showController();
                    isControllerHidden = false;
                }else if(motionEvent.getAction() == MotionEvent.ACTION_DOWN && !isControllerHidden){
                    playerView.hideController();
                    isControllerHidden = true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    playerView.hideController();
                    AdjustVolume(motionEvent);
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speaker.setVisibility(View.GONE);
                }
                return true;
            }
        });
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    Log.d("eprogress", "onPlayerStateChanged: " + playbackState);

                } else {

                    Log.d("eprogress", "onPlayerStateChanged: " + playbackState);
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TrackSelectionView.getDialog(MainActivity.this, "Select Quality", trackSelector, 0).first.show();
            }
        });
    }

    private void changePermission(MainActivity mainActivity) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        mainActivity.startActivity(intent);
    }

    private boolean hasPermission(MainActivity mainActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(mainActivity);
        }
        return false;
    }

    private void AdjustVolume(MotionEvent motionEvent) {
        speaker.setVisibility(View.VISIBLE);
        voiceProgress.setMax(exoLayer.getHeight());
        voiceProgress.setProgress((int) motionEvent.getAxisValue(MotionEvent.AXIS_Y));
        float current = motionEvent.getAxisValue(MotionEvent.AXIS_Y);
        float whole = exoLayer.getHeight();
        float percent = (current / whole) * 100;
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) ((percent * maxVolume) / 100), AudioManager.FLAG_PLAY_SOUND);
        Log.d("volume", "onTouch: " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }
}
