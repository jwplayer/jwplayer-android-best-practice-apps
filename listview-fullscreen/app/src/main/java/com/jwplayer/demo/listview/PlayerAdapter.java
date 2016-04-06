package com.jwplayer.demo.listview;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.Skin;
import com.longtailvideo.jwplayer.fullscreen.FullscreenHandler;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.HashSet;

public class PlayerAdapter extends BaseAdapter {

    private static final String SKIN_URL = "file:///android_asset/listview-skin.css";

    private Activity mActivity;
    private ListView mListView;
    private PlaylistItem[] mPlaylist;
    private LayoutInflater mLayoutInflater;
    private View mDecorView;
    private ViewGroup mRootView;
    private HashSet<JWPlayerView> mPlayerViews = new HashSet<>();
    private ViewGroup.LayoutParams mLayoutParams;
    private JWPlayerView mFullscreenedPlayer;

    public PlayerAdapter(Activity activity, ListView listView, PlaylistItem[] listItems) {
        mActivity = activity;
        mListView = listView;
        mPlaylist = listItems;
        mLayoutInflater = LayoutInflater.from(activity);
        mListView.setAdapter(this);
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
            // This prevents a call to load() on the JWPlayerView for every list item in the
            // list that is not visible, which greatly improves performance.
            return convertView;
        }

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_item, null);
        }

        final ViewGroup listItem = (ViewGroup) convertView;
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView description = (TextView) convertView.findViewById(R.id.description);

        final JWPlayerView playerView = (JWPlayerView) convertView.findViewById(R.id.player);
        if (playerView == null) {
            // If the playerView is null, then we are in fullscreen mode.
            // In that case it doesn't really matter what we return since it won't be visible anyway.
            return convertView;
        }

        mPlayerViews.add(playerView);
        PlaylistItem playlistItem = (PlaylistItem) getItem(position);
        title.setText(playlistItem.getTitle());
        description.setText(playlistItem.getDescription());
        playerView.setSkin(SKIN_URL);
        playerView.setFullscreenHandler(new FullscreenHandler() {
            @Override
            public void onFullscreenRequested() {
                // Hide system ui
                hideSystemUI();

                // Enter landscape mode for fullscreen videos
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                // Keep a copy of the layout params. We'll use this to restore the player when returning
                // from fullscreen.
                mLayoutParams = playerView.getLayoutParams();

                // Destroy the player's rendering surface, we need to do this to prevent Android's
                // MediaDecoders from crashing.
                playerView.destroySurface();

                // Remove the JWPlayerView from the list item.
                listItem.removeView(playerView);

                // Use the JW7 skin in fullscreen.
                playerView.setSkin(Skin.SEVEN);

                // Initialize a new rendering surface.
                playerView.initializeSurface();

                // Add the JWPlayerView to the RootView as soon as the UI thread is ready.
                mRootView.post(new Runnable() {
                    @Override
                    public void run() {
                        mRootView.addView(playerView, new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        mFullscreenedPlayer = playerView;
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
                playerView.destroySurface();

                // Remove the player view from the root ViewGroup.
                mRootView.removeView(playerView);

                // After we've detached the JWPlayerView we can safely reinitialize the surface.
                playerView.initializeSurface();

                // Restore the listview-skin
                playerView.setSkin(SKIN_URL);

                // As soon as the UI thread has finished processing the current message queue it
                // should add the JWPlayerView back to the list item.
                listItem.post(new Runnable() {
                    @Override
                    public void run() {
                        listItem.addView(playerView, mLayoutParams);
                        mFullscreenedPlayer = null;
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
        });
        playerView.load(playlistItem);
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
        return mFullscreenedPlayer != null;
    }

    /**
     * Handles the back button pressed event.
     */
    public void onBackPressed() {
        if (mFullscreenedPlayer != null) {
            mFullscreenedPlayer.setFullscreen(false, false);
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
}
