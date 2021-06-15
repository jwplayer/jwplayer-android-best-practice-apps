package com.jwplayer.localplayback;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.license.LicenseUtil;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.captions.Caption;
import com.longtailvideo.jwplayer.media.captions.CaptionType;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;
import java.util.List;


public class JWPlayerViewExample extends AppCompatActivity
		implements VideoPlayerEvents.OnFullscreenListener {

	private JWPlayerView mPlayerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jwplayerview);

		LicenseUtil.setLicenseKey(this, YOUR_LICENSE_KEY);

		mPlayerView = findViewById(R.id.jwplayer);

		// Handle hiding/showing of ActionBar
		mPlayerView.addOnFullscreenListener(this);

		// Keep the screen on during playback
		new KeepScreenOnHandler(mPlayerView, getWindow());

		Caption caption = new Caption.Builder()
				.file("file:///android_asset/press-play-captions.vtt")
				.kind(CaptionType.CAPTIONS)
				.label("en")
				.isdefault(true)
				.build();
		List<Caption> captionList =  new ArrayList<>();
		captionList.add(caption);

		// Load a media source
		PlaylistItem pi = new PlaylistItem.Builder()
				.file("file:///android_asset/press-play.mp4")
				.image("file:///android_asset/press-play-poster.jpg")
				.title("Press Play")
				.tracks(captionList)
				.build();

		mPlayerView.load(pi);

	}


	@Override
	protected void onStart() {
		super.onStart();
		mPlayerView.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPlayerView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPlayerView.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mPlayerView.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPlayerView.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Set fullscreen when the device is rotated to landscape
		mPlayerView.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE,
								  true);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Exit fullscreen when the user pressed the Back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mPlayerView.getFullscreen()) {
				mPlayerView.setFullscreen(false, true);
				return false;
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
