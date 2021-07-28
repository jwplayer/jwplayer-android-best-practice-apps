/*
 * Author: Efrain Gonzalez
 * c-egonzalez@jwplayer.com
 */

package com.jwplayer.demo.fullbackgroundaudio;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.PauseEvent;
import com.jwplayer.pub.api.events.PlayEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

/**
 *
 * This class holds the core functionality for background playback. Represents a Service that
 * binds to an activity to allow interaction trough a Binder interface, can also be started to live
 * detached from the launching activity, and is able to run in the foreground (Showing a
 * persistent notification to the user). This class makes use of a MediaSession to handle
 * transport controls and media notifications.
 */
public class BackgroundAudioService extends Service {

    private static final float PLAYBACK_SPEED = 1.0f;
    
    public static final String ACTION = "ACTION";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_STOP = "ACTION_STOP";

    private JWPlayerView mPlayerView;
    private JWPlayer mPlayer;

    private MediaSessionCompat mMediaSessionCompat;

    private PlayerEventHandler eventHandler = new PlayerEventHandler();

    private PlaybackStateCompat.Builder mPlaybackStateBuilder = new PlaybackStateCompat.Builder();

    private ServiceBinder mBinder = new ServiceBinder();
    
    
    /**
     * When the service is created, create and prepare a MediaSession
     */
    @Override
    public void onCreate() {
        mMediaSessionCompat = new MediaSessionCompat(this, getClass().getSimpleName());
        mMediaSessionCompat.setCallback(new MediaSessionCallback());
        mMediaSessionCompat.setActive(true);
    }
    
    /**
     * This method gets called each time a call to this service is made
     * we use the {@param intent} to determine the action to be performed.
     *
     * When the service is first bound, an intent with ACTION_START is launched, and the
     * startForeground method gets called, this creates the notification and also promotes the
     * service from background to foreground and prevents the system from killing it.
     *
     * Please note that startForeground() should be called within 5 seconds of calling
     * startForegroundService() in the activity, otherwise the service will be killed and the app
     * will crash with an error.
     *
     * When an ACTION intent is received, we call the TransportControls method to tell the media
     * session an event occurred and invoke it's callback that will forward the event to the
     * player. We also set the PlaybackState to keep the mediaSession and the JWPlayerView in
     * sync.
     *
     * Finally, the notification gets updated every time the Service receives an Intent.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra(ACTION);
        MediaControllerCompat.TransportControls transportControls =
                mMediaSessionCompat.getController().getTransportControls();
        PlaylistItem item = mPlayer.getPlaylistItem();
        switch (action) {
            case ACTION_START: {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                                                                                    App.CHANNEL_ID)
                .setContentTitle(item.getTitle())
                .setContentText(item.getDescription())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(item.getDescription());
                
                startForeground(App.NOTIFICATION_ID, builder.build());
                setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                break;
            }
            case ACTION_PLAY: {
                transportControls.play();
                setPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                break;
            }
            case ACTION_PAUSE: {
                transportControls.pause();
                setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                break;
            }
            case ACTION_STOP: {
                transportControls.stop();
                setPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                break;
            }
        }
        showNotification();
        return START_NOT_STICKY;
    }
    
    /**
     * When an Activity bind to this service, an instance of the Binder interface is returned to
     * allow interaction.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    /**
     * Free resources when the service is destroyed.
     * Clear the service's notification.
     * Stop the service.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.removeListener(EventType.PLAY, eventHandler);
        mPlayer.removeListener(EventType.PAUSE, eventHandler);
        mPlayer.stop();
        mMediaSessionCompat.release();
        NotificationManagerCompat.from(this).cancel(App.NOTIFICATION_ID);
        stopSelf();
    }
    
    /**
     * Stop the service when the user swipes away the app from the recent app view
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
    
    /**
     * Updates the current payback state
     */
    private void setPlaybackState(int state) {
        mPlaybackStateBuilder.setState(state, (long) mPlayer.getPosition(), PLAYBACK_SPEED);
        setActions(state);
        mMediaSessionCompat.setPlaybackState(mPlaybackStateBuilder.build());
    }
    
    /**
     * Sets the available actions for the current state.
     */
    private void setActions(int state) {
        switch (state) {
            case PlaybackStateCompat.STATE_PLAYING: {
                mPlaybackStateBuilder.setActions(PlaybackStateCompat.ACTION_PAUSE);
                break;
            }
            case PlaybackStateCompat.STATE_PAUSED: {
                mPlaybackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY);
                break;
            }
        }
    }
    
    /**
     * Updates the notification to be shown. We use a notification builder to create a whole new
     * notification but we use the notify() method and the same notification id to update it. It
     * is important to use the same id so the system knows that the foreground service still has
     * a notification (created in startForeground).
     */
    private void showNotification() {
        //Sets the common parameters for all notifications
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this, App.CHANNEL_ID);
        MediaStyleHelper.prepareNotification(mNotificationBuilder, mPlayerView.getContext(), mPlayer.getPlaylistItem());

        mNotificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mMediaSessionCompat.getSessionToken()));
        //Add Actions to the notification
        if(mMediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            Intent pauseIntent = new Intent(this, BackgroundAudioService.class);
            pauseIntent.putExtra(ACTION, ACTION_PAUSE);
            PendingIntent pendingIntent = PendingIntent.getService(this, 2, pauseIntent,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationBuilder.addAction(R.drawable.ic_pause, "Pause", pendingIntent);
        }
        if(mMediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
            Intent playIntent = new Intent(this, BackgroundAudioService.class);
            playIntent.putExtra(ACTION, ACTION_PLAY);
            PendingIntent pendingIntent = PendingIntent.getService(this, 3, playIntent,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationBuilder.addAction(R.drawable.ic_play, "Play", pendingIntent);
        }
        NotificationManagerCompat.from(this).notify(App.NOTIFICATION_ID, mNotificationBuilder.build());
    }
    
    /**
     * This Binder interface provides interaction between the service and the binding activity by
     * returning an instance of this Binder and allowing the activity to call its methods.
     *
     * Other custom methods can be defined here.
     *
     * Instead of defining several custom methods, the Binder interface can return an instance of
     * this service to allow the activity to call the service methods directly, but this approach
     * is more dangerous.
     */
    class ServiceBinder extends Binder {

        public void createPlayer(Context context) {
            if(context instanceof Activity){
                PlayerConfig config = new PlayerConfig.Builder().playlist(SampleMedia.SAMPLE).build();
                mPlayerView = new JWPlayerView(context, null);
                mPlayer = mPlayerView.getPlayer();
                mPlayer.addListener(EventType.PLAY, eventHandler);
                mPlayer.addListener(EventType.PAUSE, eventHandler);
                mPlayer.setup(config);
            }
        }

        public JWPlayerView getPlayerView() {
            return mPlayerView;
        }
    }
    
    /**
     * This class must override all the methods of the actions supported by each
     * application.
     * As minimum, it should override onPlay() and onPause().
     * If the application supports skipping tracks, this class should also implement onSkipToNext
     * () and onSkipToPrevious().
     *
     * Every method should forward the behavior to the JWPlayerView instance to keep sync between
     * itself and the mediaSession.
     */
    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            mPlayer.play();
            super.onPlay();
        }

        @Override
        public void onPause() {
            mPlayer.pause();
            super.onPause();
        }
        
        
    }
    
    /**
     * This class must implement all the player event listeners needed for each application.
     * As minimum, it should implement onPLayListener and onPauseListener and update the
     * playbackState and the notification, to keep sync between the JWPlayerView instance and the
     * mediaSession.
     */
    private class PlayerEventHandler implements VideoPlayerEvents.OnPlayListener,
                                                VideoPlayerEvents.OnPauseListener {

        @Override
        public void onPause(PauseEvent pauseEvent) {
            setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            showNotification();
        }

        @Override
        public void onPlay(PlayEvent playEvent) {
            setPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            showNotification();
        }
    }
}
