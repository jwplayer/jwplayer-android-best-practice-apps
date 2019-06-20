package com.jwplayer.demo.nativecontrols;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.events.ReadyEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
        VideoPlayerEvents.OnReadyListener,
        JWPlayerNativeControls.OnControlsInteraction {
    JWPlayerView mPlayerView;
    JWPlayerNativeControls mNativePlayerControls;
    RelativeLayout mTouchInterceptorView;

    final Handler mHideControlsHandler = new Handler();
    final long mDelayBeforeControlsAreHidden = 3000;

    //The runnable that will be responsible for hiding mNativePlayerControls
    //This will trigger if the video is playing and the user doesn't interact with the screen
    Runnable mHideControlsRunnable = new Runnable() {
        @Override
        public void run() {
            //Hide our native controls
            mNativePlayerControls.setVisibility(View.INVISIBLE);
            enableTouchInterceptor();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayerView = findViewById(R.id.jwplayer);

        //Subscribe to any events we're interested in
        mPlayerView.addOnReadyListener(this);

        mNativePlayerControls = findViewById(R.id.player_control_view);
        //Initialize our mNativePlayerControls
        mNativePlayerControls.setJWView(mPlayerView);
        //Subscribe to the mNativePlayerControls interaction events being emitted
        //by the mNativePlayerControls to automatically fade the mNativePlayerControls out during playback
        mNativePlayerControls.addControlsInteractionListener(this);

        //Setup our player with a demo playlist item so it plays a video
        PlaylistItem playlistItem = new PlaylistItem.Builder()
                .file("http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8")
                .title("BipBop")
                .description("A video player testing video.")
                .build();
        List<PlaylistItem> playlist = new ArrayList<>();
        playlist.add(playlistItem);
        PlayerConfig config = new PlayerConfig.Builder().playlist(playlist).build();
        mPlayerView.setup(config);

        //Disable the player's controls so we don't have conflict with our native ones
        mPlayerView.setControls(false);

        //We're going to go full native
        //So use a big view to intercept and manage clicks to get around some built in webplayer click functionality
        mTouchInterceptorView = findViewById(R.id.touch_interceptor_relative_view);
        mTouchInterceptorView.setOnClickListener(this);
        mTouchInterceptorView.setVisibility(View.INVISIBLE);

        //Hide the controls initially
        //If we encounter a load error publicly display that
        //Bring back the controls in the onReady
        mNativePlayerControls.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayerView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlayerView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayerView.onDestroy();
        mNativePlayerControls.unSubscribeFromJWEvents();
        mNativePlayerControls.removeControlsInteractionListener(this);
    }

    @Override
    public void onClick(View v) {
        //If we click on the touch interceptor and it's invisible
        //make it visible so we can see the native controls again
        //And set a delayed event to hide the native controls again if the user does nothing
        if(v.getId() == R.id.touch_interceptor_relative_view){
            int currentVisibility = mNativePlayerControls.getVisibility();
            if(currentVisibility != View.VISIBLE){
                mNativePlayerControls.setVisibility(View.VISIBLE);
                mTouchInterceptorView.setVisibility(View.INVISIBLE);
                startFadeControlsTimer();
            }
        }
    }

    //Controls Interaction Listener Events

    //When mNativePlayerControls are interacted with we want to handle them
    //automatically fading them out after a couple seconds for a smoother view experience
    @Override
    public void OnControlsInteractedWith(boolean stoppedPlayback) {
        //If the mNativePlayerControls were interacted with reset the timer
        mHideControlsHandler.removeCallbacks(mHideControlsRunnable);

        if(!stoppedPlayback){
            startFadeControlsTimer();
        }
    }

    //When a prolonged interaction begins just cancel the fading until the interaction completes
    @Override
    public void onProlongedInteractionBegan() {
        //Cancel Timer to hide mNativePlayerControls altogether
        mHideControlsHandler.removeCallbacks(mHideControlsRunnable);
    }

    //Only resume the callback to fade out the mNativePlayerControls if at the end of
    //the prolonged interaction the video is actually playing
    @Override
    public void onProlongedInteractionEnded() {
        startFadeControlsTimer();
    }

    @Override
    public void onReady(ReadyEvent readyEvent) {
        //we're ready, the license key was accepted, let's bring up the controls
        mNativePlayerControls.setVisibility(View.VISIBLE);
    }

    private void startFadeControlsTimer(){
        mHideControlsHandler.postDelayed(mHideControlsRunnable,mDelayBeforeControlsAreHidden);
    }

    private void enableTouchInterceptor(){
        mTouchInterceptorView.setClickable(true);
        mTouchInterceptorView.setVisibility(View.VISIBLE);
        mTouchInterceptorView.bringToFront();
    }
}
