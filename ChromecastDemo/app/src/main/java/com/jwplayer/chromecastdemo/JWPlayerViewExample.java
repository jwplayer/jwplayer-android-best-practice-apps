package com.jwplayer.chromecastdemo;

import android.content.Context;
import android.content.pm.PackageInfo;
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
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class JWPlayerViewExample extends AppCompatActivity
		implements VideoPlayerEvents.OnFullscreenListener {

	private JWPlayerView mPlayerView;

	private CastContext mCastContext;

	private static final String GooglePlayStorePackageNameOld = "com.google.market";
	private static final String GooglePlayStorePackageNameNew = "com.android.vending";

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

		mPlayerView.load(pi);


	}

	// Without the Google API's Chromecast won't work
	private boolean isGoogleApiAvailable(Context context) {
		boolean googlePlayStoreInstalled = false;
		PackageManager packageManager = context.getApplicationContext().getPackageManager();
		List<PackageInfo> packages = packageManager
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo packageInfo : packages) {
			if (packageInfo.packageName.equals(GooglePlayStorePackageNameOld) ||
					packageInfo.packageName.equals(GooglePlayStorePackageNameNew)) {
				googlePlayStoreInstalled = true;
				break;
			}
		}
		boolean isGoogleApiAvailable = GoogleApiAvailability.getInstance()
														  .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
		return googlePlayStoreInstalled && isGoogleApiAvailable;
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
