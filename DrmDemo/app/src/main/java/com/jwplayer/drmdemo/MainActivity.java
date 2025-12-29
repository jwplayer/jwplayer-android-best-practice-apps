package com.jwplayer.drmdemo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private JWPlayer player;
    private JWPlayerView playerView;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Handle status bar insets
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, windowInsets) -> {
            Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, statusBars.top, 0, navBars.bottom);
            return windowInsets;
        });

        playerView = findViewById(R.id.jwplayerview);
        status = findViewById(R.id.status);

        // TODO set license key
        new LicenseUtil().setLicenseKey(this, JWPLAYER_LICENSE_KEY);

        playerView.getPlayerAsync(this, this, jwPlayer -> {
            player = jwPlayer;

            attachListeners();

            String streamURL = "";
            String licenseURL = "";

            if (streamURL.isEmpty() || licenseURL.isEmpty()) {
                status.append("\nStream and license URL are required");
                return;
            }

            // For self hosted content requiring header authorization
            Map<String, String> httpHeaders = new HashMap<>();
            httpHeaders.put("sample_header_key", "sample_header_value");

            // Additional properties to pass to your DRM License server
            Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("sample_request_prop_key", "sample_request_prop_value");

            player = jwPlayer;
            List<PlaylistItem> playlist = new ArrayList<>();
            playlist.add(new PlaylistItem.Builder()
                    .file(streamURL)
                    .httpHeaders(httpHeaders)
                    .mediaDrmCallback(new WidevineCallback(licenseURL, requestProperties))
                    .build());
            player.setup(new PlayerConfig.Builder()
                    .playlist(playlist)
                    .build());

        });
    }

    private void attachListeners() {
        player.addListener(EventType.SETUP_ERROR, (VideoPlayerEvents.OnSetupErrorListener) setupErrorEvent -> status.append("\n" + setupErrorEvent.getMessage()));

        player.addListener(EventType.ERROR, (VideoPlayerEvents.OnErrorListener) errorEvent -> status.append("\n" + errorEvent.getMessage()));

        player.addListener(EventType.READY, (VideoPlayerEvents.OnReadyListener) readyEvent -> status.append("\nREADY"));
    }
}