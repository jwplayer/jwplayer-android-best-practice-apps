package com.jwplayer.demo.drmdemo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.DrmConfig;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    /** Set to true to use the custom {@link WidevineCallback} instead of {@link DrmConfig}. */
    private static final boolean USE_CUSTOM_CALLBACK = false;

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

        // INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
        // [OR] change in app-level build.gradle
        // [OR] set JWPLAYER_LICENSE_KEY as environment variable
        new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

        playerView.getPlayerAsync(this, this, jwPlayer -> {
            player = jwPlayer;

            attachListeners();

            // TODO: fill these in. See README.md for where to get them for JW Player-hosted content.
            String streamURL = "";   // e.g. a DASH manifest URL of DRM-protected content
            String licenseURL = "";  // the Widevine licence server URL for that content

            if (streamURL.isEmpty() || licenseURL.isEmpty()) {
                status.append("\nStream and license URL are required (see README.md)");
                return;
            }

            PlaylistItem item = USE_CUSTOM_CALLBACK
                    ? buildItemWithCustomCallback(streamURL, licenseURL)
                    : buildItemWithDrmConfig(streamURL, licenseURL);

            player.setup(new PlayerConfig.Builder()
                    .playlist(Collections.singletonList(item))
                    .build());
        });
    }

    /**
     * Recommended: let the SDK request the licence. A {@link DrmConfig} with the licence URL is all
     * that JW Player Studio DRM content (and most Widevine licence servers) needs.
     */
    private static PlaylistItem buildItemWithDrmConfig(String streamURL, String licenseURL) {
        return new PlaylistItem.Builder()
                .file(streamURL)
                .drmConfig(new DrmConfig.Builder()
                        .licenseUri(licenseURL)
                        .build())
                .build();
    }

    /**
     * Optional: use your own {@link com.jwplayer.pub.api.media.drm.MediaDrmCallback} when your
     * licence server needs something the SDK doesn't send, such as extra request headers or a
     * custom request/response format. See {@link WidevineCallback}.
     */
    private static PlaylistItem buildItemWithCustomCallback(String streamURL, String licenseURL) {
        // Additional properties (HTTP headers) to send to your DRM licence server
        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("sample_request_prop_key", "sample_request_prop_value");

        // HTTP headers for self-hosted content that needs header authorization to fetch the media
        Map<String, String> httpHeaders = new HashMap<>();
        httpHeaders.put("sample_header_key", "sample_header_value");

        return new PlaylistItem.Builder()
                .file(streamURL)
                .httpHeaders(httpHeaders)
                .mediaDrmCallback(new WidevineCallback(licenseURL, requestProperties))
                .build();
    }

    private void attachListeners() {
        player.addListener(EventType.SETUP_ERROR, (VideoPlayerEvents.OnSetupErrorListener) setupErrorEvent -> status.append("\n" + setupErrorEvent.getMessage()));

        player.addListener(EventType.ERROR, (VideoPlayerEvents.OnErrorListener) errorEvent -> status.append("\n" + errorEvent.getMessage()));

        player.addListener(EventType.READY, (VideoPlayerEvents.OnReadyListener) readyEvent -> status.append("\nREADY"));
    }
}