package com.jwplayer.demo.fullscreenwithoutrotationdemo;

import android.os.Bundle;
import android.view.ViewGroup;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.fullscreen.FullscreenHandler;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class JWPlayerViewExample extends AppCompatActivity {

	// Back leaves fullscreen. Enabled only while the player is fullscreen, so otherwise Back
	// behaves normally. Uses the OnBackPressedDispatcher rather than onKeyDown(KEYCODE_BACK).
	private final OnBackPressedCallback mExitFullscreenOnBack = new OnBackPressedCallback(false) {
		@Override
		public void handleOnBackPressed() {
			mPlayer.setFullscreen(false, false);
		}
	};

	private JWPlayerView mPlayerView;
	private JWPlayer mPlayer;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jwplayerview);

		// The key comes from the JWPLAYER_LICENSE_KEY environment variable at build time
		// (see app/build.gradle). You can also pass your key here directly.
		new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

		// Handle status bar insets
		ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, windowInsets) -> {
			Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
			Insets navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
			v.setPadding(0, statusBars.top, 0, navigationBars.bottom);
			return windowInsets;
		});

		mPlayerView = findViewById(R.id.jwplayer);
		mPlayer = mPlayerView.getPlayer(this);

		// Replace the default FullscreenHandler (which rotates to landscape) with one that just
		// resizes the player to fill its parent in the current orientation (system bars stay visible).
		mPlayer.setFullscreenHandler(new FullScreenHandler_NoRotation(mPlayerView));

		// Load a media source
		PlaylistItem pi = new PlaylistItem.Builder()
				.file("https://cdn.jwplayer.com/manifests/Gihxhhy6.m3u8")
				.title("Fullscreen without rotation")
				.description("Tap fullscreen: the player fills the content area in the current orientation.")
				.build();

		List<PlaylistItem> playlist = new ArrayList<>();
		playlist.add(pi);

		PlayerConfig playerConfig = new PlayerConfig.Builder()
				.playlist(playlist)
				.build();

		mPlayer.setup(playerConfig);

		getOnBackPressedDispatcher().addCallback(this, mExitFullscreenOnBack);
		mPlayer.addListener(EventType.FULLSCREEN, (VideoPlayerEvents.OnFullscreenListener) event ->
				mExitFullscreenOnBack.setEnabled(event.getFullscreen()));
	}

	public static class FullScreenHandler_NoRotation implements FullscreenHandler {
		JWPlayerView mPlayerView;
		ViewGroup.LayoutParams mDefaultParams;
		ViewGroup.LayoutParams mFullscreenParams;

		public FullScreenHandler_NoRotation(JWPlayerView view){
			mPlayerView = view;
			mDefaultParams = mPlayerView.getLayoutParams();
		}

		@Override
		public void onFullscreenRequested() {
			doFullscreen(true);
		}

		@Override
		public void onFullscreenExitRequested() {
			doFullscreen(false);
		}

		@Override
		public void onAllowRotationChanged(boolean allowRotation) {

		}

		@Override
		public void onAllowFullscreenPortraitChanged(boolean allowFullscreenPortrait) {

		}

		@Override
		public void updateLayoutParams(ViewGroup.LayoutParams layoutParams) {

		}

		@Override
		public void setUseFullscreenLayoutFlags(boolean flags) {

		}

		private void doFullscreen(boolean fullscreen){
			if (fullscreen) {
				mFullscreenParams = fullscreenLayoutParams(mDefaultParams);
				mPlayerView.setLayoutParams(mFullscreenParams);
			} else {
				mPlayerView.setLayoutParams(mDefaultParams);
			}
			mPlayerView.requestLayout();
			mPlayerView.postInvalidate();
		}

		/**
		 * Creates a clone of srcParams with the width and height set to MATCH_PARENT.
		 *
		 * @param srcParams
		 * @return LayoutParams in fullscreen.
		 */
		protected ViewGroup.LayoutParams fullscreenLayoutParams(ViewGroup.LayoutParams srcParams) {
			ViewGroup.LayoutParams params = null;
			try {
				Constructor<? extends ViewGroup.LayoutParams> ctor =
						srcParams.getClass().getConstructor(ViewGroup.LayoutParams.class);
				params = ctor.newInstance(srcParams);
			} catch (Exception e) {
				params = new ViewGroup.LayoutParams(srcParams);
			}
			params.height = ViewGroup.LayoutParams.MATCH_PARENT;
			params.width = ViewGroup.LayoutParams.MATCH_PARENT;
			return params;
		}
	}
}
