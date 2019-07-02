package com.jwplayer.demo.movableplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.core.PlayerState;
import com.longtailvideo.jwplayer.events.CompleteEvent;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.PauseEvent;
import com.longtailvideo.jwplayer.events.PlayEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

public class MainActivity extends AppCompatActivity implements VideoPlayerEvents.OnFullscreenListener, VideoPlayerEvents.OnPlayListener, VideoPlayerEvents.OnPauseListener, VideoPlayerEvents.OnCompleteListener {

    /**
     * The scaling factor that is used to calculate the movable player's width and height.
     */
    private static final double SCALING_FACTOR = 1.75;

    private Button mMovablePlayerToggle;
    private FrameLayout mButtonContainer;
    private JWPlayerView mPlayerView;
    private MovableFrameLayout mPlayerContainer;
    private CoordinatorLayout mCoordinatorLayout;
    private RelativeLayout mContentContainer;

    private ViewGroup.LayoutParams mInitialLayoutParams;

    private PlayerState mPlayerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a reference to the CoordinatorLayout
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mContentContainer = (RelativeLayout) findViewById(R.id.main_container);

        // Get a reference to the FrameLayout that we will use to contain the player in.
        mPlayerContainer = (MovableFrameLayout) findViewById(R.id.player_container);
        mButtonContainer = (FrameLayout) findViewById(R.id.button_container);
        mMovablePlayerToggle = (Button) findViewById(R.id.movable_player_toggle);
        mMovablePlayerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        mButtonContainer.getLayoutParams();
                if (!mPlayerContainer.isMovable()) {
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                } else {
                    layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                }
                mButtonContainer.setLayoutParams(layoutParams);
                toggleMovablePlayer();
            }
        });

        // Initialize a new JW Player.
        mPlayerView = new JWPlayerView(this, new PlayerConfig.Builder()
                .file("https://tungsten.aaplimg.com/VOD/bipbop_adv_example_v2/master.m3u8")
                .build());
        mPlayerView.addOnFullscreenListener(this);
        mPlayerView.addOnPlayListener(this);
        mPlayerView.addOnPauseListener(this);
        mPlayerView.addOnCompleteListener(this);

        // Add the View to the View Hierarchy.
        mPlayerContainer.addView(mPlayerView);
        setInitialLayoutParams();
    }

    /**
     * Toggles the movable player:
     *
     * If the player is not in a movable state:
     * - Disable player controls, since the skin does not scale nicely to smaller views
     *      (you could create a custom skin for this purpose).
     * - Create a 'copy' of the current Layout Parameters of the PlayerContainer.
     * - Resize the player.
     * - Set an 'onTouchListener' on the container containing the player.
     *
     *
     * Else:
     * - Re-enable the player controls.
     * - Remove the 'onTouchListener'.
     * - Reset the Layout Parameters.
     */
    private void toggleMovablePlayer() {
        if (!mPlayerContainer.isMovable()) {
            // Set the player container to movable, in order to intercept touch events.
            mPlayerContainer.setMovable(true);

            // Disable fullscreen rotation handling on the JW Player.
            mPlayerView.setFullscreen(mPlayerView.getFullscreen(), false);

            // Disable controls.
            mPlayerView.setControls(false);
            if (mPlayerState != PlayerState.PLAYING && mPlayerState != PlayerState.BUFFERING) {
                // Start playback in case the user hasn't done this yet, since we don't want to have
                // a movable player that does not play anything...
                mPlayerView.play();
            }

            // Scale the player.
            mInitialLayoutParams = mPlayerContainer.getLayoutParams();
            int newWidth = (int) (mPlayerContainer.getWidth() / SCALING_FACTOR);
            int newHeight = (int) (mPlayerContainer.getHeight() / SCALING_FACTOR);
            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(newWidth, newHeight);

            // Position the player in the right bottom corner.
            mPlayerContainer.setLayoutParams(getInitialMovablePlayerLayoutParams(layoutParams));

            // Set an onTouchListener on the player which handles MotionEvents.
            mPlayerContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (v.getId() == R.id.player_container) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                // Notify the MovablePlayerLayout that we started consuming
                                // events in order to receive ACTION_MOVE events.
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                int topMargin = (int) event.getRawY() - v.getHeight();
                                int leftMargin = (int) event.getRawX() - (v.getWidth() / 2);

                                // Make sure that the view can not go "out of bounds"
                                if (topMargin < 0) {
                                    // Out of bounds: TOP
                                    topMargin = 0;
                                }
                                if (topMargin > mContentContainer.getHeight() - mPlayerContainer.getHeight()) {
                                    // Out of bounds: BOTTOM
                                    topMargin = mContentContainer.getHeight() - mPlayerContainer.getHeight();
                                }
                                if (leftMargin < 0) {
                                    // Out of bounds: LEFT
                                    leftMargin = 0;
                                }
                                if (leftMargin > mContentContainer.getWidth() - mPlayerContainer.getWidth()) {
                                    // Out of bounds: RIGHT
                                    leftMargin = mContentContainer.getWidth() - mPlayerContainer.getWidth();
                                }

                                layoutParams.topMargin = topMargin;
                                layoutParams.leftMargin = leftMargin;

                                // Make sure the align rules have been removed.
                                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                                layoutParams.rightMargin = 0;
                                layoutParams.bottomMargin = 0;
                                // Set the new layout parameters
                                v.setLayoutParams(layoutParams);
                                return true;
                        }
                    }
                    return false;
                }
            });
        } else {
            // Disable the movable property of the MovableViewLayout.
            mPlayerContainer.setMovable(false);
            // Restore the initial layout parameters.
            mPlayerContainer.setLayoutParams(mInitialLayoutParams);
            // Remove the onTouchListener.
            mPlayerContainer.setOnTouchListener(null);
            // Re-enable the controls.
            mPlayerView.setControls(true);
            // Re-enable fullscreen rotation handling, and go to fullscreen if we're in landscape mode.
            mPlayerView.setFullscreen(getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE, true);
        }
    }

    /**
     * Sets the initial layout parameters for the {@link JWPlayerView}.
     */
    private void setInitialLayoutParams() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mPlayerContainer.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (displayMetrics.widthPixels / 16) * 9)); // 16:9
        } else {
            // We need to use height to calculate a 16:9 ratio since we're in landscape mode.
            mPlayerContainer.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (displayMetrics.heightPixels / 16) * 9)); // 16:9
            // Toggle fullscreen, since we're in landscape mode.
            mPlayerView.setFullscreen(true, true);
        }
    }

    /**
     * Positions the movable player to the right bottom corner.
     *
     * @param layoutParams
     * @return
     */
    private RelativeLayout.LayoutParams getInitialMovablePlayerLayoutParams(RelativeLayout.LayoutParams layoutParams) {
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        layoutParams.setMargins(0, 0, Math.round(displayMetrics.density * 16), Math.round(displayMetrics.density * 16));
        return layoutParams;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (!mPlayerContainer.isMovable()) {
            // Set fullscreen when the device is rotated to landscape, and not in movable player mode.
            mPlayerView.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE, true);
        } else {
            // When we rotate and the player is in movable mode, reset it's position.
            mPlayerContainer.setLayoutParams(getInitialMovablePlayerLayoutParams(
                    (RelativeLayout.LayoutParams) mPlayerContainer.getLayoutParams()));
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        // Let JW Player know that the app has returned from the background
        mPlayerView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Let JW Player know that the app is going to the background
        mPlayerView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Let JW Player know that the app is being destroyed
        mPlayerView.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // If we are in fullscreen mode, exit fullscreen mode when the user uses the back button.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPlayerView.getFullscreen()) {
                mPlayerView.setFullscreen(false, true);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onComplete(CompleteEvent completeEvent) {
        mPlayerState = PlayerState.IDLE;
    }

    /**
     * Handles JW Player going to and returning from fullscreen by hiding the ActionBar
     *
     * @param fullscreenEvent has a getFullscreen which return s true if the player is fullscreen
     */
    @Override
    public void onFullscreen(FullscreenEvent fullscreenEvent) {
        boolean fullscreen = fullscreenEvent.getFullscreen();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (fullscreen) {
                /*
                 * Because we are using a coordinator layout, which has setFitsSystemWindows set
                 * to 'true' by default, we need to disable this when the JW Player View goes into
                 * fullscreen. If we would not do this, the activity will not properly hide the
                 * system UI.
                 */
                mCoordinatorLayout.setFitsSystemWindows(false);
                actionBar.hide();
                mMovablePlayerToggle.setVisibility(View.GONE);
            } else {
                mCoordinatorLayout.setFitsSystemWindows(true);
                actionBar.show();
                mMovablePlayerToggle.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPause(PauseEvent pauseEvent) {
        mPlayerState = pauseEvent.getOldState();
    }

    @Override
    public void onPlay(PlayEvent playEvent) {
        mPlayerState = playEvent.getOldState();
    }
}
