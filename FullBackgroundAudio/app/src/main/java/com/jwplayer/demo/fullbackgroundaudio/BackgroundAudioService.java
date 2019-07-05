/**
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
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.core.PlayerState;
import com.longtailvideo.jwplayer.events.PauseEvent;
import com.longtailvideo.jwplayer.events.PlayEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;


public class BackgroundAudioService extends Service {

    private static final float PLAYBACK_SPEED = 1.0f;

    private JWPlayerView mPlayer;

    private MediaSessionCompat mMediaSessionCompat;

    private PlayerEventHandler eventHandler = new PlayerEventHandler();

    private PlaybackStateCompat.Builder mPlaybackStateBuilder = new PlaybackStateCompat.Builder();
    private MediaMetadataCompat.Builder mMediaMetadataBuilder = new MediaMetadataCompat.Builder();

    private ServiceBinder mBinder = new ServiceBinder();


    @Override
    public void onCreate() {
        mMediaSessionCompat = new MediaSessionCompat(this, getClass().getSimpleName());
        mMediaSessionCompat.setCallback(new MediaSessionCallback());
        mMediaSessionCompat.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra("ACTION");
        switch (action) {
            case "ACTION_START": {
                //START FOREGROUND SERVICE
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID);
                builder.setContentTitle(mPlayer.getPlaylistItem().getTitle())
                .setContentText(mPlayer.getPlaylistItem().getDescription())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(mPlayer.getPlaylistItem().getDescription());
                startForeground(App.NOTIFICATION_ID, builder.build());
                setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                break;
            }
            case "ACTION_PLAY": {
                mMediaSessionCompat.getController().getTransportControls().play();
                setPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                break;
            }
            case "ACTION_PAUSE": {
                mMediaSessionCompat.getController().getTransportControls().pause();
                setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                break;
            }
            case "ACTION_STOP": {
                mMediaSessionCompat.getController().getTransportControls().stop();
                setPlaybackState(PlaybackStateCompat.STATE_STOPPED);
            }
        }
        showNotification();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.removeOnPlayListener(eventHandler);
        mPlayer.removeOnPauseListener(eventHandler);
        mPlayer.stop();
        mMediaSessionCompat.release();
        NotificationManagerCompat.from(this).cancel(App.NOTIFICATION_ID);
        stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    private void setPlaybackState(int state) {
        mPlaybackStateBuilder.setState(state, (long) mPlayer.getPosition(), PLAYBACK_SPEED);
        setActions(state);
        mMediaSessionCompat.setPlaybackState(mPlaybackStateBuilder.build());
    }

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

    private void showNotification() {
        //Sets the common parameters for all notifications
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this, App.CHANNEL_ID);
        MediaStyleHelper.prepareNotification(mNotificationBuilder, mPlayer.getContext(), mPlayer.getPlaylistItem());
        mNotificationBuilder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setMediaSession(mMediaSessionCompat.getSessionToken()));
        //Add Actions to the notification
        if(mMediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            Intent pauseIntent = new Intent(this, BackgroundAudioService.class);
            pauseIntent.putExtra("ACTION", "ACTION_PAUSE");
            PendingIntent pendingIntent = PendingIntent.getService(this, 2, pauseIntent,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationBuilder.addAction(R.drawable.ic_pause, "Pause", pendingIntent);
        }
        if(mMediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
            Intent playIntent = new Intent(this, BackgroundAudioService.class);
            playIntent.putExtra("ACTION", "ACTION_PLAY");
            PendingIntent pendingIntent = PendingIntent.getService(this, 3, playIntent,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationBuilder.addAction(R.drawable.ic_play, "Play", pendingIntent);
        }
        NotificationManagerCompat.from(this).notify(App.NOTIFICATION_ID, mNotificationBuilder.build());
    }

    public class ServiceBinder extends Binder {

        public void createPLayer(Context context) {
            if(context instanceof Activity){
                PlayerConfig config = new PlayerConfig.Builder().playlist(SampleMedia.SAMPLE).build();
                mPlayer = new JWPlayerView(context, config);
                mPlayer.addOnPlayListener(eventHandler);
                mPlayer.addOnPauseListener(eventHandler);
            }
        }

        public JWPlayerView getPlayer() {
            return mPlayer;
        }
    }

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
