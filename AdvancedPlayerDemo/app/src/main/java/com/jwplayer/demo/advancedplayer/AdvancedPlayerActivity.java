package com.jwplayer.demo.advancedplayer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.configuration.UiConfig;
import com.jwplayer.pub.api.configuration.ads.VastAdvertisingConfig;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.ads.AdBreak;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Sets up a player with the SDK's own UI turned off and hands it to {@link PlayerControlsView},
 * which draws a fully custom control bar from plain Android Views.
 */
public class AdvancedPlayerActivity extends AppCompatActivity {

    /** A JW Player playlist. Each item is an HLS stream with several quality levels. */
    private static final String PLAYLIST_URL = "https://cdn.jwplayer.com/v2/playlists/9L8aDjaU";

    /** Google's public IMA sample VAST tag (a single linear ad). */
    private static final String VAST_TAG =
            "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_ad_samples"
                    + "&sz=640x480&cust_params=sample_ct%3Dlinear&ciu_szs=300x250%2C728x90&gdfp_req=1"
                    + "&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=__random-number__";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keep content clear of the status and navigation bars (edge-to-edge is the default on Android 15+).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // The license key comes from the JWPLAYER_LICENSE_KEY environment variable at build time.
        new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

        JWPlayerView playerView = findViewById(R.id.jwplayer);
        PlayerControlsView controls = findViewById(R.id.controls);

        // getPlayerAsync ties the player to this Activity's lifecycle: the SDK pauses it in
        // onPause and releases it in onDestroy, so no manual lifecycle code is needed here.
        playerView.getPlayerAsync(this, this, player -> {
            // Bind the controls before setup() so they receive the first events (LEVELS, etc.).
            controls.bind(player);
            player.setup(buildConfig());
        });
    }

    private PlayerConfig buildConfig() {
        // A pre-roll and a mid-roll 15 seconds into each item.
        List<AdBreak> schedule = new ArrayList<>();
        schedule.add(new AdBreak.Builder().tag(VAST_TAG).offset("pre").build());
        schedule.add(new AdBreak.Builder().tag(VAST_TAG).offset("15").build());

        VastAdvertisingConfig advertising = new VastAdvertisingConfig.Builder()
                .schedule(schedule)
                // The sample ad is not skippable on its own; a skip offset (in seconds) makes
                // the player allow skipping so the custom Skip button has something to do.
                .skipOffset(5)
                .build();

        return new PlayerConfig.Builder()
                .playlistUrl(PLAYLIST_URL)
                .advertisingConfig(advertising)
                // Turn off every SDK control, including the ad UI: PlayerControlsView replaces it.
                .uiConfig(new UiConfig.Builder().hideAllControls().build())
                .autostart(true)
                .build();
    }
}
