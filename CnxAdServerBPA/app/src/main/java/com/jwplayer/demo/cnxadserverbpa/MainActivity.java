package com.jwplayer.demo.cnxadserverbpa;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.view.JWPlayerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The launch screen: the simplest CNX Ad Server setup, with a live ad event log underneath so the
 * screen doubles as a debugging aid.
 *
 * <p>The menu switches between manual and dynamic ad scheduling, and opens the pause-on-scroll
 * (viewability) example.
 */
public class MainActivity extends AppCompatActivity implements AdEventLog.Listener {

    private JWPlayer player;
    private TextView eventLog;
    private ScrollView eventLogScroll;
    private CnxPlayerConfig.AdScheduling scheduling = CnxPlayerConfig.AdScheduling.MANUAL;
    // A configuration supplied while the previous one is still loading replaces it before it is
    // ready, so the scheduling menu stays disabled until each setup finishes.
    private boolean setupInProgress;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        eventLog = findViewById(R.id.event_log);
        eventLogScroll = findViewById(R.id.event_log_scroll);

        JWPlayerView playerView = findViewById(R.id.player_view);
        player = playerView.getPlayer(this);
        AdEventLog.attach(player, this);

        setUpPlayer();
    }

    private void setUpPlayer() {
        if (scheduling == CnxPlayerConfig.AdScheduling.MANUAL) {
            append("MANUAL scheduling: pre-roll, mid-roll at 30s, post-roll.");
        } else {
            append("DYNAMIC scheduling: pre-roll, then mid-rolls placed by the ad scheduler, none before "
                    + (int) CnxPlayerConfig.DYNAMIC_FIRST_MIDROLL_AFTER + "s of content and at least "
                    + (int) CnxPlayerConfig.DYNAMIC_SECONDS_BETWEEN_MIDROLLS + "s apart, and a post-roll.");
        }
        setupInProgress = true;
        invalidateOptionsMenu();
        // Setting up the same player again replaces its current setup, so no restart is needed.
        player.setup(CnxPlayerConfig.make(scheduling, false));
    }

    // A plain JWPlayerView does not go fullscreen on rotation by itself: without this, landscape would
    // push the player's controls off screen and leave no room for the log.
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        player.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE, true);
    }

    // --- Menu ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, R.id.menu_scheduling, Menu.NONE,
                scheduling == CnxPlayerConfig.AdScheduling.MANUAL ? R.string.menu_dynamic : R.string.menu_manual)
                .setEnabled(!setupInProgress);
        menu.add(Menu.NONE, R.id.menu_feed, Menu.NONE, R.string.menu_feed);
        menu.add(Menu.NONE, R.id.menu_clear, Menu.NONE, R.string.menu_clear);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_scheduling) {
            scheduling = scheduling == CnxPlayerConfig.AdScheduling.MANUAL
                    ? CnxPlayerConfig.AdScheduling.DYNAMIC
                    : CnxPlayerConfig.AdScheduling.MANUAL;
            setUpPlayer();
            return true;
        } else if (id == R.id.menu_feed) {
            player.pause();
            startActivity(new Intent(this, FeedActivity.class));
            return true;
        } else if (id == R.id.menu_clear) {
            eventLog.setText("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- AdEventLog.Listener ---

    @Override
    public void onLog(String line) {
        append(line);
    }

    @Override
    public void onSetupFinished() {
        setupInProgress = false;
        invalidateOptionsMenu();
    }

    private void append(String line) {
        eventLog.append(timeFormat.format(new Date()) + "  " + line + "\n");
        eventLogScroll.post(() -> eventLogScroll.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
