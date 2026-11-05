package com.jwplayer.demo.backgroundaudio;

import android.app.PendingIntent;
import android.content.Context;
import android.view.KeyEvent;

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
 * {@link #getActionIntent} makes an action the native control for a media key: here
 * {@code KEYCODE_MEDIA_REWIND} seeks back 10 seconds ({@link ServiceMediaApi#onRewind()}) and
 * {@code KEYCODE_MEDIA_PLAY_PAUSE} toggles playback.
 *
 * <p>Any other action is sent as its own {@link PendingIntent}. Use a broadcast for those:
 * Android 13+ blocks an Activity intent started from the system media controls while the app is
 * in the background. Note that a manifest-declared receiver has no reference to the player, so it
 * cannot act on it. To run your own code against the player from a notification tap, use
 * {@code getNotificationButtons} and {@code onNotificationButtonClick} instead.
 */
@SuppressWarnings("deprecation")
public class LegacyActionsMediaApi extends ServiceMediaApi {

    public LegacyActionsMediaApi(JWPlayer player) {
        super(player);
    }

    @Override
    public void addNotificationActions(Context context, NotificationCompat.Builder builder) {
        // Called on every player event: return the same PendingIntents each time.
        builder.addAction(R.drawable.ic_rewind, context.getString(R.string.rewind),
                getActionIntent(context, KeyEvent.KEYCODE_MEDIA_REWIND));
        builder.addAction(R.drawable.ic_play_pause, context.getString(R.string.play_pause),
                getActionIntent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    }

    @Override
    public int[] getCompactActions() {
        return new int[]{0, 1};
    }
}
