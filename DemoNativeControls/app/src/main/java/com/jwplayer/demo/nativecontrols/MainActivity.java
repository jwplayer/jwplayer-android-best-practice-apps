package com.jwplayer.demo.nativecontrols;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.configuration.UiConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.ReadyEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
                   VideoPlayerEvents.OnReadyListener,
                   JWPlayerNativeControls.OnControlsInteraction {

    final Handler mHideControlsHandler = new Handler();
    final long mDelayBeforeControlsAreHidden = 3000;
    final String videoFileName = "https://content.jwplatform.com/manifests/1sc0kL2N.m3u8";
    final String streamFileName = "https://playertest.longtailvideo.com/adaptive/wowzaid3/playlist.m3u8";
    final String scrubbaleStreamFileName = "https://playertest.longtailvideo.com/hls/hockey/new_master.m3u8";
    JWPlayerView mPlayerView;
    JWPlayer mPlayer;
    JWPlayerNativeControls mNativePlayerControls;
    RelativeLayout mTouchInterceptorView;
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

        // INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
        // [OR] change in app-level build.gradle
        // [OR] set JWPLAYER_LICENSE_KEY as environment variable
        LicenseUtil.setLicenseKey(this,BuildConfig.JWPLAYER_LICENSE_KEY );

        mPlayerView = findViewById(R.id.jwplayer);
        mPlayer = mPlayerView.getPlayer();

        //Subscribe to any events we're interested in
        mPlayer.addListener(EventType.READY, this);
        mNativePlayerControls = new JWPlayerNativeControls(this);
        //Initialize our mNativePlayerControls
        mNativePlayerControls.setJWView(mPlayer);
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
        PlayerConfig config = new PlayerConfig.Builder()
                .uiConfig(new UiConfig.Builder()
                                  .hideAllControls()
                                  .build())
                .playlist(playlist)
                .build();

        mPlayer.setup(config);

        //Disable the player's controls so we don't have conflict with our native ones
        mPlayer.setControls(false);

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
    protected void onDestroy() {
        super.onDestroy();
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
        mPlayer.stop();
        mNativePlayerControls.resetVideoPlaybackUI();

        //Cancel Timer to hide mNativePlayerControls altogether
        mHideControlsHandler.removeCallbacks(mHideControlsRunnable);
        mTouchInterceptorView.setClickable(false);
        mTouchInterceptorView.setVisibility(View.INVISIBLE);
        mPlayer.setup(config);
        mPlayer.setControls(false);
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
        if(mPlayer.getFullscreen()){
            mPlayer.setFullscreen(false,true);
        } else {
            super.onBackPressed();
        }
    }
}
