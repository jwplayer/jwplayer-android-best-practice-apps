package com.jwplayer.demo.localassetplayback;

import android.content.res.Configuration;
import android.os.Bundle;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.captions.Caption;
import com.jwplayer.pub.api.media.captions.CaptionType;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class JWPlayerViewExample extends AppCompatActivity {

	private JWPlayerView mPlayerView;
	private JWPlayer mPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jwplayerview);

		// Handle status bar insets
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_jwplayerview), (v, windowInsets) -> {
			Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
			Insets navBar = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
			v.setPadding(0, statusBars.top, 0, navBar.bottom);
			return windowInsets;
		});

		// The key comes from the JWPLAYER_LICENSE_KEY environment variable at build time
		// (see app/build.gradle). You can also pass your key here directly.
		new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

		mPlayerView = findViewById(R.id.jwplayer);
		mPlayer = mPlayerView.getPlayer(this);

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
		// Set fullscreen when the device is rotated to landscape
		mPlayer.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE,
								  true);
		super.onConfigurationChanged(newConfig);
	}
}
