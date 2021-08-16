package com.jwplayer.chromecastdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.captions.Caption;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class JWPlayerViewExample extends AppCompatActivity
		implements VideoPlayerEvents.OnFullscreenListener {

	private JWPlayerView mPlayerView;

	private CastContext mCastContext;

	private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD = "com.google.market";
	private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW = "com.android.vending";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jwplayerview);

		if (isGoogleApiAvailable(this)) {
			mCastContext = CastContext.getSharedInstance(getApplicationContext());
		}

		mPlayerView = findViewById(R.id.jwplayer);

		// Handle hiding/showing of ActionBar
		mPlayerView.addOnFullscreenListener(this);

		// Keep the screen on during playback
		new KeepScreenOnHandler(mPlayerView, getWindow());

		// Load a media source
		PlaylistItem pi = new PlaylistItem.Builder()
				.file("https://content.jwplatform.com/manifests/1sc0kL2N.m3u8")
				.title("Press Play")
				.image("https://content.jwplatform.com/thumbs/1sc0kL2N.jpg")
				.description("Press play with JW Player")
				.build();
// Load a media source
		List<Caption> captionTracks = new ArrayList<>();

		Caption captionEn = new Caption.Builder()
				.file("https://assets-jpcust.jwpsrv.com/tracks/qb8d155c.vtt")
				.label("English")
				.isdefault(true)
				.build();

		captionTracks.add(captionEn);

		PlaylistItem playlistItem = new PlaylistItem.Builder()
				.file("https://multiplatform-f.akamaihd.net/i/multi/will/bunny/big_buck_bunny_,640x360_400,640x360_700,640x360_1000,950x540_1500,.f4v.csmil/master.m3u8")
				.title("BBBunny")
				.description("Some really great content")
				.tracks(captionTracks)
				.build();

		List<PlaylistItem> playlist = new ArrayList<>();
		playlist.add(playlistItem);

		mPlayerView.load(playlist);


	}


	private boolean doesPackageExist(String targetPackage) {
		try {
			getPackageManager().getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
		return true;
	}

	// Without the Google API's Chromecast won't work
	private boolean isGoogleApiAvailable(Context context) {
		boolean isOldPlayStoreInstalled = doesPackageExist(GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD);
		boolean isNewPlayStoreInstalled = doesPackageExist(GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW);

		boolean isPlaystoreInstalled = isNewPlayStoreInstalled||isOldPlayStoreInstalled;

		boolean isGoogleApiAvailable = GoogleApiAvailability.getInstance()
															.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
		return isPlaystoreInstalled && isGoogleApiAvailable;
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
			boolean isCasting = mCastContext != null ? mCastContext
					.getCastState() == CastState.CONNECTED : false;
			if (fullscreenEvent.getFullscreen() && !isCasting) {
				actionBar.hide();
			} else {
				actionBar.show();
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_jwplayerview, menu);
		// Register the MediaRouterButton
		CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
												R.id.media_route_menu_item);
		return true;
	}

}
