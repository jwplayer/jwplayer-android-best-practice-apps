package com.jwplayer.demo.backgroundaudio;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.background.ServiceMediaApi;

/**
 * The pre-4.28.0 way of customising the notification, still supported: add
 * {@link NotificationCompat.Action}s in {@link #addNotificationActions} and choose the collapsed
 * view with {@link #getCompactActions()}. New code should prefer
 * {@code getNotificationButtons} (see {@link LikeButtonsMediaApi}).
 *
 * <p>Since 4.28.0 the actions are rendered exactly as added, so add play/pause yourself.
 * {@link #getActionIntent} makes an action the native control for a media key. For your own
 * actions use a broadcast {@link PendingIntent}: Android 13+ blocks an Activity intent started
 * from the system media controls while the app is in the background.
 */
@SuppressWarnings("deprecation")
public class LegacyActionsMediaApi extends ServiceMediaApi {

    static final String ACTION_REWIND = "com.jwplayer.demo.backgroundaudio.REWIND";

    public LegacyActionsMediaApi(JWPlayer player) {
        super(player);
    }

    @Override
    public void addNotificationActions(Context context, NotificationCompat.Builder builder) {
        // Called on every player event: return the same PendingIntents each time.
        builder.addAction(R.drawable.ic_rewind, context.getString(R.string.rewind),
                PendingIntent.getBroadcast(context, 0,
                        new Intent(ACTION_REWIND).setClass(context, ActionReceiver.class),
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        builder.addAction(R.drawable.ic_play_pause, context.getString(R.string.play_pause),
                getActionIntent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    }

    @Override
    public int[] getCompactActions() {
        return new int[]{0, 1};
    }

    /** Handles the custom rewind action. Declared in the manifest, not exported. */
    public static class ActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_REWIND.equals(intent.getAction())) {
                Toast.makeText(context, "Rewind 10 s", Toast.LENGTH_SHORT).show();
                // Your app would seek here, e.g. through a reference to the JWPlayer it owns.
            }
        }
    }
}
