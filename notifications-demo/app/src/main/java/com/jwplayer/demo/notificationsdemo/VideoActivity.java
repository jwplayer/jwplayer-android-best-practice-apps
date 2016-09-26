package com.jwplayer.demo.notificationsdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

public class VideoActivity extends AppCompatActivity implements VideoPlayerEvents.OnFullscreenListener {

	private static final String TAG = "VideoActivity";

	/**
	 * The JWPlayerView used for video playback.
	 */
	private JWPlayerView mPlayerView;

	/**
	 * Whether we have bound to a {@link MediaPlaybackService}.
	 */
	private boolean mIsBound;

	/**
	 * The {@link MediaPlaybackService} we are bound to.
	 */
	private MediaPlaybackService mMediaPlaybackService;

	/**
	 * The {@link ServiceConnection} serves as glue between this activity and the {@link MediaPlaybackService}.
	 */
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service.  Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mMediaPlaybackService = ((MediaPlaybackService.MediaPlaybackServiceBinder) service)
					.getService();
			mMediaPlaybackService.setActivePlayer(mPlayerView);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			mMediaPlaybackService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);

		// Create a JWPlayerConfig
		PlayerConfig playerConfig = new PlayerConfig.Builder()
				.playlist(Sample.PLAYLIST)
				.build();

		// Create a new JWPlayerView
		mPlayerView = new JWPlayerView(this, playerConfig);
		mPlayerView.addOnFullscreenListener(this);

		// Add the JWPlayerView to the screen. Make sure it's 16:9.
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
		container.addView(mPlayerView, new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, metrics.widthPixels / 16 * 9));

		// Bind to the MediaPlaybackService.
		doBindService();
	}

	@Override
	protected void onPause() {
		// Allow background audio playback.
		mPlayerView.setBackgroundAudio(true);
		mPlayerView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mPlayerView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		doUnbindService();
		mPlayerView.onDestroy();
		super.onDestroy();
	}

	private void doBindService() {
		// Establish a connection with the service.  We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(VideoActivity.this,
				MediaPlaybackService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	private void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mServiceConnection);
			mIsBound = false;
		}
	}

	@Override
	public void onFullscreen(boolean fullscreen) {
		if (fullscreen) {
			getSupportActionBar().hide();
		} else {
			getSupportActionBar().show();
		}
	}
}
