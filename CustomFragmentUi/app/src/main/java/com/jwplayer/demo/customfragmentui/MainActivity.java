package com.jwplayer.demo.customfragmentui;

import android.content.res.Configuration;
import android.os.Bundle;

import com.jwplayer.demo.customfragmentui.player.CustomPlayerFragment;
import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Hosts {@link CustomPlayerFragment} and shows a small log of player events below it.
 *
 * The fragment calls {@link #onPlayerInitialized(JWPlayer)} once it has created its player, which
 * also happens again after the fragment is restored (e.g. after process death).
 */
public class MainActivity extends AppCompatActivity implements JWPlayer.PlayerInitializationListener {

    // Back leaves fullscreen. Enabled only while the player is fullscreen, so otherwise Back
    // behaves normally. Uses the OnBackPressedDispatcher rather than onKeyDown(KEYCODE_BACK).
    private final OnBackPressedCallback mExitFullscreenOnBack = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            CustomPlayerFragment fragment = getPlayerFragment();
            if (fragment != null) {
                fragment.setFullscreen(false, true);
            }
        }
    };

    private static final String PLAYER_FRAGMENT_TAG = "PlayerFragment";
    private ConstraintLayout mRoot;
    private EventLog mEventLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(isLandscape() ? R.layout.activity_main_land : R.layout.activity_main);
        mRoot = findViewById(R.id.root);

        // targetSdk 35+ draws edge-to-edge, so keep the content clear of the system bars.
        ViewCompat.setOnApplyWindowInsetsListener(mRoot, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });

        // INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
        // [OR] change in app-level build.gradle
        // [OR] set JWPLAYER_LICENSE_KEY as environment variable
        new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

        // Only add the fragment the first time; after recreation the FragmentManager restores it.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.player_container, new CustomPlayerFragment(), PLAYER_FRAGMENT_TAG)
                    .commit();
        }

        getOnBackPressedDispatcher().addCallback(this, mExitFullscreenOnBack);
    }

    @Override
    public void onPlayerInitialized(JWPlayer player) {
        if (mEventLog == null) {
            mEventLog = new EventLog(findViewById(R.id.event_log));
        }
        mEventLog.attach(player);
        player.addListener(EventType.FULLSCREEN, (VideoPlayerEvents.OnFullscreenListener) event ->
                mExitFullscreenOnBack.setEnabled(event.getFullscreen()));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // The activity handles orientation changes itself (see the manifest), so swap constraints
        // instead of recreating the activity and the player.
        boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this, isLandscape ? R.layout.activity_main_land : R.layout.activity_main);
        constraintSet.applyTo(mRoot);
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private CustomPlayerFragment getPlayerFragment() {
        return (CustomPlayerFragment) getSupportFragmentManager().findFragmentByTag(PLAYER_FRAGMENT_TAG);
    }
}
