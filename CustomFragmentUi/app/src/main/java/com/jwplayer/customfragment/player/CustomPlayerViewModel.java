package com.jwplayer.customfragment.player;

import android.util.Log;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.PlayerState;
import com.jwplayer.pub.api.events.AdBreakEndEvent;
import com.jwplayer.pub.api.events.AdBreakStartEvent;
import com.jwplayer.pub.api.events.AdMetaEvent;
import com.jwplayer.pub.api.events.AdTimeEvent;
import com.jwplayer.pub.api.events.CompleteEvent;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FirstFrameEvent;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.PauseEvent;
import com.jwplayer.pub.api.events.PlayEvent;
import com.jwplayer.pub.api.events.ReadyEvent;
import com.jwplayer.pub.api.events.TimeEvent;
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * This is only an example of how you could handle player events to drive UI state and behavior
 */
public class CustomPlayerViewModel implements VideoPlayerEvents.OnFirstFrameListener,
                                              VideoPlayerEvents.OnPlayListener,
                                              VideoPlayerEvents.OnPauseListener,
                                              VideoPlayerEvents.OnCompleteListener,
                                              VideoPlayerEvents.OnTimeListener,
                                              AdvertisingEvents.OnAdTimeListener,
                                              AdvertisingEvents.OnAdMetaListener,
                                              AdvertisingEvents.OnAdBreakStartListener,
                                              AdvertisingEvents.OnAdBreakEndListener,
                                              VideoPlayerEvents.OnFullscreenListener,
                                              VideoPlayerEvents.OnReadyListener {

    private static final int NO_VALUE_POSITION = -1;
    private static final String NO_VALUE_STRING = "";
    private final JWPlayer player;

    // ADS
    private boolean isAdPlaying;
    private MutableLiveData<Integer> adProgressPercentage = new MutableLiveData<>(NO_VALUE_POSITION);
    private MutableLiveData<Boolean> isAdProgressVisible = new MutableLiveData<>(false);
    private int currentSkipOffset = NO_VALUE_POSITION;
    private MutableLiveData<String> skipOffsetCountdown = new MutableLiveData<>(NO_VALUE_STRING);
    private MutableLiveData<Boolean> isSkipButtonVisible = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isSkipButtonEnabled = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isLearnMoreVisible = new MutableLiveData<>(false);
    private String clickthroughURL = NO_VALUE_STRING;

    // CONTENT
    private MutableLiveData<Boolean> isPlayToggleVisible = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isPlayIcon = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isSeekbarVisible = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isFullscreen = new MutableLiveData<>(false);
    private MutableLiveData<Integer> contentProgressPercentage = new MutableLiveData<>(
            NO_VALUE_POSITION);

    public CustomPlayerViewModel(JWPlayer player) {
        this.player = player;
        player.addListener(EventType.FIRST_FRAME, this);
        player.addListener(EventType.PLAY, this);
        player.addListener(EventType.PAUSE, this);
        player.addListener(EventType.TIME, this);
        player.addListener(EventType.COMPLETE, this);
        player.addListener(EventType.FULLSCREEN, this);
        player.addListener(EventType.AD_TIME, this);
        player.addListener(EventType.AD_META, this);
        player.addListener(EventType.AD_BREAK_START, this);
        player.addListener(EventType.AD_BREAK_END, this);
        player.addListener(EventType.READY, this);
    }

    @Override
    public void onFirstFrame(FirstFrameEvent firstFrameEvent) {
        Log.d("VIEWMODEL", "FirstFrame fired");
    }

    @Override
    public void onPlay(PlayEvent playEvent) {
        resetAdState();
        updateAdsUi();
        updateContentUi();
    }

    @Override
    public void onPause(PauseEvent pauseEvent) {
        updateContentUi();
    }

    @Override
    public void onTime(TimeEvent timeEvent) {
        handleTimeUpdate(timeEvent.getPosition(),
                         timeEvent.getDuration(),
                         contentProgressPercentage
        );
    }

    @Override
    public void onAdMeta(AdMetaEvent adMetaEvent) {
        currentSkipOffset = adMetaEvent.getSkipOffset();
        clickthroughURL = adMetaEvent.getClickThroughUrl();
    }

    @Override
    public void onAdTime(AdTimeEvent adTimeEvent) {
        handleTimeUpdate(adTimeEvent.getPosition(),
                         adTimeEvent.getDuration(),
                         adProgressPercentage
        );
        handleSkipOffsetUpdates((int) adTimeEvent.getPosition());
    }

    private void handleSkipOffsetUpdates(int position) {
        if (currentSkipOffset == NO_VALUE_POSITION) {
            isSkipButtonVisible.setValue(false);
            isSkipButtonEnabled.setValue(false);
            return;
        }
        int skipCounter = currentSkipOffset - position;
        if (skipCounter <= 0) {
            // you are allowed to skip
            isSkipButtonVisible.setValue(true);
            isSkipButtonEnabled.setValue(true);
            skipOffsetCountdown.setValue("Skip ad");
        } else {
            // skip is available, but still counting down
            isSkipButtonVisible.setValue(true);
            isSkipButtonEnabled.setValue(false);
            skipOffsetCountdown.setValue("Skip ad in " + skipCounter);
        }
    }

    /**
     * This assumes VOD content only. Does not account for Live and DVR scenarios
     * */
    private void handleTimeUpdate(double position,
                                  double duration,
                                  MutableLiveData<Integer> percentageLD) {
        int currentPercentage = calculateProgressPercentage(position, duration);
        int lastPercentage = percentageLD.getValue();
        if (currentPercentage != lastPercentage) {
            percentageLD.setValue(currentPercentage);
        }
    }

    private int calculateProgressPercentage(double position, double duration) {
        return (int) (position * 100 / duration);
    }

    @Override
    public void onAdBreakEnd(AdBreakEndEvent adBreakEndEvent) {
        resetAdState();
        updateAdsUi();
        updateContentUi();
    }

    @Override
    public void onAdBreakStart(AdBreakStartEvent adBreakStartEvent) {
        isAdPlaying = true;
        updateContentUi();
        updateAdsUi();
    }

    @Override
    public void onReady(ReadyEvent readyEvent) {
        resetAdState();
        updateAdsUi();
        updateContentUi();
    }

    @Override
    public void onFullscreen(FullscreenEvent fullscreenEvent) {
        isFullscreen.setValue(fullscreenEvent.getFullscreen());
    }

    private void resetAdState() {
        isAdPlaying = false;
        clickthroughURL = NO_VALUE_STRING;
        currentSkipOffset = NO_VALUE_POSITION;
        adProgressPercentage.setValue(NO_VALUE_POSITION);
        skipOffsetCountdown.setValue(NO_VALUE_STRING);
        isSkipButtonVisible.setValue(false);
        isAdProgressVisible.setValue(false);
        isLearnMoreVisible.setValue(false);
    }

    private void updateContentUi() {
        isPlayToggleVisible.setValue(shouldPlayToggleBeVisible());
        isPlayIcon.setValue(shouldPlayIconBeVisible());
        isSeekbarVisible.setValue(shouldSeekbarBeVisible());
    }

    private void updateAdsUi() {
        isAdProgressVisible.setValue(shouldAdProgressBeVisible());
        isSkipButtonVisible.setValue(shouldSkipButtonBeVisible());
        isLearnMoreVisible.setValue(shouldLearnMoreBeVisible());
    }

    private boolean shouldPlayToggleBeVisible() {
        return player.getState() != PlayerState.ERROR &&
                player.getState() != PlayerState.BUFFERING &&
                !isAdPlaying;
    }

    private boolean shouldPlayIconBeVisible() {
        return player.getState() != PlayerState.PLAYING &&
                player.getState() != PlayerState.ERROR &&
                player.getState() != PlayerState.BUFFERING &&
                !isAdPlaying;
    }

    private boolean shouldSeekbarBeVisible() {
        return (player.getState() == PlayerState.PLAYING || player
                .getState() == PlayerState.PAUSED) && !isAdPlaying;
    }

    private boolean shouldAdProgressBeVisible() {
        return isAdPlaying;
    }

    private boolean shouldSkipButtonBeVisible() {
        return isAdPlaying && currentSkipOffset != NO_VALUE_POSITION;
    }

    private boolean shouldLearnMoreBeVisible() {
        return isAdPlaying && clickthroughURL.equals(NO_VALUE_STRING);
    }

    public void togglePlay() {
        if (player.getState() != PlayerState.PLAYING) {
            player.play();
        } else {
            player.pause();
        }
    }

    public void seek(int percentage) {
        int position = (int) (percentage * player.getDuration() / 100);
        player.seek(position);
    }

    public void skipAd() {
        player.skipAd();
    }

    public void toggleFullscreen() {
        player.setFullscreen(!player.getFullscreen(), true);
    }

    public LiveData<Integer> getAdProgressPercentage() {
        return adProgressPercentage;
    }

    public LiveData<String> getSkipOffsetCountdown() {
        return skipOffsetCountdown;
    }

    public LiveData<Boolean> getIsSkipButtonVisible() {
        return isSkipButtonVisible;
    }

    public LiveData<Boolean> getIsSkipButtonEnabled() {
        return isSkipButtonEnabled;
    }

    public void openAdClickthrough() {
        player.openAdClickthrough();
    }

    public LiveData<Boolean> getIsPlayToggleVisible() {
        return isPlayToggleVisible;
    }

    public LiveData<Boolean> getIsPlayIcon() {
        return isPlayIcon;
    }

    public LiveData<Boolean> getIsSeekbarVisible() {
        return isSeekbarVisible;
    }

    public LiveData<Boolean> getIsAdProgressVisible() {
        return isAdProgressVisible;
    }

    public LiveData<Boolean> getIsLearnMoreVisible() {
        return isLearnMoreVisible;
    }

    public LiveData<Boolean> getIsFullscreen() {
        return isFullscreen;
    }

    public LiveData<Integer> getContentProgressPercentage() {
        return contentProgressPercentage;
    }

    public boolean isAdPlaying() {
        return isAdPlaying;
    }

    @Override
    public void onComplete(CompleteEvent completeEvent) {
        updateContentUi();
    }
}
