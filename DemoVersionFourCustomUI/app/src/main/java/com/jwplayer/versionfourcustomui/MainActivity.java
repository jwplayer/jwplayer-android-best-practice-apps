package com.jwplayer.versionfourcustomui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.UiGroup;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.configuration.UiConfig;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.api.ui.viewmodels.IBaseViewModel;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private JWPlayer mPlayer;
    private JWPlayerView mPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayerView = findViewById(R.id.jwplayer);

        LicenseUtil.setLicenseKey(this, "YOUR LICENSE KEY");
        mPlayerView.getPlayerAsync(this, this, new JWPlayer.PlayerInitializationListener() {
            @Override
            public void onPlayerInitialized(JWPlayer jwPlayer) {
                mPlayer = jwPlayer;

                //We'll need to make a UIConfig to pass to the config
                // inside it we can add a flag to hide the existing next up view
                UiConfig noNextUpViewUiConfig = new UiConfig.Builder()
                        .displayAllControls() //Default all the controls to true
                        .hide(UiGroup.NEXT_UP) //Hide the next up view in particular since we're going to replace that one
                        .build();

                //Set up the playlist with more than 1 video
                // so we'll see next up behaviour at the end of the first video
                List<PlaylistItem> playlist = new ArrayList<>();
                playlist.add(new PlaylistItem.Builder()
                        .file("https://cdn.jwplayer.com/manifests/Y5UQq0fG.m3u8")
                        .image("https://cdn.jwplayer.com/v2/media/Y5UQq0fG/poster.jpg?width=720")
                        .build());

                //Add a 2nd video to play
                playlist.add(new PlaylistItem.Builder()
                        .file("https://cdn.jwplayer.com/videos/tkM1zvBq-Zq6530MP.mp4")
                        .image("https://cdn.jwplayer.com/v2/media/tkM1zvBq/poster.jpg?width=720")
                        .build());

                //Build a config with our settings to pass to the player
                PlayerConfig config = new PlayerConfig.Builder()
                        //Attach the playlist so we have content to view
                        .playlist(playlist)
                        //Attach the uiconfig so we'll hide the next up view
                        .uiConfig(noNextUpViewUiConfig)
                        .build();

                //Setup the player
                mPlayer.setup(config);

                IBaseViewModel v = mPlayer.getViewModelForUiGroup(UiGroup.NEXT_UP);

            }
        });
    }
}