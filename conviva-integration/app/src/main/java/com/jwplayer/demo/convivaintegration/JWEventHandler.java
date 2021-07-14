package com.jwplayer.demo.convivaintegration;

import android.util.Log;
import android.widget.TextView;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.events.AdClickEvent;
import com.jwplayer.pub.api.events.AdCompleteEvent;
import com.jwplayer.pub.api.events.AdErrorEvent;
import com.jwplayer.pub.api.events.AdImpressionEvent;
import com.jwplayer.pub.api.events.AdPauseEvent;
import com.jwplayer.pub.api.events.AdPlayEvent;
import com.jwplayer.pub.api.events.AdScheduleEvent;
import com.jwplayer.pub.api.events.AdSkippedEvent;
import com.jwplayer.pub.api.events.AdTimeEvent;
import com.jwplayer.pub.api.events.AudioTrackChangedEvent;
import com.jwplayer.pub.api.events.AudioTracksEvent;
import com.jwplayer.pub.api.events.BeforeCompleteEvent;
import com.jwplayer.pub.api.events.BeforePlayEvent;
import com.jwplayer.pub.api.events.BufferEvent;
import com.jwplayer.pub.api.events.CaptionsChangedEvent;
import com.jwplayer.pub.api.events.CaptionsListEvent;
import com.jwplayer.pub.api.events.CompleteEvent;
import com.jwplayer.pub.api.events.ControlsEvent;
import com.jwplayer.pub.api.events.DisplayClickEvent;
import com.jwplayer.pub.api.events.ErrorEvent;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FirstFrameEvent;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.IdleEvent;
import com.jwplayer.pub.api.events.LevelsChangedEvent;
import com.jwplayer.pub.api.events.LevelsEvent;
import com.jwplayer.pub.api.events.MetaEvent;
import com.jwplayer.pub.api.events.MuteEvent;
import com.jwplayer.pub.api.events.PauseEvent;
import com.jwplayer.pub.api.events.PlayEvent;
import com.jwplayer.pub.api.events.PlaylistCompleteEvent;
import com.jwplayer.pub.api.events.PlaylistEvent;
import com.jwplayer.pub.api.events.PlaylistItemEvent;
import com.jwplayer.pub.api.events.SeekEvent;
import com.jwplayer.pub.api.events.SeekedEvent;
import com.jwplayer.pub.api.events.SetupErrorEvent;
import com.jwplayer.pub.api.events.TimeEvent;
import com.jwplayer.pub.api.events.VisualQualityEvent;
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;

/**
 * Outputs all JW Player Events to logging, with the exception of time events.
 */
public class JWEventHandler implements VideoPlayerEvents.OnSetupErrorListener,
        VideoPlayerEvents.OnPlaylistListener,
        VideoPlayerEvents.OnPlaylistItemListener,
        VideoPlayerEvents.OnPlayListener,
        VideoPlayerEvents.OnPauseListener,
        VideoPlayerEvents.OnBufferListener,
        VideoPlayerEvents.OnIdleListener,
        VideoPlayerEvents.OnErrorListener,
        VideoPlayerEvents.OnSeekListener,
        VideoPlayerEvents.OnTimeListener,
        VideoPlayerEvents.OnFullscreenListener,
        VideoPlayerEvents.OnAudioTracksListener,
        VideoPlayerEvents.OnAudioTrackChangedListener,
        VideoPlayerEvents.OnCaptionsListListener,
        VideoPlayerEvents.OnMetaListener,
        VideoPlayerEvents.OnPlaylistCompleteListener,
        VideoPlayerEvents.OnCompleteListener,
        VideoPlayerEvents.OnLevelsChangedListener,
        VideoPlayerEvents.OnLevelsListener,
        VideoPlayerEvents.OnCaptionsChangedListener,
        VideoPlayerEvents.OnControlsListener,
        VideoPlayerEvents.OnDisplayClickListener,
        VideoPlayerEvents.OnMuteListener,
        VideoPlayerEvents.OnSeekedListener,
        VideoPlayerEvents.OnVisualQualityListener,
        VideoPlayerEvents.OnFirstFrameListener,
        AdvertisingEvents.OnAdClickListener,
        AdvertisingEvents.OnAdCompleteListener,
        AdvertisingEvents.OnAdSkippedListener,
        AdvertisingEvents.OnAdErrorListener,
        AdvertisingEvents.OnAdImpressionListener,
        AdvertisingEvents.OnAdTimeListener,
        AdvertisingEvents.OnAdPauseListener,
        AdvertisingEvents.OnAdPlayListener,
        AdvertisingEvents.OnAdScheduleListener,
        AdvertisingEvents.OnBeforePlayListener,
        AdvertisingEvents.OnBeforeCompleteListener {
    private String TAG = JWEventHandler.class.getName();

    TextView mOutput;

    public JWEventHandler(JWPlayer player, TextView output) {
        mOutput = output;
        // Subscribe to all JW Player events
        player.addListener(EventType.FIRST_FRAME, this);
        player.addListener(EventType.SETUP_ERROR, this);
        player.addListener(EventType.PLAYLIST, this);
        player.addListener(EventType.PLAYLIST_ITEM, this);
        player.addListener(EventType.PLAY, this);
        player.addListener(EventType.PAUSE, this);
        player.addListener(EventType.BUFFER, this);
        player.addListener(EventType.IDLE, this);
        player.addListener(EventType.ERROR, this);
        player.addListener(EventType.SEEK, this);
        player.addListener(EventType.TIME, this);
        player.addListener(EventType.FULLSCREEN, this);
        player.addListener(EventType.LEVELS, this);
        player.addListener(EventType.LEVELS_CHANGED, this);
        player.addListener(EventType.CAPTIONS_LIST, this);
        player.addListener(EventType.CAPTIONS_CHANGED, this);
        player.addListener(EventType.CONTROLS, this);
        player.addListener(EventType.DISPLAY_CLICK, this);
        player.addListener(EventType.MUTE, this);
        player.addListener(EventType.VISUAL_QUALITY, this);
        player.addListener(EventType.SEEKED, this);
        player.addListener(EventType.AD_CLICK, this);
        player.addListener(EventType.AD_COMPLETE, this);
        player.addListener(EventType.AD_SKIPPED, this);
        player.addListener(EventType.AD_ERROR, this);
        player.addListener(EventType.AD_IMPRESSION, this);
        player.addListener(EventType.AD_TIME, this);
        player.addListener(EventType.AD_PAUSE, this);
        player.addListener(EventType.AD_PLAY, this);
        player.addListener(EventType.META, this);
        player.addListener(EventType.PLAYLIST_COMPLETE, this);
        player.addListener(EventType.BEFORE_PLAY, this);
        player.addListener(EventType.BEFORE_COMPLETE, this);
        player.addListener(EventType.AD_SCHEDULE, this);
    }

    private void updateOutput(String output) {
        mOutput.setText(output);
    }

    /**
     * Regular playback events below here
     */

    @Override
    public void onAudioTracks(AudioTracksEvent audioTracksEvent) {
        Log.d(TAG, "onAudioTracks");
        updateOutput("onAudioTracks(List<AudioTrack>)");
    }

    @Override
    public void onBeforeComplete(BeforeCompleteEvent beforeCompleteEvent) {
        Log.d(TAG, "onBeforeComplete");
        updateOutput("onBeforeComplete()");
    }

    @Override
    public void onBeforePlay(BeforePlayEvent beforePlayEvent
    ) {
        Log.d(TAG, "onBeforePlay");
        updateOutput("onBeforePlay()");
    }

    @Override
    public void onBuffer(BufferEvent bufferEvent) {
        Log.d(TAG, "onBuffer");
        updateOutput("onBuffer()");
    }

    @Override
    public void onCaptionsList(CaptionsListEvent captionsListEvent) {
        Log.d(TAG, "onCaptionsList");
        updateOutput("onCaptionsList(List<Caption>)");
    }

    @Override
    public void onComplete(CompleteEvent completeEvent) {
        Log.d(TAG, "onComplete");
        updateOutput("onComplete()");
    }

    @Override
    public void onFullscreen(FullscreenEvent fullscreen) {
        Log.d(TAG, "onFullscreen");
        updateOutput("onFullscreen(" + fullscreen.getFullscreen() + ")");
    }

    @Override
    public void onIdle(IdleEvent idleEvent) {
        Log.d(TAG, "onIdle");
        updateOutput("onIdle()");
    }

    @Override
    public void onMeta(MetaEvent metaEvent) {
        Log.d(TAG, "onMeta");
        updateOutput("onMeta()");
    }

    @Override
    public void onPause(PauseEvent pauseEvent) {
        Log.d(TAG, "onPause");
        updateOutput("onPause()");
    }

    @Override
    public void onPlay(PlayEvent playEvent) {
        Log.d(TAG, "onPlay");
        updateOutput("onPlay()");
    }

    @Override
    public void onPlaylistComplete(PlaylistCompleteEvent playlistCompleteEvent) {
        Log.d(TAG, "onPlaylistComplete");
        updateOutput("onPlaylistComplete()");
    }

    @Override
    public void onPlaylistItem(PlaylistItemEvent playlistItemEvent) {
        Log.d(TAG, "onPlaylistItem");
        updateOutput("onPlaylistItem()");
    }

    @Override
    public void onPlaylist(PlaylistEvent playlistEvent) {
        Log.d(TAG, "onPlaylist");
        updateOutput("onPlaylist()");
    }


    @Override
    public void onSeek(SeekEvent seekEvent) {
        Log.d(TAG, "onSeek");
        updateOutput("onSeek(" + seekEvent.getPosition() + ", " + seekEvent.getOffset() + ")");
    }

    @Override
    public void onSetupError(SetupErrorEvent setupErrorEvent) {
        Log.d("onSetupError: ", setupErrorEvent.getMessage());
        updateOutput("onSetupError(\"" + setupErrorEvent.getMessage() + "\")");
    }

    @Override
    public void onTime(TimeEvent timeEvent) {
        Log.d(TAG, timeEvent.getDuration() + "  ***  ");
    }

    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        updateOutput("onAdError(\"" + "\", \"" + adErrorEvent.getMessage() + "\")");
        Log.d(TAG, "onAdError tag : " + adErrorEvent.getTag());
        Log.d(TAG, "onAdError msg : " + adErrorEvent.getMessage());
    }

    @Override
    public void onError(ErrorEvent errorEvent) {
        Log.d(TAG, "onError : " + errorEvent);
        Log.d(TAG, "onError : " + errorEvent.getMessage());
        updateOutput("onError(\"" + errorEvent.getMessage() + "\")");
    }

    @Override
    public void onLevelsChanged(LevelsChangedEvent levelsChangedEvent) {
        Log.d(TAG, "onLevelsChanged");
        updateOutput("onLevelsChange(" + levelsChangedEvent.getCurrentQualityIndex() + ")");
    }

    @Override
    public void onLevels(LevelsEvent levelsEvent) {
        Log.d(TAG, "onLevels");
        updateOutput("onLevels(List<QualityLevel>)");
    }

    @Override
    public void onAudioTrackChanged(AudioTrackChangedEvent audioTrackChangedEvent) {
        Log.d(TAG, "onAudioTrackChanged");
        updateOutput("onAudioTrackChanged(" + audioTrackChangedEvent.getCurrentTrack() + ")");
    }

    @Override
    public void onCaptionsChanged(CaptionsChangedEvent list) {
        Log.d(TAG, "onCaptionsChanged");
        updateOutput("onCaptionsChanged(" + list.getCurrentTrack() + ", List<Caption>)");
    }

    @Override
    public void onAdClick(AdClickEvent adClickEvent) {
        Log.d(TAG, "onAdClick");
        updateOutput("onAdClick(\"" + adClickEvent.getTag() + "\")");
    }

    @Override
    public void onAdComplete(AdCompleteEvent adCompleteEvent) {
        Log.d(TAG, "onAdComplete");
        updateOutput("onAdComplete(\"" + adCompleteEvent.getTag() + "\")");
    }

    @Override
    public void onAdSkipped(AdSkippedEvent adSkippedEvent) {
        Log.d(TAG, "onAdSkipped");
        updateOutput("onAdSkipped(\"" + adSkippedEvent.getTag() + "\")");
    }

    @Override
    public void onAdImpression(AdImpressionEvent adImpressionEvent) {
        Log.d(TAG, "onAdImpression");
        updateOutput("onAdImpression(\"" + adImpressionEvent.getTag() + "\", \"" + adImpressionEvent.getCreativeType() + "\", \"" + adImpressionEvent.getAdPosition().name() + "\")");

    }

    @Override
    public void onAdTime(AdTimeEvent adTimeEvent) {
        Log.d(TAG, "onAdTime");
        // Do nothing - this fires several times per second
    }

    @Override
    public void onAdPause(AdPauseEvent adPauseEvent) {
        Log.d(TAG, "onAdPause");
        updateOutput("onAdPause(\"" + adPauseEvent.getTag() + "\", \"" + adPauseEvent.getOldState() + "\")");
    }

    @Override
    public void onAdPlay(AdPlayEvent adPlayEvent) {
        Log.d(TAG, "onAdPlay");
        updateOutput("onAdPlay(\"" + adPlayEvent.getTag() + "\", \"" + adPlayEvent.getOldState() + "\")");
    }

    public void onSeeked(SeekedEvent seekedEvent) {
        Log.d(TAG, "onSeeked");
        updateOutput("onSeeked(\"" + "seeked" + "\")");
    }

    @Override
    public void onControls(ControlsEvent controlsEvent) {
        Log.d(TAG, "onControls");
        updateOutput("onControls(\"" + controlsEvent.getControls() + "\")");
    }

    @Override
    public void onDisplayClick(DisplayClickEvent displayClickEvent) {
        Log.d(TAG, "onDisplayClick");
        updateOutput("onDisplayClick()");
    }

    @Override
    public void onVisualQuality(VisualQualityEvent visualQuality) {
        Log.d(TAG, "onVisualQuality");
        updateOutput("onVisualQuality(\"" + "\")");
    }

    @Override
    public void onMute(MuteEvent muteEvent) {
        Log.d(TAG, "onMute");
        updateOutput("onMute()");

    }

    @Override
    public void onFirstFrame(FirstFrameEvent firstFrameEvent) {
        Log.d(TAG, "firstFrameEvent");
        updateOutput("onFirstFrame()");
    }


    @Override
    public void onAdSchedule(AdScheduleEvent adScheduleEvent) {
        Log.d(TAG, "onAdSchedule");
        updateOutput("onAdSchedule()");
    }
}
