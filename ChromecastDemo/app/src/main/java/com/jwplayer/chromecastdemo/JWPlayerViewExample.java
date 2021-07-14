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
import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;


public class JWPlayerViewExample extends AppCompatActivity
		implements VideoPlayerEvents.OnFullscreenListener {

	private JWPlayerView mPlayerView;
	private JWPlayer mPlayer;

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

		LicenseUtil.setLicenseKey(this, YOUR_LICENSE_KEY);

		mPlayerView = findViewById(R.id.jwplayer);

		mPlayer = mPlayerView.getPlayer();
		// Handle hiding/showing of ActionBar
		mPlayer.addListener(EventType.FULLSCREEN, this);

		// Keep the screen on during playback
		new KeepScreenOnHandler(mPlayer, getWindow());

		// Load a media source
		PlaylistItem pi = new PlaylistItem.Builder()
				.file("https://content.jwplatform.com/manifests/1sc0kL2N.m3u8")
				.title("Press Play")
				.image("https://content.jwplatform.com/thumbs/1sc0kL2N.jpg")
				.description("Press play with JW Player")
				.build();

		List<PlaylistItem> playlist = new ArrayList<>();
		playlist.add(pi);

		PlayerConfig playerConfig = new PlayerConfig.Builder()
				.playlist(playlist)
				.build();

		mPlayer.setup(playerConfig);
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
