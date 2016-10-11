package com.jwplayer.demo.notificationsdemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaButtonReceiver;

import com.longtailvideo.jwplayer.JWPlayerView;

/**
 * Manages the {@link android.media.session.MediaSession} and responds to {@link MediaButtonReceiver}
 * events.
 */
public class MediaPlaybackService extends Service {

	/**
	 * The binder used by clients to access this instance.
	 */
	private final Binder mBinder = new MediaPlaybackServiceBinder();

	/**
	 * The MediaSession used to control this service.
	 */
	private MediaSessionManager mMediaSession;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mMediaSession != null) {
			MediaButtonReceiver.handleIntent(mMediaSession.getMediaSession(), intent);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onDestroy() {
		mMediaSession.release();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// Stop this service when all clients have been unbound.
		mMediaSession.release();
		stopSelf();
		return false;
	}

	/**
	 * Used to set a player to control the MediaSession for.
	 * @param player the player that should be controlled by this service.
	 */
	public void setActivePlayer(JWPlayerView player) {
		if (mMediaSession != null) {
			mMediaSession.release();
		}
		mMediaSession = new MediaSessionManager(this, player);
	}

	/**
	 * Clients access this service through this class.
	 * Because we know this service always runs in the same process
	 * as its clients, we don't need to deal with IPC.
	 */
	public class MediaPlaybackServiceBinder extends Binder {
		MediaPlaybackService getService() {
			return MediaPlaybackService.this;
		}
	}
}
