package com.jwplayer.stylingdemo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;


public class JWPlayerViewExample extends AppCompatActivity
		implements VideoPlayerEvents.OnFullscreenListener {

	private JWPlayerView mPlayerView;

	private CastContext mCastContext;

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

		// TODO: Add your license key
		new LicenseUtil().setLicenseKey(this, YOUR_LICENSE_KEY );
		mPlayerView = findViewById(R.id.jwplayer);
		mPlayer = mPlayerView.getPlayer(this);


		// Handle hiding/showing of ActionBar
		mPlayer.addListener(EventType.FULLSCREEN, this);

		// Keep the screen on during playback
		new KeepScreenOnHandler(mPlayer, getWindow());

		// Load a media source
		PlayerConfig config = new PlayerConfig.Builder()
				.playlistUrl("https://cdn.jwplayer.com/v2/playlists/3jBCQ2MI?format=json")
				.build();

		mPlayer.setup(config);

		// Get a reference to the CastContext
		mCastContext = CastContext.getSharedInstance(this);

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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Exit fullscreen when the user pressed the Back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mPlayer.getFullscreen()) {
				mPlayer.setFullscreen(false, true);
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
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
