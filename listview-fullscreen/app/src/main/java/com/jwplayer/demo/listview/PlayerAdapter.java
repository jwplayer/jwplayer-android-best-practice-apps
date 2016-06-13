package com.jwplayer.demo.listview;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.configuration.Skin;
import com.longtailvideo.jwplayer.fullscreen.FullscreenHandler;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.HashSet;

public class PlayerAdapter extends BaseAdapter {

    private static final String SKIN_URL = "file:///android_asset/listview-skin.css";

    private Activity mActivity;
    private PlaylistItem[] mPlaylist;
    private LayoutInflater mLayoutInflater;
    private View mDecorView;
    private ViewGroup mRootView;
    private HashSet<JWPlayerView> mPlayerViews = new HashSet<>();
    private JWPlayerView mFullscreenPlayer;

    public PlayerAdapter(Activity activity, ListView listView, PlaylistItem[] listItems) {
        mActivity = activity;
        mPlaylist = listItems;
        mLayoutInflater = LayoutInflater.from(activity);
        listView.setAdapter(this);
        mDecorView = activity.getWindow().getDecorView();
        mRootView = (ViewGroup) activity.findViewById(android.R.id.content);
    }

    @Override
    public int getCount() {
        return mPlaylist.length;
    }

    @Override
    public Object getItem(int position) {
        return mPlaylist[position];
    }

    @Override
    public long getItemId(int position) {
        return mPlaylist[position].hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (isFullscreen()) {
            // Don't bother doing anything if list items won't be visible anyway.
            return convertView;
        }

        if (convertView == null) {
            // We need to instantiate a new view.
            convertView = mLayoutInflater.inflate(R.layout.list_item, null);
        }

        // Get references to the UI elements
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView description = (TextView) convertView.findViewById(R.id.description);
        final FrameLayout playerContainer = (FrameLayout) convertView.findViewById(R.id.player_container);
        final ImageView placeholder = (ImageView) playerContainer.findViewById(R.id.player_placeholder);

        // Update the UI
        final PlaylistItem playlistItem = (PlaylistItem) getItem(position);
        title.setText(playlistItem.getTitle());
        description.setText(playlistItem.getDescription());

        // Check if there is already a JWPlayerView present in the convertView.
        // If there is, then we want to recycle it, because player instantiation is quite expensive.
        JWPlayerView player = null;
        for (int i = 0; i < playerContainer.getChildCount(); i++) {
            if (playerContainer.getChildAt(i) instanceof JWPlayerView) {
                // Recycle the JWPlayerView
                player = (JWPlayerView) playerContainer.getChildAt(i);
                if (player.getPlaylistItem(player.getPlaylistIndex()) != playlistItem) {
                    // Make sure the player has been released.
                    player.stop();
                    // Hide the player, it will be shown again when a user interacts with the ImageView.
                    player.setVisibility(View.GONE);
                }
            }
        }

        final JWPlayerView finalPlayer = player;
        placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // As soon as a user interacts with the ImageView, swap it out for the JW Player.
                // We're using ImageView's in order to optimize ListView performance.
                JWPlayerView playerView;
                if (finalPlayer == null) {
                    playerView = new JWPlayerView(mActivity, new PlayerConfig.Builder().build());
                    playerView.setFullscreenHandler(new ListViewFullscreenHandler(
                            playerContainer, playerView));
                    playerView.setSkin(SKIN_URL);
                    mPlayerViews.add(playerView);
                    playerContainer.addView(playerView);
                } else {
                    // Recycle an existing player.
                    playerView = finalPlayer;
                    playerView.setVisibility(View.VISIBLE);
                }
                playerView.load(playlistItem);
                playerView.play(true);
            }
        });

        return convertView;
    }

    /*
     * Life cycle methods
     */

    public void onPause() {
        for (JWPlayerView playerView : mPlayerViews) {
            playerView.onPause();
        }
    }

    public void onResume() {
        for (JWPlayerView playerView : mPlayerViews) {
            playerView.onResume();
        }
    }

    public void onDestroy() {
        for (JWPlayerView playerView : mPlayerViews) {
            playerView.onDestroy();
        }
    }

    /**
     * Returns whether a player is in fullscreen mode.
     * @return true if a player is in fullscreen mode.
     */
    public boolean isFullscreen() {
        return mFullscreenPlayer != null;
    }

    /**
     * Handles the back button pressed event.
     */
    public void onBackPressed() {
        if (mFullscreenPlayer != null) {
            mFullscreenPlayer.setFullscreen(false, false);
        }
    }

    /**
     * Shows the system ui.
     */
    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * Hides the system ui.
     */
    private void hideSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * A FullscreenHandler for JWPlayerView's in a ListView.
     */
    private class ListViewFullscreenHandler implements FullscreenHandler {

        private ViewGroup mPlayerContainer;
        private JWPlayerView mPlayerView;

        public ListViewFullscreenHandler(ViewGroup playerContainer, JWPlayerView playerView) {
            mPlayerContainer = playerContainer;
            mPlayerView = playerView;
        }

        @Override
        public void onFullscreenRequested() {
            // Hide system ui
            hideSystemUI();

            // Enter landscape mode for fullscreen videos
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            // Destroy the player's rendering surface, we need to do this to prevent Android's
            // MediaDecoders from crashing.
            mPlayerView.destroySurface();

            // Remove the JWPlayerView from the list item.
            mPlayerContainer.removeView(mPlayerView);

            // Use the JW7 skin in fullscreen.
            mPlayerView.setSkin(Skin.SEVEN);

            // Initialize a new rendering surface.
            mPlayerView.initializeSurface();

            // Add the JWPlayerView to the RootView as soon as the UI thread is ready.
            mRootView.post(new Runnable() {
                @Override
                public void run() {
                    mRootView.addView(mPlayerView, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    mFullscreenPlayer = mPlayerView;
                }
            });
        }

        @Override
        public void onFullscreenExitRequested() {
            // Show the system ui
            showSystemUI();

            // Enter portrait mode
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // Destroy the surface that is used for video output, we need to do this before
            // we can detach the JWPlayerView from a ViewGroup.
            mPlayerView.destroySurface();

            // Remove the player view from the root ViewGroup.
            mRootView.removeView(mPlayerView);

            // After we've detached the JWPlayerView we can safely reinitialize the surface.
            mPlayerView.initializeSurface();

            // Restore the listview-skin
            mPlayerView.setSkin(SKIN_URL);

            // As soon as the UI thread has finished processing the current message queue it
            // should add the JWPlayerView back to the list item.
            mPlayerContainer.post(new Runnable() {
                @Override
                public void run() {
                    mPlayerContainer.addView(mPlayerView, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    mFullscreenPlayer = null;
                }
            });
        }

        @Override
        public void onResume() {
            // Do nothing, we're not listening for device rotation changes.
        }

        @Override
        public void onPause() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public void onAllowRotationChanged(boolean allowRotation) {

        }

        @Override
        public void updateLayoutParams(ViewGroup.LayoutParams layoutParams) {

        }

        @Override
        public void setUseFullscreenLayoutFlags(boolean useFlags) {

        }
    }
}
