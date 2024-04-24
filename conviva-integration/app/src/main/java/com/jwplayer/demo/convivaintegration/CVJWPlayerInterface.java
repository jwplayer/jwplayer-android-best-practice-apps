package com.jwplayer.demo.convivaintegration;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import androidx.media3.common.Format;
import androidx.media3.common.Metadata;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.DecoderCounters;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.mediacodec.MediaCodecRenderer;
import androidx.media3.exoplayer.source.LoadEventInfo;
import androidx.media3.exoplayer.source.MediaLoadData;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.TrackSelectionArray;

import com.conviva.api.Client.ErrorSeverity;
import com.conviva.api.ConvivaException;
import com.conviva.api.SystemSettings.LogLevel;
import com.conviva.api.player.IClientMeasureInterface;
import com.conviva.api.player.IPlayerInterface;
import com.conviva.api.player.PlayerStateManager;
import com.conviva.api.player.PlayerStateManager.PlayerState;
import com.conviva.api.system.ICancelTimer;
import com.conviva.api.system.ITimerInterface;
import com.conviva.platforms.android.AndroidNetworkUtils;
import com.conviva.platforms.android.AndroidTimerInterface;

import com.jwplayer.pub.api.JWPlayer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class CVJWPlayerInterface implements IClientMeasureInterface, IPlayerInterface, AnalyticsListener {

    private static final String TAG = CVJWPlayerInterface.class.getSimpleName();

    private final PlayerStateManager mStateManager;

    private final JWPlayer mPlayer;

    private final Handler mMainHandler;

    private ICancelTimer mTimer;

    private Method mSendLogMethod;

    private int mDuration = -1;
    private int mVideoBitrate = -1;
    private int mAudioBitrate = -1;
    private int mPht = -1;

    private boolean mIsSendLogMethodAvailable;


    public CVJWPlayerInterface(PlayerStateManager stateManager, JWPlayer player) {

        mMainHandler = createHandler();

        mStateManager = stateManager;
        mPlayer = player;

        mIsSendLogMethodAvailable = checkIfLogMethodExist();

        ITimerInterface iTimerInterface = new AndroidTimerInterface();
        Runnable mPollStreamerTask = CVJWPlayerInterface.this::updateMetrics;

        mTimer = iTimerInterface.createTimer(mPollStreamerTask, 200, "CVJWPlayerInterface");

        if (mStateManager != null) {
            mStateManager.setPlayerVersion("3.5.0");
            mStateManager.setPlayerType("JWPlayer");
            mStateManager.setClientMeasureInterface(this);
            mStateManager.setModuleNameAndVersion(getClass().getSimpleName(), "1.0");
        }

        mPlayer.setAnalyticsListener(this);
    }

    private boolean checkIfLogMethodExist() {

        final Class[] paramTypeArray = new Class[]{String.class, LogLevel.class, IPlayerInterface.class};

        try {
            Method _sendLogMethod = PlayerStateManager.class.getMethod("sendLogMessage", paramTypeArray);
            if (_sendLogMethod != null) {
                mSendLogMethod = _sendLogMethod;
                return true;
            }
        } catch (NoSuchMethodException var3) {
            // DO Nothing            
        }

        return false;
    }

    private void log(String message, LogLevel logLevel) {

        if (mIsSendLogMethodAvailable && mSendLogMethod != null && mStateManager != null) {

            final Object[] paramValueArray = new Object[]{message, logLevel, this};

            try {
                mSendLogMethod.invoke(mStateManager, paramValueArray);
                return;
            } catch (IllegalAccessException var5) {
                // DO Nothing
            } catch (InvocationTargetException var6) {
                // DO Nothing
            }
        }

        switch (logLevel) {
            case DEBUG: {
                Log.d(TAG, message);
                break;
            }
            case INFO: {
                Log.i(TAG, message);
                break;
            }
            case WARNING: {
                Log.w(TAG, message);
                break;
            }
            case ERROR: {
                Log.e(TAG, message);
            }
            case NONE: {
            }
        }

    }

    public void cleanup() {

        log("CVExoPlayerInterface.Cleanup()", LogLevel.DEBUG);

        mTimer.cancel();

        mPlayer.setAnalyticsListener(null);
    }

    public long getPHT() {
        return (long) mPht;
    }

    public int getBufferLength() {
        return 0;
    }

    public double getSignalStrength() {
        return (double) AndroidNetworkUtils.getSignalStrength();
    }

    public void onSeekProcessed(EventTime eventTime) {

        if (mStateManager != null) {

            try {
                mStateManager.setPlayerSeekEnd();
            } catch (ConvivaException var3) {
                log(" Exception occured while processing seekEnd ", LogLevel.DEBUG);
            }
        }
    }

    public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {

        log("onPlayerStateChanged - State : " + playbackState, LogLevel.DEBUG);

        updatePlayerState(playWhenReady, playbackState);
    }

    private void updatePlayerState(boolean playWhenReady, int playbackState) {

        try {
            switch (playbackState) {
                case Player.STATE_BUFFERING: {
                    log("onPlayerStateChanged : STATE_BUFFERING : Conviva  Report BUFFERING ", LogLevel.DEBUG);
                    mStateManager.setPlayerState(PlayerState.BUFFERING);
                    break;
                }
                case Player.STATE_READY: {
                    if (playWhenReady && mPlayer != null) {

                        log("onPlayerStateChanged : STATE_READY : Conviva  Report PLAYING ", LogLevel.DEBUG);

                        mStateManager.setPlayerState(PlayerState.PLAYING);

                        final int duration = (int) mPlayer.getDuration();

                        if (mDuration != duration && duration > 0) {
                            mStateManager.setDuration((int) mPlayer.getDuration());
                            mDuration = duration;
                        }
                    } else {
                        mStateManager.setPlayerState(PlayerState.PAUSED);
                    }
                    break;
                }
                case Player.STATE_ENDED: {
                    mStateManager.setPlayerState(PlayerState.STOPPED);
                    log("onPlayerStateChanged : STATE_ENDED : Conviva  Report STOPPED ", LogLevel.DEBUG);
                }
            }
        } catch (ConvivaException ce) {
            log("Player state exception", LogLevel.DEBUG);
        }

    }

    private void updateMetrics() {

        if (mPlayer != null && mStateManager != null && mMainHandler != null) {

            mMainHandler.post(() -> {

                try {
                    mPht = (int) mPlayer.getPosition();

                } catch (Exception e) {
                    log("Exception occurred " + e.getMessage(), LogLevel.DEBUG);
                }
            });
        }

    }

    public void onPlayerError(EventTime eventTime, ExoPlaybackException error) {

        final String errorMsg;

        if (error.type == 1) {
            Exception cause = error.getRendererException();

            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                errorMsg = "Decoder Initialization Error";
            } else {
                errorMsg = "Render Initialization Error";
            }
        } else {
            errorMsg = "Player Error";
        }

        try {
            if (mStateManager != null) {
                mStateManager.setPlayerState(PlayerState.STOPPED);
                mStateManager.sendError(errorMsg, ErrorSeverity.FATAL);
            }
        } catch (ConvivaException ce) {
            log("Exception occurred while reporting Error", LogLevel.DEBUG);
        }

    }

    public void onVideoSizeChanged(EventTime eventTime, int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        log("video size change. width:" + width + " height:" + height + " unappliedRotationDegrees:" + unappliedRotationDegrees + " pixelWidthHeightRatio:" + pixelWidthHeightRatio, LogLevel.DEBUG);

        try {
            mStateManager.setVideoWidth(width);
            mStateManager.setVideoHeight(height);
        } catch (ConvivaException var7) {
            log("Exception occurred while reporting resolution", LogLevel.DEBUG);
        }
    }

    public int getFrameRate() {
        return -1;
    }

    public void onDownstreamFormatChanged(EventTime eventTime, MediaLoadData mediaLoadData) {

        if (mediaLoadData.trackType == 0) {
            mVideoBitrate = mediaLoadData.trackFormat.bitrate;
            mAudioBitrate = 0;
        } else if (mediaLoadData.trackType == 1) {
            mAudioBitrate = mediaLoadData.trackFormat.bitrate;
        } else if (mediaLoadData.trackType == 2) {
            mVideoBitrate = mediaLoadData.trackFormat.bitrate;
        }

        final int bitrate = mAudioBitrate + mVideoBitrate;

        try {
            mStateManager.setBitrateKbps(bitrate / 1000);

        } catch (ConvivaException ce) {
            ce.printStackTrace();
        }
    }

    public void onTimelineChanged(EventTime eventTime, int reason) {
    }

    public void onPositionDiscontinuity(EventTime eventTime, int reason) {
    }

    public void onSeekStarted(EventTime eventTime) {
    }

    public void onPlaybackParametersChanged(EventTime eventTime, PlaybackParameters playbackParameters) {
    }

    public void onRepeatModeChanged(EventTime eventTime, int repeatMode) {
    }

    public void onShuffleModeChanged(EventTime eventTime, boolean shuffleModeEnabled) {
    }

    public void onLoadingChanged(EventTime eventTime, boolean isLoading) {
    }

    public void onTracksChanged(EventTime eventTime, TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    public void onLoadStarted(EventTime eventTime, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
    }

    public void onLoadCompleted(EventTime eventTime, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
    }

    public void onLoadCanceled(EventTime eventTime, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
    }

    public void onLoadError(EventTime eventTime, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
    }

    public void onUpstreamDiscarded(EventTime eventTime, MediaLoadData mediaLoadData) {
    }

    public void onMediaPeriodCreated(EventTime eventTime) {
    }

    public void onMediaPeriodReleased(EventTime eventTime) {
    }

    public void onReadingStarted(EventTime eventTime) {
    }

    public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {
    }

    public void onMetadata(EventTime eventTime, Metadata metadata) {
    }

    public void onDecoderEnabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {
    }

    public void onDecoderInitialized(EventTime eventTime, int trackType, String decoderName, long initializationDurationMs) {
    }

    public void onDecoderInputFormatChanged(EventTime eventTime, int trackType, Format format) {
    }

    public void onDecoderDisabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {
    }

    public void onAudioSessionId(EventTime eventTime, int audioSessionId) {
    }

    public void onAudioUnderrun(EventTime eventTime, int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
    }

    public void onDroppedVideoFrames(EventTime eventTime, int droppedFrames, long elapsedMs) {
    }

    public void onRenderedFirstFrame(EventTime eventTime, Surface surface) {
    }

    public void onDrmKeysLoaded(EventTime eventTime) {
    }

    public void onDrmSessionManagerError(EventTime eventTime, Exception error) {
    }

    public void onDrmKeysRestored(EventTime eventTime) {
    }

    public void onDrmKeysRemoved(EventTime eventTime) {
    }

    private static Handler createHandler() {

        return (Looper.myLooper() != Looper.getMainLooper())
                ? new Handler(Looper.getMainLooper())
                : new Handler();
    }
}
