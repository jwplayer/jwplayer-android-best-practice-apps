package com.jwplayer.demo.customfragmentui;

import android.widget.TextView;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.events.AdBreakEndEvent;
import com.jwplayer.pub.api.events.AdBreakStartEvent;
import com.jwplayer.pub.api.events.CompleteEvent;
import com.jwplayer.pub.api.events.ErrorEvent;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FirstFrameEvent;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.PauseEvent;
import com.jwplayer.pub.api.events.PlayEvent;
import com.jwplayer.pub.api.events.ReadyEvent;
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;

/**
 * Appends a line to a TextView for a handful of key player events.
 * See {@link EventType} for the full list of events you can listen to.
 */
class EventLog implements VideoPlayerEvents.OnReadyListener,
        VideoPlayerEvents.OnFirstFrameListener,
        VideoPlayerEvents.OnPlayListener,
        VideoPlayerEvents.OnPauseListener,
        VideoPlayerEvents.OnCompleteListener,
        VideoPlayerEvents.OnErrorListener,
        VideoPlayerEvents.OnFullscreenListener,
        AdvertisingEvents.OnAdBreakStartListener,
        AdvertisingEvents.OnAdBreakEndListener {

    private final TextView mOutput;

    EventLog(TextView output) {
        mOutput = output;
    }

    void attach(JWPlayer player) {
        player.addListeners(this,
                EventType.READY,
                EventType.FIRST_FRAME,
                EventType.PLAY,
                EventType.PAUSE,
                EventType.COMPLETE,
                EventType.ERROR,
                EventType.FULLSCREEN,
                EventType.AD_BREAK_START,
                EventType.AD_BREAK_END);
    }

    private void log(String line) {
        mOutput.append(line + "\n");
    }

    @Override
    public void onReady(ReadyEvent event) {
        log("ready (setup time " + event.getSetupTime() + " ms)");
    }

    @Override
    public void onFirstFrame(FirstFrameEvent event) {
        log("firstFrame (" + event.getLoadTime() + " ms)");
    }

    @Override
    public void onPlay(PlayEvent event) {
        log("play");
    }

    @Override
    public void onPause(PauseEvent event) {
        log("pause");
    }

    @Override
    public void onComplete(CompleteEvent event) {
        log("complete");
    }

    @Override
    public void onError(ErrorEvent event) {
        log("error " + event.getErrorCode() + ": " + event.getMessage());
    }

    @Override
    public void onFullscreen(FullscreenEvent event) {
        log("fullscreen " + event.getFullscreen());
    }

    @Override
    public void onAdBreakStart(AdBreakStartEvent event) {
        log("adBreakStart");
    }

    @Override
    public void onAdBreakEnd(AdBreakEndEvent event) {
        log("adBreakEnd");
    }
}
