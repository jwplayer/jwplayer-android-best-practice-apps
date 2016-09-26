package com.jwplayer.demo.notificationsdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.view.KeyEvent;

/**
 * Utility class for creating Media related notifications.
 */
public class MediaNotificationUtil {

	/**
	 * Creates a new Notification builder from an existing media session.
	 * @param context
	 * @param mediaSession
	 * @return
	 */
	public static NotificationCompat.Builder from(Context context, MediaSessionCompat mediaSession) {
		MediaControllerCompat controller = mediaSession.getController();
		MediaMetadataCompat mediaMetadata = controller.getMetadata();
		MediaDescriptionCompat description = mediaMetadata.getDescription();

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle(description.getTitle())
				.setContentText(description.getSubtitle())
				.setSubText(description.getDescription())
				.setLargeIcon(description.getIconBitmap())
				.setStyle(new NotificationCompat.MediaStyle()
						.setMediaSession(mediaSession.getSessionToken()))
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setSmallIcon(R.drawable.ic_jw_developer)
				.setDeleteIntent(getActionIntent(context, KeyEvent.KEYCODE_MEDIA_STOP));

		if (context instanceof Activity) {
			// We want to resume the existing VideoActivity, over creating a new one.
			Intent intent = new Intent(context, context.getClass());
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			builder.setContentIntent(pendingIntent);
		}

		return builder;
	}

	public static PendingIntent getActionIntent(Context context, int mediaKeyEvent) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.setPackage(context.getPackageName());
		intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, mediaKeyEvent));
		return PendingIntent.getBroadcast(context, mediaKeyEvent, intent, 0);
	}
}
