package com.jwplayer.demo.backgroundaudio;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.background.NotificationButton;
import com.jwplayer.pub.api.background.ServiceMediaApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a Like toggle and a Bookmark button to the media notification and lock-screen controls
 * with the declarative notification-button API (JW Player SDK 4.28.0+).
 *
 * <p>The SDK calls {@link #getNotificationButtons} whenever the player state, item or metadata
 * changes; call {@link #invalidateNotificationButtons()} when the buttons depend on something
 * else, such as the liked state here. The system shows a limited number of buttons (usually 3
 * to 5 including play/pause), so put the important ones first.
 */
public class LikeButtonsMediaApi extends ServiceMediaApi {

    private static final String ID_LIKE = "like";
    private static final String ID_BOOKMARK = "bookmark";

    private final Context mAppContext;
    private boolean mLiked;

    public LikeButtonsMediaApi(@NonNull JWPlayer player, @NonNull Context appContext) {
        super(player);
        mAppContext = appContext;
    }

    @NonNull
    @Override
    public List<NotificationButton> getNotificationButtons(@NonNull Context context) {
        // Keep the SDK's own transport controls (play/pause, next, previous, ...).
        List<NotificationButton> buttons = new ArrayList<>(super.getNotificationButtons(context));
        buttons.add(new NotificationButton.Builder(ID_LIKE, R.drawable.ic_like,
                context.getString(mLiked ? R.string.unlike : R.string.like))
                .inCompactView(true)
                .build());
        buttons.add(new NotificationButton.Builder(ID_BOOKMARK, R.drawable.ic_bookmark,
                context.getString(R.string.bookmark))
                .placement(NotificationButton.PLACEMENT_FORWARD)
                .build());
        return buttons;
    }

    @Override
    public void onNotificationButtonClick(@NonNull String buttonId) {
        // Called on the main thread, from the notification, the lock screen, Android 13+ system
        // media controls, Bluetooth and Android Auto alike.
        if (ID_LIKE.equals(buttonId)) {
            mLiked = !mLiked;
            invalidateNotificationButtons(); // re-label the button: Like <-> Unlike
            Toast.makeText(mAppContext, mLiked ? "Liked" : "Unliked", Toast.LENGTH_SHORT).show();
        } else if (ID_BOOKMARK.equals(buttonId)) {
            Toast.makeText(mAppContext, "Bookmarked", Toast.LENGTH_SHORT).show();
        }
    }
}
