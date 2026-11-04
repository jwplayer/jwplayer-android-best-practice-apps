package com.jwplayer.demo.chromecastdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;


public class JWPlayerViewExample extends AppCompatActivity
		implements VideoPlayerEvents.OnFullscreenListener {

	// Back leaves fullscreen. Enabled only while the player is fullscreen, so otherwise Back
	// behaves normally. Uses the OnBackPressedDispatcher rather than onKeyDown(KEYCODE_BACK).
	private final OnBackPressedCallback mExitFullscreenOnBack = new OnBackPressedCallback(false) {
		@Override
		public void handleOnBackPressed() {
			mPlayer.setFullscreen(false, true);
		}
	};

	private JWPlayerView mPlayerView;
	private JWPlayer mPlayer;

	private static final String TAG = "ChromecastDemo";

	private CastContext mCastContext;
	private AppBarLayout mAppBarLayout;

	private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD = "com.google.market";
	private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW = "com.android.vending";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		setContentView(R.layout.activity_jwplayerview);

		mAppBarLayout = findViewById(R.id.app_bar_layout);
		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Handle status bar insets
		ViewCompat.setOnApplyWindowInsetsListener(mAppBarLayout, (v, windowInsets) -> {
			Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
			v.setPadding(0, statusBars.top, 0, 0);
			return windowInsets;
		});

		if (isGoogleApiAvailable(this)) {
			// The main executor delivers the result on the UI thread, where mCastContext is read.
			Task<CastContext> task = CastContext.getSharedInstance(this,
					ContextCompat.getMainExecutor(this));
			task.addOnCompleteListener(result -> {
				if (result.isSuccessful()) {
					mCastContext = result.getResult();
				} else {
					Log.w(TAG, "CastContext unavailable; casting is disabled", result.getException());
				}
			});
		}

		// INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
		// [OR] change in app-level build.gradle
		// [OR] set JWPLAYER_LICENSE_KEY as environment variable
		new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

		mPlayerView = findViewById(R.id.jwplayer);

		mPlayer = mPlayerView.getPlayer(this);
		// Handle hiding/showing of ActionBar
		mPlayer.addListener(EventType.FULLSCREEN, this);

		// Keep the screen on during playback
		new KeepScreenOnHandler(mPlayer, getWindow());

		// Load a media source
		PlaylistItem pi = new PlaylistItem.Builder()
				.file("https://cdn.jwplayer.com/manifests/Gihxhhy6.m3u8")
				.title("Chromecast demo")
				.description("Tap the cast button in the player controls to cast this video")
				.build();

		List<PlaylistItem> playlist = new ArrayList<>();
		playlist.add(pi);

		PlayerConfig playerConfig = new PlayerConfig.Builder()
				.playlist(playlist)
				.build();

		mPlayer.setup(playerConfig);

		getOnBackPressedDispatcher().addCallback(this, mExitFullscreenOnBack);
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
	public void onFullscreen(FullscreenEvent fullscreenEvent) {
		mExitFullscreenOnBack.setEnabled(fullscreenEvent.getFullscreen());
		if (mAppBarLayout != null) {
			boolean isCasting = mCastContext != null && mCastContext
					.getCastState() == CastState.CONNECTED;
			if (fullscreenEvent.getFullscreen() && !isCasting) {
				mAppBarLayout.setVisibility(View.GONE);
			} else {
				mAppBarLayout.setVisibility(View.VISIBLE);
			}
		}
	}

}
