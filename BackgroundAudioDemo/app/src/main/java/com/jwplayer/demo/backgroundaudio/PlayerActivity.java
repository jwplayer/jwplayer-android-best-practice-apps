package com.jwplayer.demo.backgroundaudio;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.background.MediaServiceController;
import com.jwplayer.pub.api.background.ServiceMediaApi;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Plays a playlist with background audio. The SDK runs the media session, the media
 * notification and the lock-screen controls; the {@link ServiceMediaApi} passed to
 * {@link MediaServiceController#enable} only customises them.
 */
public class PlayerActivity extends AppCompatActivity {

    static final String EXTRA_MODE = "mode";
    static final String MODE_BUTTONS = "buttons";
    static final String MODE_LEGACY = "legacy";
    static final String MODE_DEFAULT = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
        // [OR] change in app-level build.gradle
        // [OR] set JWPLAYER_LICENSE_KEY as environment variable
        new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

        JWPlayerView playerView = findViewById(R.id.jwplayer);
        JWPlayer player = playerView.getPlayer(this);

        // Keep playing when the app goes to the background.
        player.allowBackgroundAudio(true);

        String mode = getIntent().getStringExtra(EXTRA_MODE);
        TextView status = findViewById(R.id.status);
        ServiceMediaApi api;
        if (MODE_BUTTONS.equals(mode)) {
            api = new LikeButtonsMediaApi(player, getApplicationContext());
            status.setText(R.string.mode_buttons);
        } else if (MODE_LEGACY.equals(mode)) {
            api = new LegacyActionsMediaApi(player);
            status.setText(R.string.mode_legacy);
        } else {
            api = null; // the SDK's default transport controls
            status.setText(R.string.mode_default);
        }

        // Binds the SDK's background-audio service for the lifetime of this activity: it is
        // unbound automatically on ON_DESTROY, so there is no onDestroy() bookkeeping.
        MediaServiceController.enable(this, player, api);

        player.setup(new PlayerConfig.Builder()
                .playlist(createPlaylist())
                .autostart(true)
                .build());
    }

    private static List<PlaylistItem> createPlaylist() {
        List<PlaylistItem> playlist = new ArrayList<>();
        playlist.add(new PlaylistItem.Builder()
                .file("https://cdn.jwplayer.com/manifests/Gihxhhy6.m3u8")
                .title("First item")
                .description("Background audio with the SDK media session")
                .image("https://cdn.jwplayer.com/v2/media/Gihxhhy6/poster.jpg")
                .build());
        playlist.add(new PlaylistItem.Builder()
                .file("https://cdn.jwplayer.com/manifests/klAxrVbn.m3u8")
                .title("Second item")
                .description("Use the notification's next / previous controls")
                .image("https://cdn.jwplayer.com/v2/media/klAxrVbn/poster.jpg")
                .build());
        return playlist;
    }
}
