package com.jwplayer.opensourcedemo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.fullscreen.FullscreenHandler;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Constructor;


public class JWPlayerViewExample extends AppCompatActivity {

	private JWPlayerView mPlayerView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jwplayerview);

		mPlayerView = findViewById(R.id.jwplayer);

		// set screen handler for manage the rotation of the screen
		mPlayerView.setFullscreenHandler(new FullScreenHandler_NoRotation(mPlayerView));

		// Load a media source
		PlaylistItem pi = new PlaylistItem.Builder()
				.file("https://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8")
				.title("BipBop")
				.description("A video player testing video.")
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
		public void onResume() {

		}

		@Override
		public void onPause() {

		}

		@Override
		public void onDestroy() {

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
