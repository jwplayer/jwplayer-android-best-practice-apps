package com.jwplayer.demo.pip;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.errors.ErrorCodes;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.PipCloseEvent;
import com.jwplayer.pub.api.events.PipOpenEvent;
import com.jwplayer.pub.api.events.WarningEvent;
import com.jwplayer.pub.api.events.listeners.PipPluginEvents;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.view.JWPlayerView;

/**
 * Picture-in-picture with the JW Player SDK.
 *
 * <p>The SDK does most of the work once the activity is registered with
 * {@link JWPlayer#registerActivityForPip}: it builds the PiP params from the player view, hides
 * the player controls in the PiP window, enters PiP automatically when the user presses Home
 * during playback ({@link JWPlayer#setPipOnBackground}), and pauses when the PiP window is
 * closed. The app is responsible for its own UI and for forwarding
 * {@link #onPictureInPictureModeChanged} to the player.</p>
 */
public class PipActivity extends AppCompatActivity implements
        VideoPlayerEvents.OnWarningListener,
        PipPluginEvents.OnPipOpenListener,
        PipPluginEvents.OnPipCloseListener {

    private static final String TAG = "PipDemo";

    private JWPlayer player;
    private View appUi;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pip);

        // The key comes from the JWPLAYER_LICENSE_KEY environment variable at build time
        // (see app/build.gradle). You can also pass your key here directly.
        new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

        appUi = findViewById(R.id.app_ui);
        status = findViewById(R.id.status);

        JWPlayerView playerView = findViewById(R.id.jwplayer);
        player = playerView.getPlayer(this);

        // PiP problems (unsupported device, PiP disabled for the app in system settings,
        // activity not registered, ...) are reported as WARNING events, not exceptions.
        player.addListener(EventType.WARNING, this);
        player.addListener(EventType.PIP_OPEN, this);
        player.addListener(EventType.PIP_CLOSE, this);

        // Registering hands the SDK this activity. Passing the action bar lets the SDK hide it in
        // PiP; this demo's theme has none, so null is fine. If the device can't do PiP the SDK
        // emits a warning here, which lands in the status line.
        player.registerActivityForPip(this, getSupportActionBar());

        // Auto-enter on Home. True is already the SDK default; it is set explicitly so the
        // checkbox below can show turning it off. On Android 12+ the SDK uses the system's
        // auto-enter, so there is no onUserLeaveHint() override here: entering PiP there as
        // well would race the system and can trigger an "already in PiP" warning.
        CheckBox autoPip = findViewById(R.id.auto_pip);
        player.setPipOnBackground(autoPip.isChecked());
        autoPip.setOnCheckedChangeListener((button, checked) -> player.setPipOnBackground(checked));

        findViewById(R.id.enter_pip).setOnClickListener(v -> {
            // Returns false, and emits a warning, when PiP can't be entered.
            if (!player.enterPictureInPictureMode()) {
                setStatus("enterPictureInPictureMode() returned false");
            }
        });

        player.setup(new PlayerConfig.Builder()
                .playlistUrl("https://cdn.jwplayer.com/v2/playlists/9L8aDjaU")
                .autostart(true)
                .build());
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode,
                                              @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        // Required: the SDK uses this to hide/restore its controls and to pause when the user
        // closes the PiP window (rather than expanding it back to full screen).
        player.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);

        // The PiP window shows the whole activity, so hide everything except the player.
        appUi.setVisibility(isInPictureInPictureMode ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onPipOpen(PipOpenEvent pipOpenEvent) {
        setStatus("entered PiP");
    }

    @Override
    public void onPipClose(PipCloseEvent pipCloseEvent) {
        setStatus("left PiP");
    }

    @Override
    public void onWarning(WarningEvent warningEvent) {
        String message = describe(warningEvent.getErrorCode()) + ": " + warningEvent.getMessage();
        Log.w(TAG, message);
        setStatus(message);
    }

    /** Names the PiP warning codes so the status line is readable. */
    private static String describe(int code) {
        switch (code) {
            case ErrorCodes.ERROR_PIP_UNSUPPORTED:
            case ErrorCodes.ERROR_PIP_UNAVAILABLE_DUE_TO_API:
                return "PiP not available on this device or disabled for this app";
            case ErrorCodes.ERROR_PIP_ACTIVITY_NOT_REGISTERED:
                return "Activity not registered for PiP";
            case ErrorCodes.ERROR_ENTERING_PIP_MODE_FAILED:
                return "Entering PiP failed";
            case ErrorCodes.ERROR_PIP_ASPECT_RATIO_OUTSIDE_LIMITS:
                return "PiP aspect ratio outside the system limits";
            case ErrorCodes.ERROR_PIP_MODE_ALREADY_ENABLED:
                return "Already in PiP";
            default:
                return "Warning " + code;
        }
    }

    private void setStatus(String text) {
        status.setText("Status: " + text);
    }

    @Override
    protected void onDestroy() {
        // Releases the SDK's reference to this activity.
        player.deregisterActivityForPip();
        super.onDestroy();
    }
}
