package com.jwplayer.demo.advancedplayer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.PlayerState;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.media.adaptive.QualityLevel;

import java.util.List;
import java.util.Locale;

/**
 * A custom control bar built from plain Android Views. It only talks to the public
 * {@link JWPlayer} API: it calls methods to control playback and listens to player events
 * to keep its own state in sync.
 */
public class PlayerControlsView extends LinearLayout {

    private static final double[] PLAYBACK_RATES = {0.5, 1.0, 1.25, 1.5, 2.0};

    private final Button playPauseButton;
    private final SeekBar seekBar;
    private final TextView positionText;
    private final TextView durationText;
    private final Button rateButton;
    private final Button qualityButton;
    private final TextView adLabel;
    private final Button skipButton;

    // Player events are delivered on the main thread, but posting UI work through a
    // main-thread Handler keeps this view safe if it is ever driven from elsewhere.
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private JWPlayer player;
    /** True while the user drags the seek bar, so TIME events don't fight the finger. */
    private boolean isScrubbing;
    /** Skip offset of the current ad in seconds, or null when the ad cannot be skipped. */
    @Nullable private Integer adSkipOffset;

    public PlayerControlsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        inflate(context, R.layout.player_controls, this);

        playPauseButton = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seek_bar);
        positionText = findViewById(R.id.position);
        durationText = findViewById(R.id.duration);
        rateButton = findViewById(R.id.rate);
        qualityButton = findViewById(R.id.quality);
        adLabel = findViewById(R.id.ad_label);
        skipButton = findViewById(R.id.skip);
    }

    /** True between AD_BREAK_START and AD_BREAK_END. */
    private boolean inAdBreak;
    /** True while an ad is playing (between AD_PLAY and AD_PAUSE). */
    private boolean adPlaying;

    /** Connects the controls to a player. Call once, before {@link JWPlayer#setup}. */
    public void bind(JWPlayer player) {
        this.player = player;
        wireUserInput();
        listenToPlayer();
    }

    // ---- User input -> player API ----

    private void wireUserInput() {
        playPauseButton.setOnClickListener(v -> {
            // During an ad break the ad has its own pause API; play()/pause() act on content.
            if (inAdBreak) {
                player.pauseAd(adPlaying);
                return;
            }
            PlayerState state = player.getState();
            if (state == PlayerState.PLAYING || state == PlayerState.BUFFERING) {
                player.pause();
            } else {
                player.play();
            }
        });

        // The seek bar works in whole seconds.
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (fromUser) positionText.setText(formatTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
                isScrubbing = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                isScrubbing = false;
                player.seek(bar.getProgress());
            }
        });

        rateButton.setOnClickListener(this::showRateMenu);
        qualityButton.setOnClickListener(this::showQualityMenu);
        skipButton.setOnClickListener(v -> player.skipAd());
    }

    private void showRateMenu(View anchor) {
        PopupMenu menu = new PopupMenu(getContext(), anchor);
        for (int i = 0; i < PLAYBACK_RATES.length; i++) {
            menu.getMenu().add(Menu.NONE, i, i, formatRate(PLAYBACK_RATES[i]));
        }
        menu.setOnMenuItemClickListener(item -> {
            player.setPlaybackRate(PLAYBACK_RATES[item.getItemId()]);
            return true;
        });
        menu.show();
    }

    private void showQualityMenu(View anchor) {
        // getQualityLevels() is filled in once the stream's renditions are known (LEVELS event).
        List<QualityLevel> levels = player.getQualityLevels();
        if (levels == null || levels.isEmpty()) return;
        PopupMenu menu = new PopupMenu(getContext(), anchor);
        for (int i = 0; i < levels.size(); i++) {
            menu.getMenu().add(Menu.NONE, i, i, levels.get(i).getLabel());
        }
        menu.setOnMenuItemClickListener(item -> {
            player.setCurrentQuality(item.getItemId());
            return true;
        });
        menu.show();
    }

    // ---- Player events -> UI ----

    private void listenToPlayer() {
        player.addListener(EventType.PLAY,
                (VideoPlayerEvents.OnPlayListener) e -> ui(() -> playPauseButton.setText(R.string.pause)));
        player.addListener(EventType.PAUSE,
                (VideoPlayerEvents.OnPauseListener) e -> ui(() -> {
                    // Content also reports a pause when an ad break takes over; the ad events
                    // below own the button while the break runs.
                    if (!inAdBreak) playPauseButton.setText(R.string.play);
                }));
        player.addListener(EventType.COMPLETE,
                (VideoPlayerEvents.OnCompleteListener) e -> ui(() -> playPauseButton.setText(R.string.play)));

        // Content progress. Ads report their own time through AD_TIME (below).
        player.addListener(EventType.TIME,
                (VideoPlayerEvents.OnTimeListener) e -> ui(() -> showProgress(e.getPosition(), e.getDuration())));

        player.addListener(EventType.PLAYBACK_RATE_CHANGED,
                (VideoPlayerEvents.OnPlaybackRateChangedListener) e ->
                        ui(() -> rateButton.setText(formatRate(e.getPlaybackRate()))));

        // LEVELS fires when a new item's renditions are known; LEVELS_CHANGED when one is picked.
        player.addListener(EventType.LEVELS,
                (VideoPlayerEvents.OnLevelsListener) e ->
                        ui(() -> showQuality(e.getLevels(), e.getCurrentQualityIndex())));
        player.addListener(EventType.LEVELS_CHANGED,
                (VideoPlayerEvents.OnLevelsChangedListener) e ->
                        ui(() -> showQuality(player.getQualityLevels(), e.getCurrentQualityIndex())));

        listenToAds();
    }

    private void listenToAds() {
        // An ad break is starting: show the "Ad" badge and stop the user seeking the ad.
        player.addListener(EventType.AD_BREAK_START,
                (AdvertisingEvents.OnAdBreakStartListener) e -> ui(() -> setAdMode(true)));
        player.addListener(EventType.AD_BREAK_END,
                (AdvertisingEvents.OnAdBreakEndListener) e -> ui(() -> setAdMode(false)));

        // The impression tells us whether this particular ad can be skipped.
        player.addListener(EventType.AD_IMPRESSION,
                (AdvertisingEvents.OnAdImpressionListener) e -> ui(() -> {
                    Integer offset = e.getSkipOffset();
                    adSkipOffset = (offset != null && offset >= 0) ? offset : null;
                    // Start the countdown at the ad's beginning, so the button never shows an
                    // enabled "Skip" before the first AD_TIME arrives.
                    updateSkipButton(0);
                    skipButton.setVisibility(adSkipOffset != null ? VISIBLE : GONE);
                }));

        // Ad progress drives the seek bar and the "Skip in N" countdown.
        player.addListener(EventType.AD_TIME,
                (AdvertisingEvents.OnAdTimeListener) e -> ui(() -> {
                    showProgress(e.getPosition(), e.getDuration());
                    updateSkipButton(e.getPosition());
                    // Ad time only advances while the ad plays, so it also keeps the
                    // play/pause label right if AD_PLAY came before the break started.
                    if (!adPlaying) {
                        adPlaying = true;
                        playPauseButton.setText(R.string.pause);
                    }
                }));

        player.addListener(EventType.AD_SKIPPED,
                (AdvertisingEvents.OnAdSkippedListener) e -> ui(() -> skipButton.setVisibility(GONE)));
        player.addListener(EventType.AD_PLAY,
                (AdvertisingEvents.OnAdPlayListener) e -> ui(() -> {
                    adPlaying = true;
                    playPauseButton.setText(R.string.pause);
                }));
        player.addListener(EventType.AD_PAUSE,
                (AdvertisingEvents.OnAdPauseListener) e -> ui(() -> {
                    adPlaying = false;
                    playPauseButton.setText(R.string.play);
                }));
    }

    private void setAdMode(boolean inAd) {
        inAdBreak = inAd;
        if (!inAd) adPlaying = false;
        adLabel.setVisibility(inAd ? VISIBLE : GONE);
        seekBar.setEnabled(!inAd);
        // Rate and quality apply to content, so disable them while an ad plays.
        rateButton.setEnabled(!inAd);
        qualityButton.setEnabled(!inAd);
        if (!inAd) {
            adSkipOffset = null;
            skipButton.setVisibility(GONE);
        }
    }

    private void updateSkipButton(double adPosition) {
        if (adSkipOffset == null) return;
        int remaining = (int) Math.ceil(adSkipOffset - adPosition);
        boolean canSkip = remaining <= 0;
        skipButton.setEnabled(canSkip);
        skipButton.setText(canSkip
                ? getContext().getString(R.string.skip)
                : getContext().getString(R.string.skip_in, remaining));
    }

    private void showProgress(double position, double duration) {
        if (isScrubbing) return;
        // Live streams report a duration <= 0; there is nothing to seek in that case.
        boolean seekable = duration > 0;
        seekBar.setVisibility(seekable ? VISIBLE : INVISIBLE);
        seekBar.setMax(seekable ? (int) duration : 0);
        seekBar.setProgress((int) position);
        positionText.setText(seekable ? formatTime(position) : getContext().getString(R.string.live));
        durationText.setText(seekable ? formatTime(duration) : "");
    }

    private void showQuality(@Nullable List<QualityLevel> levels, int currentIndex) {
        boolean hasChoice = levels != null && levels.size() > 1;
        qualityButton.setVisibility(hasChoice ? VISIBLE : GONE);
        if (hasChoice && currentIndex >= 0 && currentIndex < levels.size()) {
            qualityButton.setText(levels.get(currentIndex).getLabel());
        }
    }

    private void ui(Runnable action) {
        mainHandler.post(action);
    }

    private static String formatTime(double seconds) {
        int total = (int) seconds;
        return String.format(Locale.US, "%d:%02d", total / 60, total % 60);
    }

    private static String formatRate(double rate) {
        return String.format(Locale.US, "%sx", rate == Math.floor(rate)
                ? String.valueOf((int) rate) : String.valueOf(rate));
    }
}
