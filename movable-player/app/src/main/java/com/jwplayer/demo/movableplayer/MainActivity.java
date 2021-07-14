package com.jwplayer.demo.movableplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;

public class MainActivity extends AppCompatActivity implements
													VideoPlayerEvents.OnFullscreenListener {
	private MovablePlayerView mPlayerView;
	private JWPlayer mPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		LicenseUtil.setLicenseKey(this, YOUR_LICENSE_KEY);

		// Get a reference to the CoordinatorLayout
		RelativeLayout mRelativeLayout = findViewById(R.id.relative_layout);

		// Initialize a new JW Player.
		mPlayerView = new MovablePlayerView(this, null);
		mPlayer = mPlayerView.getPlayer();
		mPlayer.setup(new PlayerConfig.Builder()
				.file("https://content.jwplatform.com/manifests/mkZVAqxV.m3u8")
				.build());
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		RelativeLayout.LayoutParams params;
		params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				(displayMetrics.widthPixels / 16) * 9);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mPlayerView.setLayoutParams(params);
		// Add the View to the View Hierarchy.
		mRelativeLayout.addView(mPlayerView);
		mPlayer.addListener(EventType.FULLSCREEN, this);

		findViewById(R.id.movable_player_toggle).setOnClickListener(v -> {
			mPlayerView.toggleDrag();
		});
	}


	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		// Set fullscreen when the device is rotated to landscape, and not in movable player mode.
		mPlayer.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE,
								  true);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// If we are in fullscreen mode, exit fullscreen mode when the user uses the back button.
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mPlayer.getFullscreen()) {
				mPlayer.setFullscreen(false, true);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onFullscreen(FullscreenEvent fullscreenEvent) {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			if (fullscreenEvent.getFullscreen()) {
				actionBar.hide();
			} else {
				actionBar.show();
			}
		}
	}

}
