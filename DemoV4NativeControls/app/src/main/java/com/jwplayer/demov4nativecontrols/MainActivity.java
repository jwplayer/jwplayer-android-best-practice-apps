package com.jwplayer.demov4nativecontrols;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.UiGroup;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.configuration.UiConfig;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.ui.viewmodels.NextUpViewModel;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JWPlayerView playerView = findViewById(R.id.player);
        final String videoFileName = "http://content.bitsontherun.com/videos/bkaovAYt-injeKYZS.mp4";
        final String videoFileName2 = "http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8";
        final String videoFileName3 = "http://content.bitsontherun.com/videos/3XnJSIm4-52qL9xLP.mp4";

        String licenseKey = "<your license key here>";
        LicenseUtil.setLicenseKey(this,licenseKey);

        playerView.getPlayerAsync(this, this, new JWPlayer.PlayerInitializationListener() {
            @Override
            public void onPlayerInitialized(JWPlayer jwPlayer) {
                //Setup our UIConfig to hide the current next up view
                UiConfig uiConfig = new UiConfig.Builder()
                        .displayAllControls()
                        .hide(UiGroup.NEXT_UP)
                        .build();

                //Setup our playlist of 3 items
                PlaylistItem item1 = new PlaylistItem.Builder()
                        .file(videoFileName)
                        .title("test video 1")
                        .build();

                PlaylistItem item2 = new PlaylistItem.Builder()
                        .file(videoFileName2)
                        .title("test video 2")
                        .build();

                PlaylistItem item3 = new PlaylistItem.Builder()
                        .file(videoFileName3)
                        .title("test video 3")
                        .build();

                List<PlaylistItem> playlistItems = new ArrayList<>();
                playlistItems.add(item1);
                playlistItems.add(item2);
                playlistItems.add(item3);

                //Call setup on the jwplayer to load the playlist and the UI changes
                jwPlayer.setup(new PlayerConfig.Builder()
                        .playlist(playlistItems)
                        .uiConfig(uiConfig)
                        .build());

                CustomNextUpView customNextUpView = new CustomNextUpView(getBaseContext());
                NextUpViewModel nextUpViewModel = (NextUpViewModel)jwPlayer.getViewModelForUiGroup(UiGroup.NEXT_UP);
                customNextUpView.setNextUpViewModel(nextUpViewModel);
                playerView.addView(customNextUpView);
            }
        });
    }
}