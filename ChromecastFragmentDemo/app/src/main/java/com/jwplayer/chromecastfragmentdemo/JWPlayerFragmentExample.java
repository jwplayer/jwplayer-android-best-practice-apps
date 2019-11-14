package com.jwplayer.chromecastfragmentdemo;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.longtailvideo.jwplayer.JWPlayerSupportFragment;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class JWPlayerFragmentExample extends AppCompatActivity {

    /**
     * A reference to the {@link JWPlayerSupportFragment}.
     */
    private JWPlayerSupportFragment mPlayerFragment;

    /**
     * A reference to the {@link JWPlayerView} used by the JWPlayerSupportFragment.
     */
    private JWPlayerView mPlayerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwplayerfragment);

        List<PlaylistItem> playlist = new ArrayList<>();
        playlist.add(new PlaylistItem.Builder()
                             .file("https://cdn.jwplayer.com/manifests/wLT8wkeE.m3u8")
                             .mediaId("wLT8wkeE")
                             .image("https://content.jwplatform.com/thumbs/wLT8wkeE-720.jpg")
                             .build());

        // Construct a new JWPlayerSupportFragment (since we're using AppCompatActivity)
        mPlayerFragment = JWPlayerSupportFragment.newInstance(new PlayerConfig.Builder()
                .playlist(playlist)
                .build());

        // Attach the Fragment to our layout
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fragment_container, mPlayerFragment);
        ft.commit();

        // Make sure all the pending fragment transactions have been completed, otherwise
        // mPlayerFragment.getPlayer() may return null
        fm.executePendingTransactions();

        // Get a reference to the JWPlayerView from the fragment
        mPlayerView = mPlayerFragment.getPlayer();

        // Keep the screen on during playback
        new KeepScreenOnHandler(mPlayerView, getWindow());

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Exit fullscreen when the user pressed the Back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPlayerView.getFullscreen()) {
                mPlayerView.setFullscreen(false, true);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_jwplayerfragment, menu);

        // Register the MediaRouterButton
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
                R.id.media_route_menu_item);

        return true;
    }

}
