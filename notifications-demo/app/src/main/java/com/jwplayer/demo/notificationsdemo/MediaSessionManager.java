package com.jwplayer.demo.notificationsdemo;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.view.KeyEvent;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.core.PlayerState;
import com.longtailvideo.jwplayer.events.AdCompleteEvent;
import com.longtailvideo.jwplayer.events.AdPlayEvent;
import com.longtailvideo.jwplayer.events.AdSkippedEvent;
import com.longtailvideo.jwplayer.events.ErrorEvent;
import com.longtailvideo.jwplayer.events.listeners.AdvertisingEvents;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.List;

/**
 * Manages a {@link MediaSessionCompat}.
 */
public class MediaSessionManager implements VideoPlayerEvents.OnPlayListener,
		VideoPlayerEvents.OnPauseListener,
		VideoPlayerEvents.OnBufferListener,
		VideoPlayerEvents.OnErrorListenerV2,
		VideoPlayerEvents.OnPlaylistListener,
		VideoPlayerEvents.OnPlaylistItemListener,
		VideoPlayerEvents.OnPlaylistCompleteListener,
		AdvertisingEvents.OnAdPlayListenerV2,
		AdvertisingEvents.OnAdErrorListener,
		AdvertisingEvents.OnAdSkippedListenerV2,
		AdvertisingEvents.OnAdCompleteListenerV2 {

	/**
	 * Playback Rate for the JW Player is always 1.0.
	 */
	private static final float PLAYBACK_RATE = 1.0f;

	/**
	 * The notification id.
	 */
	private static final int NOTIFICATION_ID = 0;

	/**
	 * The Player we're managing the media session of.
	 */
	private JWPlayerView mPlayer;

	/**
	 * The current playlist index.
	 */
	private int mPlaylistIndex;

	/**
	 * The playlist that is currently loaded on the player.
	 */
	private List<PlaylistItem> mPlaylist;

	/**
	 * The underlying {@link MediaSessionCompat}.
	 */
	private MediaSessionCompat mMediaSessionCompat;

	/**
	 * Whether the {@link JWPlayerView} broadcasted an error.
	 */
	private boolean mReceivedError;

	/**
	 * An {@link android.os.AsyncTask} used for downloading artwork.
	 */
	private DownloadImageTask mDownloadImageTask;

	/**
	 * Initializes a new MediaSessionManager.
	 *
	 * @param context
	 * @param playerView
	 */
	public MediaSessionManager(Context context, JWPlayerView playerView) {
		mPlayer = playerView;

		// Create a new MediaSession
		mMediaSessionCompat = new MediaSessionCompat(context, MediaSessionManager.class.getSimpleName());
		mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
				| MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
		mMediaSessionCompat.setCallback(new MediaSessionCallback(mPlayer));

		// Register listeners.
		mPlayer.addOnPlayListener(this);
		mPlayer.addOnPauseListener(this);
		mPlayer.addOnBufferListener(this);
		mPlayer.addOnErrorListener(this);
		mPlayer.addOnPlaylistListener(this);
		mPlayer.addOnPlaylistItemListener(this);
		mPlayer.addOnPlaylistCompleteListener(this);
		mPlayer.addOnAdPlayListener(this);
		mPlayer.addOnErrorListener(this);
		mPlayer.addOnAdSkippedListener(this);
		mPlayer.addOnAdCompleteListener(this);
	}

	private @PlaybackStateCompat.Actions long getCapabilities(PlayerState playerState) {
		long capabilities = 0;

		switch (playerState) {
			case PLAYING:
				capabilities |= PlaybackStateCompat.ACTION_PAUSE
						| PlaybackStateCompat.ACTION_STOP
						| PlaybackStateCompat.ACTION_SEEK_TO;
				break;
			case PAUSED:
				capabilities |= PlaybackStateCompat.ACTION_PLAY
					| PlaybackStateCompat.ACTION_STOP
					| PlaybackStateCompat.ACTION_SEEK_TO;
				break;
			case BUFFERING:
				capabilities |= PlaybackStateCompat.ACTION_PAUSE
					| PlaybackStateCompat.ACTION_STOP
					| PlaybackStateCompat.ACTION_SEEK_TO;
				break;
			case IDLE:
				if (!mReceivedError && mPlaylist != null && mPlaylist.size() >= 1) {
					capabilities |= PlaybackStateCompat.ACTION_PLAY;
				}
				break;
		}

		if (mPlaylist != null && mPlaylist.size() - mPlaylistIndex > 1) {
			capabilities |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
		}

		if (mPlaylistIndex > 0 && mPlaylist != null && mPlaylist.size() > 1) {
			capabilities |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
		}

		return capabilities;
	}

	private NotificationCompat.Builder addActions(NotificationCompat.Builder notification,
												 long capabilities) {
		// Attach actions to the notification.
		if ((capabilities & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
			notification.addAction(R.drawable.ic_previous, "Previous",
					MediaNotificationUtil.getActionIntent(mPlayer.getContext(), KeyEvent.KEYCODE_MEDIA_PREVIOUS));
		}
		if ((capabilities & PlaybackStateCompat.ACTION_PAUSE) != 0) {
			notification.addAction(R.drawable.ic_pause, "Pause",
					MediaNotificationUtil.getActionIntent(mPlayer.getContext(), KeyEvent.KEYCODE_MEDIA_PAUSE));
		}
		if ((capabilities & PlaybackStateCompat.ACTION_PLAY) != 0) {
			notification.addAction(R.drawable.ic_play, "Play",
					MediaNotificationUtil.getActionIntent(mPlayer.getContext(), KeyEvent.KEYCODE_MEDIA_PLAY));
		}
		if ((capabilities & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
			notification.addAction(R.drawable.ic_next, "Next",
					MediaNotificationUtil.getActionIntent(mPlayer.getContext(), KeyEvent.KEYCODE_MEDIA_NEXT));
		}
		return notification;
	}

	private void updateNotification(long capabilities) {
		NotificationCompat.Builder notificationBuilder = MediaNotificationUtil.from(
				mPlayer.getContext(), mMediaSessionCompat);
		notificationBuilder = addActions(notificationBuilder, capabilities);

		NotificationManager notificationManager = (NotificationManager)
				mPlayer.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}

	private PlaybackStateCompat.Builder getPlaybackStateBuilder() {
		PlaybackStateCompat playbackState = mMediaSessionCompat.getController().getPlaybackState();
		return playbackState == null
				? new PlaybackStateCompat.Builder()
				: new PlaybackStateCompat.Builder(playbackState);
	}

	private void updatePlaybackState(PlayerState playerState) {
		PlaybackStateCompat.Builder newPlaybackState = getPlaybackStateBuilder();
		long capabilities = getCapabilities(playerState);
		//noinspection WrongConstant
		newPlaybackState.setActions(capabilities);

		int playbackStateCompat = PlaybackStateCompat.STATE_NONE;
		switch (playerState) {
			case PLAYING:
				playbackStateCompat = PlaybackStateCompat.STATE_PLAYING;
				break;
			case PAUSED:
				playbackStateCompat = PlaybackStateCompat.STATE_PAUSED;
				break;
			case BUFFERING:
				playbackStateCompat = PlaybackStateCompat.STATE_BUFFERING;
				break;
			case IDLE:
				if (mReceivedError) {
					playbackStateCompat = PlaybackStateCompat.STATE_ERROR;
				} else {
					playbackStateCompat = PlaybackStateCompat.STATE_STOPPED;
				}
				break;
		}
		newPlaybackState.setState(playbackStateCompat, mPlayer.getPosition(), PLAYBACK_RATE);
		mMediaSessionCompat.setPlaybackState(newPlaybackState.build());
		updateNotification(capabilities);
	}

	@Override
	public void onPlay(PlayerState oldState) {
		// Tell Android that we're playing media.
		mMediaSessionCompat.setActive(true);
		// Update the MediaSession
		updatePlaybackState(PlayerState.PLAYING);
	}

	@Override
	public void onPlaylistComplete() {
		//updatePlaybackState(PlayerState.IDLE);
		mMediaSessionCompat.setActive(false);
		mMediaSessionCompat.release();
		mPlaylistIndex = 0;
	}

	@Override
	public void onPlaylistItem(int index, PlaylistItem playlistItem) {
		mPlaylistIndex = index;

		// Update Metadata
		MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
				.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, playlistItem.getTitle())
				.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, playlistItem.getDescription())
				.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, playlistItem.getMediaId())
				.build();
		mMediaSessionCompat.setMetadata(metadata);

		// Fetch artwork
		if (mDownloadImageTask != null) {
			mDownloadImageTask.cancel(true);
		}
		mDownloadImageTask = new DownloadImageTask() {
			@Override
			protected void onPostExecute(Bitmap bitmap) {
				if (mMediaSessionCompat != null) {
					MediaMetadataCompat currentMetadata = mMediaSessionCompat.getController().getMetadata();
					MediaMetadataCompat.Builder newBuilder = currentMetadata == null
							? new MediaMetadataCompat.Builder()
							: new MediaMetadataCompat.Builder(currentMetadata);
					mMediaSessionCompat.setMetadata(newBuilder
							.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
							.build());
				}
			}
		};
		mDownloadImageTask.execute(playlistItem.getImage());
	}

	@Override
	public void onBuffer(PlayerState oldState) {
		// Update the PlaybackState.
		updatePlaybackState(PlayerState.BUFFERING);
	}

	@Override
	public void onPause(PlayerState oldState) {
		updatePlaybackState(PlayerState.PAUSED);
	}

	@Override
	public void onPlaylist(List<PlaylistItem> list) {
		mPlaylist = list;
	}

	@Override
	public void onError(ErrorEvent errorEvent) {
		mReceivedError = true;
	}

	@Override
	public void onAdComplete(AdCompleteEvent adCompleteEvent) {
		mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
				| MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
	}

	@Override
	public void onAdError(String s, String s1) {
		mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
				| MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
	}

	@Override
	public void onAdSkipped(AdSkippedEvent adSkippedEvent) {
		mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
				| MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
	}

	@Override
	public void onAdPlay(AdPlayEvent adPlayEvent) {
		// We don't want to show the notification during ad playback.
		NotificationManager notificationManager = (NotificationManager)
				mPlayer.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
		mMediaSessionCompat.setFlags(0);
	}

	/**
	 * Returns the underlying media session.
	 * @return
	 */
	public MediaSessionCompat getMediaSession() {
		return mMediaSessionCompat;
	}

	/**
	 * Releases this MediaSession.
	 */
	public void release() {
		mMediaSessionCompat.release();
		mPlayer.removeOnPlayListener(this);
		mPlayer.removeOnPauseListener(this);
		mPlayer.removeOnBufferListener(this);
		mPlayer.removeOnErrorListener(this);
		mPlayer.removeOnPlaylistListener(this);
		mPlayer.removeOnPlaylistItemListener(this);
		mPlayer.removeOnPlaylistCompleteListener(this);
		mPlayer.removeOnAdPlayListener(this);
		mPlayer.removeOnAdErrorListener(this);
		mPlayer.removeOnAdSkippedListener(this);
		mPlayer.removeOnAdCompleteListener(this);

		// Remove any notifications
		NotificationManager notificationManager = (NotificationManager)
				mPlayer.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
	}

	/**
	 * A {@link android.support.v4.media.session.MediaSessionCompat.Callback} implementation for JW Player.
	 */
	private final class MediaSessionCallback extends MediaSessionCompat.Callback {

		public MediaSessionCallback(JWPlayerView playerView) {
			mPlayer = playerView;
		}

		@Override
		public void onPause() {
			mPlayer.pause(true);
		}

		@Override
		public void onPlay() {
			mPlayer.play(true);
		}

		@Override
		public void onSeekTo(long pos) {
			mPlayer.seek(pos);
		}

		@Override
		public void onSkipToNext() {
			mPlayer.playlistItem(mPlayer.getPlaylistIndex() + 1);
		}

		@Override
		public void onSkipToPrevious() {
			mPlayer.playlistItem(mPlayer.getPlaylistIndex() - 1);
		}

		@Override
		public void onStop() {
			mPlayer.stop();
		}

		@Override
		public void onSkipToQueueItem(long id) {
			mPlayer.playlistItem((int)id);
		}
	}

}
