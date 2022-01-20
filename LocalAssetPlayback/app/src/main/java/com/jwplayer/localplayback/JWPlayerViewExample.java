package com.jwplayer.localplayback;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.captions.Caption;
import com.jwplayer.pub.api.media.captions.CaptionType;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;


public class JWPlayerViewExample extends AppCompatActivity
		implements VideoPlayerEvents.OnFullscreenListener {

	private JWPlayerView mPlayerView;
	private JWPlayer mPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jwplayerview);

		// INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
		// [OR] change in app-level build.gradle
		// [OR] set JWPLAYER_LICENSE_KEY as environment variable
		new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

		mPlayerView = findViewById(R.id.jwplayer);
		mPlayer = mPlayerView.getPlayer();

		// Handle hiding/showing of ActionBar
		mPlayer.addListener(EventType.FULLSCREEN, this);

		// Keep the screen on during playback
		new KeepScreenOnHandler(mPlayer, getWindow());

		Caption caption = new Caption.Builder()
				.file("file:///android_asset/press-play-captions.vtt")
				.kind(CaptionType.CAPTIONS)
				.label("en")
				.isDefault(true)
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

		List<PlaylistItem> playlist = new ArrayList<>();
		playlist.add(pi);

		PlayerConfig playerConfig = new PlayerConfig.Builder()
				.playlist(playlist)
				.build();

		mPlayer.setup(playerConfig);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Set fullscreen when the device is rotated to landscape
		mPlayer.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE,
								  true);
		super.onConfigurationChanged(newConfig);
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
