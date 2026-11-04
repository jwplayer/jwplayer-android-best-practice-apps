package com.jwplayer.demo.stylingdemo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.concurrent.Executors;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;


public class JWPlayerViewExample extends AppCompatActivity
		implements VideoPlayerEvents.OnFullscreenListener {

	private JWPlayerView mPlayerView;

	private JWPlayer mPlayer;
	
	private AppBarLayout mAppBarLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		setContentView(R.layout.activity_jwplayerview);

		mAppBarLayout = findViewById(R.id.app_bar_layout);
		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Handle status bar insets
		ViewCompat.setOnApplyWindowInsetsListener(mAppBarLayout, (v, windowInsets) -> {
			Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
			v.setPadding(0, statusBars.top, 0, 0);
			return windowInsets;
		});

		// INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
		// [OR] change in app-level build.gradle
		// [OR] set JWPLAYER_LICENSE_KEY as environment variable
		new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);
		mPlayerView = findViewById(R.id.jwplayer);
		mPlayer = mPlayerView.getPlayer(this);


		// Handle hiding/showing of ActionBar
		mPlayer.addListener(EventType.FULLSCREEN, this);

		// Keep the screen on during playback
		new KeepScreenOnHandler(mPlayer, getWindow());

		// Load a media source
		PlayerConfig config = new PlayerConfig.Builder()
				.playlistUrl("https://cdn.jwplayer.com/v2/playlists/9L8aDjaU")
				.build();

		mPlayer.setup(config);

		// Initialise the Cast framework so the player's cast button is available
		// (the casting UI is styled by the same resource overrides).
		CastContext.getSharedInstance(this, Executors.newSingleThreadExecutor());

		// Exit fullscreen when the user presses Back
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (mPlayer.getFullscreen()) {
					mPlayer.setFullscreen(false, true);
				} else {
					setEnabled(false);
					getOnBackPressedDispatcher().onBackPressed();
				}
			}
		});
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (!mPlayer.isInPictureInPictureMode()) {
			final boolean isFullscreen = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
			mPlayer.setFullscreen(isFullscreen, true);
		}
	}

	@Override
	public void onFullscreen(FullscreenEvent fullscreenEvent) {
		if (mAppBarLayout != null) {
			if (fullscreenEvent.getFullscreen()) {
				mAppBarLayout.setVisibility(View.GONE);
			} else {
				mAppBarLayout.setVisibility(View.VISIBLE);
			}
		}
	}
}
