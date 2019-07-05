package com.jwplayer.demo.fullbackgroundaudio;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

public class MediaStyleHelper {
    public static NotificationCompat.Builder prepareNotification(NotificationCompat.Builder builder, Context context, PlaylistItem item) {
        Intent deleteIntent = new Intent(context, BackgroundAudioService.class);
        deleteIntent.putExtra("ACTION", "ACTION_STOP");
        PendingIntent pendingIntentDelete = PendingIntent.getService(context, 0, deleteIntent,
                                                                      PendingIntent.FLAG_UPDATE_CURRENT);

        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntentContent = PendingIntent.getActivity(context, 1, contentIntent,
                                                                       PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle(item.getTitle())
                .setContentText(item.getDescription())
                .setSubText(item.getDescription())
                //.setLargeIcon(item.getImage())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntentContent)
                .setDeleteIntent(pendingIntentDelete)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        return builder;
    }
}
