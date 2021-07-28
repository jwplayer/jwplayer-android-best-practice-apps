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

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.fullscreen.FullscreenHandler;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PlayerAdapter extends BaseAdapter {

    private Activity mActivity;
    private PlaylistItem[] mPlaylist;
    private LayoutInflater mLayoutInflater;
    private View mDecorView;
    private ViewGroup mRootView;
    private HashSet<JWPlayerView> mPlayerViews = new HashSet<>();
    private JWPlayer mFullscreenPlayer;

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

        List<PlaylistItem> playlist = new ArrayList<>();
        playlist.add(playlistItem);
        final PlayerConfig playerConfig = new PlayerConfig.Builder().playlist(playlist).build();

        // Check if there is already a JWPlayerView present in the convertView.
        // If there is, then we want to recycle it, because player instantiation is quite expensive.
        JWPlayerView playerView = null;
        for (int i = 0; i < playerContainer.getChildCount(); i++) {
            if (playerContainer.getChildAt(i) instanceof JWPlayerView) {
                // Recycle the JWPlayerView
                playerView = (JWPlayerView) playerContainer.getChildAt(i);
                JWPlayer player = playerView.getPlayer();
                if (player.getPlaylistItem(player.getPlaylistIndex()) != playlistItem) {
                    // Make sure the player has been released.
                    player.stop();
                    // Hide the player, it will be shown again when a user interacts with the ImageView.
                    playerView.setVisibility(View.GONE);
                }
            }
        }

        final JWPlayerView finalPlayer = playerView;
        placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // As soon as a user interacts with the ImageView, swap it out for the JW Player.
                // We're using ImageView's in order to optimize ListView performance.
                JWPlayerView playerView;
                JWPlayer player;
                if (finalPlayer == null) {
                    playerView = new JWPlayerView(mActivity, null);
                    player = playerView.getPlayer();
                    player.setFullscreenHandler(new ListViewFullscreenHandler(
                            playerContainer, playerView));
                    mPlayerViews.add(playerView);
                    playerContainer.addView(playerView);
                } else {
                    // Recycle an existing player.
                    playerView = finalPlayer;
                    player = playerView.getPlayer();
                    playerView.setVisibility(View.VISIBLE);
                }
                player.setup(playerConfig);
                player.play();
            }
        });

        return convertView;
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

            // Remove the JWPlayerView from the list item.
            mPlayerContainer.removeView(mPlayerView);

            // Add the JWPlayerView to the RootView as soon as the UI thread is ready.
            mRootView.post(new Runnable() {
                @Override
                public void run() {
                    mRootView.addView(mPlayerView, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    mFullscreenPlayer = mPlayerView.getPlayer();
                }
            });
        }

        @Override
        public void onFullscreenExitRequested() {
            // Show the system ui
            showSystemUI();

            // Enter portrait mode
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // Remove the player view from the root ViewGroup.
            mRootView.removeView(mPlayerView);

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
