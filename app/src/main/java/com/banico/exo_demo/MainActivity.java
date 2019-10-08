package com.banico.exo_demo;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
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
    GestureDetector gestureDetector;
    GestureDetector simpleDetector;
    FrameLayout exoLayer;
    SimpleExoPlayer exoPlayer;
    ImageView speaker;
    boolean isControllerHidden = false;

    enum Direction {
        up,
        down,
        left,
        right
    }

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
        voiceProgress.setMax(100);
        voiceProgress.setProgress(50);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                if(isControllerHidden){
                    playerView.showController();
                    isControllerHidden = false;
                }else {
                    playerView.hideController();
                    isControllerHidden = true;
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
                double angle = getAngle(motionEvent.getX(), motionEvent.getY(), motionEvent1.getX(), motionEvent1.getY());
                Direction direction = fromAngle(angle);
                switch (direction) {
                    case up:
                        voiceProgress.setVisibility(View.VISIBLE);
                        speaker.setVisibility(View.VISIBLE);
                        voiceProgress.setProgress(voiceProgress.getProgress() + 1);
                        exoPlayer.setVolume(exoPlayer.getVolume() + 0.01f);
                        Toast.makeText(MainActivity.this, "volume: " +  exoPlayer.getVolume(), Toast.LENGTH_LONG).show();
                        break;
                    case down:
                        voiceProgress.setVisibility(View.VISIBLE);
                        speaker.setVisibility(View.VISIBLE);
                        voiceProgress.setProgress(voiceProgress.getProgress() - 1);
                        exoPlayer.setVolume(exoPlayer.getVolume() - 0.01f);
                        Toast.makeText(MainActivity.this, "volume: " +  exoPlayer.getVolume(), Toast.LENGTH_LONG).show();
                        break;
                    case right:
                        Toast.makeText(MainActivity.this, "go right", Toast.LENGTH_LONG).show();
                        break;
                    case left:
                        Toast.makeText(MainActivity.this, "go left", Toast.LENGTH_LONG).show();
                        break;
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velocityX, float velocityY) {
                return false;
            }
        });
        simpleDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                double angle = getAngle(e1.getX(), e1.getY(), e2.getX(), e2.getY());
                Direction direction = fromAngle(angle);
                switch (direction) {
                    case up:
                        voiceProgress.setVisibility(View.VISIBLE);
                        speaker.setVisibility(View.VISIBLE);
                        voiceProgress.setProgress(voiceProgress.getProgress() + 1);
                        exoPlayer.setVolume(exoPlayer.getVolume() + 0.1f);
                        Toast.makeText(MainActivity.this, "go up", Toast.LENGTH_LONG).show();
                        break;
                    case down:
                        voiceProgress.setVisibility(View.VISIBLE);
                        speaker.setVisibility(View.VISIBLE);
                        voiceProgress.setProgress(voiceProgress.getProgress() - 1);
                        exoPlayer.setVolume(exoPlayer.getVolume() - 0.1f);
                        Toast.makeText(MainActivity.this, "go down", Toast.LENGTH_LONG).show();
                        break;
                    case right:
                        Toast.makeText(MainActivity.this, "go right", Toast.LENGTH_LONG).show();
                        break;
                    case left:
                        Toast.makeText(MainActivity.this, "go left", Toast.LENGTH_LONG).show();
                        break;
                }
                return true;
            }
        });
        ImageView brightness = findViewById(R.id.brightness);
        brightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WindowManager.LayoutParams attributes = getWindow().getAttributes();
                attributes.screenBrightness = 0F;
                getWindow().setAttributes(attributes);
                getWindow().addFlags(WindowManager.LayoutParams.FLAGS_CHANGED);
            }
        });

        final DefaultTrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory());
        exoPlayer = ExoPlayerFactory
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
                gestureDetector.onTouchEvent(motionEvent);
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    voiceProgress.setVisibility(View.GONE);
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
        exoPlayer.setVolume(0.5f);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TrackSelectionView.getDialog(MainActivity.this, "Select Quality", trackSelector, 0).first.show();
            }
        });
    }

    private double getAngle(float x, float y, float x2, float y2) {
        double rad = Math.atan2(y-y2,x2-x) + Math.PI;
        return (rad*180/Math.PI + 180)%360;
    }

    public static Direction fromAngle(double angle) {
        Log.d("anggle","angel: " + angle);
        if (inRange(angle, 45, 135)) {
            return Direction.up;
        } else if (inRange(angle, 0, 45) || inRange(angle, 315, 360)) {
            return Direction.right;
        } else if (inRange(angle, 225, 315)) {
            return Direction.down;
        } else {
            return Direction.left;
        }
    }

    private static boolean inRange(double angle, int start, int end) {
        return angle >= start && angle < end;
    }
}
