package com.jwplayer.demo.nativecontrols;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.license.LicenseUtil;
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
    final String videoFileName = "http://samplescdn.origin.mediaservices.windows.net/e0e820ec-f6a2-4ea2-afe3-1eed4e06ab2c/AzureMediaServices_Overview.ism/manifest(format=m3u8-aapl-v3)";
    final String streamFileName = "http://playertest.longtailvideo.com/adaptive/wowzaid3/playlist.m3u8";
    final String scrubbaleStreamFileName = "http://playertest.longtailvideo.com/hls/hockey/new_master.m3u8";

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

        LicenseUtil.setLicenseKey(this, YOUR_LICENSE_KEY);

        mPlayerView = findViewById(R.id.jwplayer);

        //Subscribe to any events we're interested in
        mPlayerView.addOnReadyListener(this);
        mNativePlayerControls = new JWPlayerNativeControls(this);
        //Initialize our mNativePlayerControls
        mNativePlayerControls.setJWView(mPlayerView);
        mPlayerView.addView(mNativePlayerControls);
        //Subscribe to the mNativePlayerControls interaction events being emitted
        //by the mNativePlayerControls to automatically fade the mNativePlayerControls out during playback
        mNativePlayerControls.addControlsInteractionListener(this);



        //You can demo stream functionality by using the other file url
        String chosenFile = videoFileName;


        //Setup our player with a demo playlist item so it plays a video
        PlaylistItem playlistItem = new PlaylistItem.Builder()
                .file(chosenFile)
                .title(getString(R.string.demo_video_title))
                .description("A video player testing video.")
                .build();
        List<PlaylistItem> playlist = new ArrayList<>();
        playlist.add(playlistItem);
        PlayerConfig config = new PlayerConfig.Builder().playlist(playlist).build();
        mPlayerView.setup(config);

        //Disable the player's controls so we don't have conflict with our native ones
        mPlayerView.setControls(false);

        //We're going to go full native
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        //So use a big view to intercept and manage clicks to get around some built in webplayer click functionality
        mTouchInterceptorView = new RelativeLayout(this);
        mTouchInterceptorView.setLayoutParams(params);
        mTouchInterceptorView.setOnClickListener(this);
        mTouchInterceptorView.setVisibility(View.INVISIBLE);

        mPlayerView.addView(mTouchInterceptorView);
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
        if(v == mTouchInterceptorView){
            int currentVisibility = mNativePlayerControls.getVisibility();
            if(currentVisibility != View.VISIBLE){
                mNativePlayerControls.setVisibility(View.VISIBLE);
                mTouchInterceptorView.setVisibility(View.INVISIBLE);
                startFadeControlsTimer();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String chosenFile = "";
        String video_title = getString(R.string.demo_video_title);
        switch (item.getItemId()) {
            case R.id.menu_swap_to_live_stream: {
                chosenFile = streamFileName;
                video_title = getString(R.string.demo_live_stream_title);
                break;
            }
            case R.id.menu_swap_to_scrubbale_stream: {
                chosenFile = scrubbaleStreamFileName;
                video_title = getString(R.string.demo_archived_stream_title);
                break;
            }
            case R.id.menu_swap_to_video: {
                chosenFile = videoFileName;
                video_title = getString(R.string.demo_video_title);
                break;
            }
        }
        //Setup our player with a demo playlist item so it plays a video
        PlaylistItem playlistItem = new PlaylistItem.Builder()
                .file(chosenFile)
                .title(video_title)
                .description("Demonstration of playback")
                .build();
        List<PlaylistItem> playlist = new ArrayList<>();
        playlist.add(playlistItem);
        PlayerConfig config = new PlayerConfig.Builder().playlist(playlist).build();
        mPlayerView.stop();
        mNativePlayerControls.resetVideoPlaybackUI();

        //Cancel Timer to hide mNativePlayerControls altogether
        mHideControlsHandler.removeCallbacks(mHideControlsRunnable);
        mTouchInterceptorView.setClickable(false);
        mTouchInterceptorView.setVisibility(View.INVISIBLE);
        mPlayerView.setup(config);
        mPlayerView.setControls(false);
        return true;
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

    @Override
    public void onBackPressed() {
        if(mPlayerView.getFullscreen()){
            mPlayerView.setFullscreen(false,true);
        } else {
            super.onBackPressed();
        }
    }
}
