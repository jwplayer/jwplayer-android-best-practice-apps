package com.jwplayer.demo.cnxadserverbpa;

import android.util.Log;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.events.AdBreakEndEvent;
import com.jwplayer.pub.api.events.AdBreakStartEvent;
import com.jwplayer.pub.api.events.AdCompleteEvent;
import com.jwplayer.pub.api.events.AdErrorEvent;
import com.jwplayer.pub.api.events.AdImpressionEvent;
import com.jwplayer.pub.api.events.AdLoadedEvent;
import com.jwplayer.pub.api.events.AdPauseEvent;
import com.jwplayer.pub.api.events.AdPlayEvent;
import com.jwplayer.pub.api.events.AdRequestEvent;
import com.jwplayer.pub.api.events.AdSkippedEvent;
import com.jwplayer.pub.api.events.AdStartedEvent;
import com.jwplayer.pub.api.events.AdViewableImpressionEvent;
import com.jwplayer.pub.api.events.AdWarningEvent;
import com.jwplayer.pub.api.events.ErrorEvent;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.ReadyEvent;
import com.jwplayer.pub.api.events.SetupErrorEvent;
import com.jwplayer.pub.api.events.WarningEvent;
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;

/**
 * Reports every ad lifecycle event, plus every player and ad error and warning, as one line of
 * text, so a screen can show a live event log.
 *
 * <p>AD_TIME fires many times per second while an ad plays, so it is not logged here. Listen for it
 * only if you need ad progress.
 */
final class AdEventLog implements
        VideoPlayerEvents.OnReadyListener,
        VideoPlayerEvents.OnSetupErrorListener,
        VideoPlayerEvents.OnErrorListener,
        VideoPlayerEvents.OnWarningListener,
        AdvertisingEvents.OnAdRequestListener,
        AdvertisingEvents.OnAdLoadedListener,
        AdvertisingEvents.OnAdBreakStartListener,
        AdvertisingEvents.OnAdBreakEndListener,
        AdvertisingEvents.OnAdStartedListener,
        AdvertisingEvents.OnAdImpressionListener,
        AdvertisingEvents.OnAdViewableImpressionListener,
        AdvertisingEvents.OnAdPlayListener,
        AdvertisingEvents.OnAdPauseListener,
        AdvertisingEvents.OnAdCompleteListener,
        AdvertisingEvents.OnAdSkippedListener,
        AdvertisingEvents.OnAdErrorListener,
        AdvertisingEvents.OnAdWarningListener {

    interface Listener {
        /** One line per event. */
        void onLog(String line);

        /**
         * A player setup finished, successfully or not. Call {@code setup(...)} again only after
         * this, so a new configuration doesn't replace one that is still loading.
         */
        void onSetupFinished();
    }

    private static final String TAG = "CnxAdServerBPA";
    private final Listener listener;

    private AdEventLog(Listener listener) {
        this.listener = listener;
    }

    /** Registers a new log on {@code player}. */
    static void attach(JWPlayer player, Listener listener) {
        AdEventLog log = new AdEventLog(listener);
        player.addListener(EventType.READY, log);
        player.addListener(EventType.SETUP_ERROR, log);
        player.addListener(EventType.ERROR, log);
        player.addListener(EventType.WARNING, log);
        player.addListener(EventType.AD_REQUEST, log);
        player.addListener(EventType.AD_LOADED, log);
        player.addListener(EventType.AD_BREAK_START, log);
        player.addListener(EventType.AD_BREAK_END, log);
        player.addListener(EventType.AD_STARTED, log);
        player.addListener(EventType.AD_IMPRESSION, log);
        player.addListener(EventType.AD_VIEWABLE_IMPRESSION, log);
        player.addListener(EventType.AD_PLAY, log);
        player.addListener(EventType.AD_PAUSE, log);
        player.addListener(EventType.AD_COMPLETE, log);
        player.addListener(EventType.AD_SKIPPED, log);
        player.addListener(EventType.AD_ERROR, log);
        player.addListener(EventType.AD_WARNING, log);
    }

    private void log(String line) {
        Log.i(TAG, line);
        listener.onLog(line);
    }

    // --- Setup ---

    @Override
    public void onReady(ReadyEvent event) {
        log("Player ready");
        listener.onSetupFinished();
    }

    // This configuration failed to set up.
    @Override
    public void onSetupError(SetupErrorEvent event) {
        log("SETUP_ERROR " + event.getCode() + ": " + event.getMessage());
        listener.onSetupFinished();
    }

    // --- Player errors and warnings ---

    @Override
    public void onError(ErrorEvent event) {
        log("ERROR " + event.getErrorCode() + ": " + event.getMessage());
    }

    // Player warnings report problems that did not stop the player.
    @Override
    public void onWarning(WarningEvent event) {
        log("WARNING " + event.getErrorCode() + ": " + event.getMessage());
    }

    // --- Ad lifecycle ---

    @Override
    public void onAdRequest(AdRequestEvent event) {
        log("AD_REQUEST");
    }

    @Override
    public void onAdLoaded(AdLoadedEvent event) {
        log("AD_LOADED");
    }

    @Override
    public void onAdBreakStart(AdBreakStartEvent event) {
        log("AD_BREAK_START");
    }

    @Override
    public void onAdBreakEnd(AdBreakEndEvent event) {
        log("AD_BREAK_END");
    }

    @Override
    public void onAdStarted(AdStartedEvent event) {
        log("AD_STARTED");
    }

    @Override
    public void onAdImpression(AdImpressionEvent event) {
        log("AD_IMPRESSION");
    }

    @Override
    public void onAdViewableImpression(AdViewableImpressionEvent event) {
        log("AD_VIEWABLE_IMPRESSION");
    }

    @Override
    public void onAdPlay(AdPlayEvent event) {
        log("AD_PLAY");
    }

    @Override
    public void onAdPause(AdPauseEvent event) {
        log("AD_PAUSE");
    }

    @Override
    public void onAdComplete(AdCompleteEvent event) {
        log("AD_COMPLETE");
    }

    @Override
    public void onAdSkipped(AdSkippedEvent event) {
        log("AD_SKIPPED");
    }

    // Ad errors end the ad or break, but content playback continues. The code says why: for example
    // 10064 is usually a no-fill, and 40100 means the ad service failed to set up, so no ad breaks play
    // until the player is set up again. The codes are constants in ErrorCodes (e.g. AD_NO_AD_AVAILABLE).
    @Override
    public void onAdError(AdErrorEvent event) {
        log("AD_ERROR " + event.getAdErrorCode() + ": " + event.getMessage());
    }

    // Ad warnings report something that did not stop playback.
    @Override
    public void onAdWarning(AdWarningEvent event) {
        log("AD_WARNING " + event.getAdErrorCode() + ": " + event.getMessage());
    }
}
