package com.jwplayer.opensourcedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;

public class JWPlayerViewExample extends AppCompatActivity {

    private JWPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JWPlayerView mPlayerView = findViewById(R.id.demoJWPlayerView);
        mPlayerView.getPlayerAsync(this, (JWPlayer player) -> {
            mPlayer = player;

            setupPlayer();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPlayer != null) {
            mPlayer.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mPlayer != null) {
            mPlayer.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.onDestroy();
        }
    }

    private void setupPlayer() {

        ArrayList<PlaylistItem> playlist = new ArrayList<PlaylistItem>();

        // Load a media source
        PlaylistItem pi = new PlaylistItem.Builder()
                .file("https://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8")
                .title("BipBop")
                .description("A video player testing video.")
                .build();

        playlist.add(pi);

        PlayerConfig config = new PlayerConfig.Builder()
                .playlist(playlist)
                .build();

        mPlayer.setup(config);
    }
}