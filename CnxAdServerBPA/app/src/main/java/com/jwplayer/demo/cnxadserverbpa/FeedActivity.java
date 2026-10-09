package com.jwplayer.demo.cnxadserverbpa;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.view.JWPlayerView;

/**
 * Pause-on-scroll (viewability): an article with a player in the middle of it.
 *
 * <p>The player is set up with {@code autoPauseAdsOnViewability(true)}. Start an ad, then scroll
 * until less than half of the player is on screen: the ad pauses. Scroll back and it resumes.
 *
 * <p>The bar at the top shows the latest ad event, so you can watch the pause and resume happen.
 */
public class FeedActivity extends AppCompatActivity implements AdEventLog.Listener {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        status = findViewById(R.id.ad_status);

        JWPlayerView playerView = findViewById(R.id.player_view);
        // Tied to this activity's lifecycle: the player stops when you leave the screen.
        JWPlayer player = playerView.getPlayer(this);
        AdEventLog.attach(player, this);
        player.setup(CnxPlayerConfig.make(CnxPlayerConfig.AdScheduling.MANUAL, true));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // --- AdEventLog.Listener ---

    @Override
    public void onLog(String line) {
        status.setText("Last ad event: " + line);
    }

    @Override
    public void onSetupFinished() {
    }
}
