package com.jwplayer.googledai;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.AdErrorEvent;
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.ads.dai.ImaDaiSettings;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AdvertisingEvents.OnAdErrorListener{

    JWPlayerView mPlayerView;
    JWPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LicenseUtil.setLicenseKey(this, YOUR_LICENSE_KEY);

        mPlayerView = findViewById(R.id.jwplayer);
        mPlayer = mPlayerView.getPlayer();

        List<PlaylistItem> playlist = new ArrayList<>();
        //videoId: Identifier of the DAI video to be displayed, used for video on demand
        //cmsId: Content management system ID of the video, used for video on demand
        String videoId = "tears-of-steel";
        String cmsId = "2528370";
        //streamType: Defines the type of stream to use
        ImaDaiSettings.StreamType streamType = ImaDaiSettings.StreamType.HLS;
        ImaDaiSettings imaDaiSettings = new ImaDaiSettings(videoId, cmsId, streamType, null);
        //fallbackUrl: Contain URL in case ads stream fails. This url will be use automatically
        //in case the DAI stream encounters an error
        String fallbackUrl = "https://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8";
        PlaylistItem playlistItem = new PlaylistItem.Builder()
                .file(fallbackUrl)
                .imaDaiSettings(imaDaiSettings)
                .title("Google DAI Demo")
                .description("A video player testing video.")
                .build();

        playlist.add(playlistItem);
        PlayerConfig config = new PlayerConfig.Builder().playlist(playlist).build();

        mPlayer.setup(config);
    }

    @Override
    public void onBackPressed() {
        if (mPlayer.getFullscreen()) {
            mPlayer.setFullscreen(false, true);
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
