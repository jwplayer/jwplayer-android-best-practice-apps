package com.jwplayer.opensourcedemo;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.fullscreen.FullscreenHandler;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Constructor;


public class JWPlayerViewExample extends AppCompatActivity
		implements VideoPlayerEvents.OnFullscreenListener {

	private JWPlayerView mPlayerView;

	private CallbackScreen mCallbackScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jwplayerview);

		mPlayerView = findViewById(R.id.jwplayer);


		// Handle hiding/showing of ActionBar
		mPlayerView.addOnFullscreenListener(this);

		// Keep the screen on during playback
		new KeepScreenOnHandler(mPlayerView, getWindow());

		mPlayerView.setFullscreenHandler(new FullScreenHandler_NoRotation(mPlayerView));

		// Event Logging
		mCallbackScreen = findViewById(R.id.callback_screen);
		mCallbackScreen.registerListeners(mPlayerView);

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


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_jwplayerview, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.switch_to_fragment:
				Intent i = new Intent(this, JWPlayerFragmentExample.class);
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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
