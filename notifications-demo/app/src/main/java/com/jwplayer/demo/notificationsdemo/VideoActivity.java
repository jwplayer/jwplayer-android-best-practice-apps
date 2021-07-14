package com.jwplayer.demo.notificationsdemo;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FirstFrameEvent;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.view.JWPlayerView;

public class VideoActivity extends AppCompatActivity implements
													 VideoPlayerEvents.OnFullscreenListener,
													 VideoPlayerEvents.OnFirstFrameListener {

	/**
	 * The JWPlayerView used for video playback.
	 */
	private JWPlayerView mPlayerView;
	private JWPlayer mPlayer;

	/**
	 * Whether we have bound to a {@link MediaPlaybackService}.
	 */
	private boolean mIsBound = false;

	/**
	 * The {@link MediaPlaybackService} we are bound to. T
	 */
	private MediaPlaybackService mMediaPlaybackService;

	/**
	 * The {@link MediaSessionManager} handles the MediaSession logic, along with updates to the notification
	 */
	private MediaSessionManager mMediaSessionManager;

	/**
	 * The {@link MediaSessionManager} handles the Notification set and dismissal logic
	 */
	private NotificationWrapper mNotificationWrapper;

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
			mIsBound = true;
			mMediaPlaybackService = ((MediaPlaybackService.MediaPlaybackServiceBinder)service)
					.getService();
			mMediaPlaybackService.setupMediaSession(mMediaSessionManager, mNotificationWrapper);
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

		LicenseUtil.setLicenseKey(this, YOUR_LICENSE_KEY);

		// Create a JWPlayerConfig
		PlayerConfig playerConfig = new PlayerConfig.Builder()
				.playlist(Sample.PLAYLIST)
				.build();

		// Create a new JWPlayerView
		mPlayerView = new JWPlayerView(this, null);
		mPlayer = mPlayerView.getPlayer();
		mPlayer.addListener(EventType.FULLSCREEN, this);
		mPlayer.addListener(EventType.FIRST_FRAME, this);
		mPlayer.setup(playerConfig);

		// Add the JWPlayerView to the screen. Make sure it's 16:9.
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		RelativeLayout container = findViewById(R.id.container);
		container.addView(mPlayerView, new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, metrics.widthPixels / 16 * 9));

		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationWrapper = new NotificationWrapper(notificationManager);
		mMediaSessionManager = new MediaSessionManager(this,
													   mPlayerView,
													   mNotificationWrapper);
	}

	private void doBindService() {
		// Establish a connection with the service.  We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(VideoActivity.this,
							   MediaPlaybackService.class),
					mServiceConnection,
					Context.BIND_AUTO_CREATE);

	}

	private void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mServiceConnection);
			mIsBound = false;
		}
	}

	@Override
	public void onFullscreen(FullscreenEvent fullscreenEvent) {
		if (fullscreenEvent.getFullscreen()) {
			getSupportActionBar().hide();
		} else {
			getSupportActionBar().show();
		}
	}

	@Override
	public void onFirstFrame(FirstFrameEvent firstFrameEvent) {
		// Only bind to the service if media has begun playback
		// You could also use onBeforePlay as your listener
		// if you wanted to start the service and notification earlier
		if (!mIsBound) {
			doBindService();
		}
	}
}
