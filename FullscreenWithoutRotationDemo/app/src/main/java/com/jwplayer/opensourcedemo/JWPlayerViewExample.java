package com.jwplayer.opensourcedemo;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.fullscreen.FullscreenHandler;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;


public class JWPlayerViewExample extends AppCompatActivity {

	private JWPlayerView mPlayerView;
	private JWPlayer mPlayer;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jwplayerview);

		LicenseUtil.setLicenseKey(this, YOUR_LICENSE_KEY);

		mPlayerView = findViewById(R.id.jwplayer);
		mPlayer = mPlayerView.getPlayer();

		// set screen handler for manage the rotation of the screen
		mPlayer.setFullscreenHandler(new FullScreenHandler_NoRotation(mPlayerView));

		// Load a media source
		PlaylistItem pi = new PlaylistItem.Builder()
				.file("https://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8")
				.title("BipBop")
				.description("A video player testing video.")
				.build();

		List<PlaylistItem> playlist = new ArrayList<>();
		playlist.add(pi);

		PlayerConfig playerConfig = new PlayerConfig.Builder()
				.playlist(playlist)
				.build();

		mPlayer.setup(playerConfig);
	}

	public class FullScreenHandler_NoRotation implements FullscreenHandler {
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
