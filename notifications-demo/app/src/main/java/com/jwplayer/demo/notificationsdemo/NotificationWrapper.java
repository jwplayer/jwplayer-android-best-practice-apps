package com.jwplayer.demo.notificationsdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;

public class NotificationWrapper {

	private final String NOTIFICATION_CHANNEL_ID = "NotificationBarController";
	public static final int NOTIFICATION_ID = 1969;
	private NotificationManager mNotificationManager;
	private NotificationChannel mNotificationChannel;

	public NotificationWrapper(NotificationManager notificationManager) {
		this.mNotificationManager = notificationManager;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel();
		}

	}

	@RequiresApi(Build.VERSION_CODES.O)
	private void createNotificationChannel() {
		CharSequence channelNameDisplayedToUser = "Notification Bar Video Controls";
		int importance = NotificationManager.IMPORTANCE_LOW;
		mNotificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
													   channelNameDisplayedToUser,
													   importance);
		mNotificationChannel.setDescription("All notifications");
		mNotificationChannel.setShowBadge(false);
		mNotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
		mNotificationManager.createNotificationChannel(mNotificationChannel);
	}

	public Notification createNotification(Context context,
										   MediaSessionCompat mediaSession,
										   long capabilities
	) {

		// Media metadata
		MediaControllerCompat controller = mediaSession.getController();
		MediaMetadataCompat mediaMetadata = controller.getMetadata();
		MediaDescriptionCompat description = mediaMetadata.getDescription();

		// Notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
																			NOTIFICATION_CHANNEL_ID);
		builder.setContentTitle(description.getTitle())
			   .setContentText(description.getSubtitle())
			   .setSubText(description.getDescription())
			   .setLargeIcon(description.getIconBitmap())
			   .setOnlyAlertOnce(true)
			   .setStyle(new MediaStyle()
								 .setMediaSession(mediaSession.getSessionToken()))
			   .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
			   .setSmallIcon(R.drawable.ic_jw_developer)
			   .setDeleteIntent(getActionIntent(context, PlaybackStateCompat.ACTION_STOP));


		// Attach actions to the notification.
		if ((capabilities & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
			builder.addAction(R.drawable.ic_previous, "Previous",
							  getActionIntent(context,
											  PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
		}
		if ((capabilities & PlaybackStateCompat.ACTION_PAUSE) != 0) {
			builder.addAction(R.drawable.ic_pause, "Pause",
							  getActionIntent(context,
											  PlaybackStateCompat.ACTION_PAUSE));
		}
		if ((capabilities & PlaybackStateCompat.ACTION_PLAY) != 0) {
			builder.addAction(R.drawable.ic_play, "Play",
							  getActionIntent(context,
											  PlaybackStateCompat.ACTION_PLAY)
			);
		}
		if ((capabilities & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
			builder.addAction(R.drawable.ic_next, "Next",
							  getActionIntent(context,
											  PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
		}

		// We want to resume the existing VideoActivity, over creating a new one.
		Intent intent = new Intent(context, context.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
		builder.setContentIntent(pendingIntent);

		Notification notification = builder.build();

		mNotificationManager.notify(NOTIFICATION_ID, notification);

		return notification;
	}

	/**
	 * Builds a MEDIA_BUTTON PendingIntent for the given action. androidx's MediaButtonReceiver
	 * (declared in the manifest) receives it and forwards it to MediaPlaybackService, which hands it
	 * to the MediaSession via MediaButtonReceiver.handleIntent().
	 */
	private PendingIntent getActionIntent(Context context, @PlaybackStateCompat.MediaKeyAction long action) {
		return MediaButtonReceiver.buildMediaButtonPendingIntent(context, action);
	}

	void cancelNotification() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
}
