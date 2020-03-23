package com.jwplayer.googledai;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.events.AdErrorEvent;
import com.longtailvideo.jwplayer.events.listeners.AdvertisingEvents;
import com.longtailvideo.jwplayer.media.ads.dai.ImaDaiSettings;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AdvertisingEvents.OnAdErrorListener{

    JWPlayerView mPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayerView = findViewById(R.id.jwplayer);

        List<PlaylistItem> playlist = new ArrayList<>();
        String videoId = "bbb-clear";
        String cmsId = "2474148";
        ImaDaiSettings.StreamType streamType = ImaDaiSettings.StreamType.DASH;
        ImaDaiSettings imaDaiSettings = new ImaDaiSettings(videoId, cmsId, streamType, null);
        String fallbackUrl = "http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8";

        PlaylistItem playlistItem = new PlaylistItem.Builder()
                .file(fallbackUrl)
                .imaDaiSettings(imaDaiSettings)
                .title("Google DAI Demo")
                .description("A video player testing video.")
                .build();

        playlist.add(playlistItem);
        PlayerConfig config = new PlayerConfig.Builder().playlist(playlist).build();

        mPlayerView.setup(config);
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
    }

    @Override
    public void onBackPressed() {
        if (mPlayerView.getFullscreen()) {
            mPlayerView.setFullscreen(false, true);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        Log.e("DaiAdError", adErrorEvent.getMessage());
    }
}
