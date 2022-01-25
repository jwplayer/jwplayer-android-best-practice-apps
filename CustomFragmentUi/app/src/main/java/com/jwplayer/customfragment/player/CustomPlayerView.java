package com.jwplayer.customfragment.player;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.jwplayer.customfragment.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;

public class CustomPlayerView extends ConstraintLayout {

    private ImageView playToggle;
    private ImageView fullscreenToggle;
    private SeekBar contentSeekBar;
    private ProgressBar adProgressBar;
    private Button skipAd;
    private Button learnMore;

    public CustomPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public CustomPlayerView(@NonNull Context context,
                            @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomPlayerView(@NonNull Context context,
                            @Nullable AttributeSet attrs,
                            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomPlayerView(@NonNull Context context,
                            @Nullable AttributeSet attrs,
                            int defStyleAttr,
                            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.view_custom_player_ui, this);
        playToggle = findViewById(R.id.play_pause_toggle);
        fullscreenToggle = findViewById(R.id.fullscreen_toggle);
        contentSeekBar = findViewById(R.id.seekbar);
        adProgressBar = findViewById(R.id.ad_progress);
        skipAd = findViewById(R.id.skip_button);
        learnMore = findViewById(R.id.learn_more);
    }

    public void bind(CustomPlayerViewModel viewModel, LifecycleOwner lifecycleOwner) {

        viewModel.getAdProgressPercentage().observe(lifecycleOwner, progress -> {
            adProgressBar.setProgress(progress);
        });
        viewModel.getIsAdProgressVisible().observe(lifecycleOwner, isVisible -> {
            adProgressBar.setVisibility(isVisible ? VISIBLE : GONE);
        });
        viewModel.getSkipOffsetCountdown().observe(lifecycleOwner, message -> {
            skipAd.setText(message);
        });
        viewModel.getIsSkipButtonEnabled().observe(lifecycleOwner, isEnabled -> {
            skipAd.setEnabled(isEnabled);
        });
        viewModel.getIsSkipButtonVisible().observe(lifecycleOwner, isVisible -> {
            skipAd.setVisibility(isVisible ? VISIBLE : GONE);
        });
        skipAd.setOnClickListener(v -> {
            viewModel.skipAd();
        });
        viewModel.getIsLearnMoreVisible().observe(lifecycleOwner, isVisible -> {
            learnMore.setVisibility(isVisible ? VISIBLE : GONE);
        });
        learnMore.setOnClickListener(v -> {
            viewModel.openAdClickthrough();
        });

        viewModel.getContentProgressPercentage().observe(lifecycleOwner, progress -> {
            contentSeekBar.setProgress(progress);
        });
        viewModel.getIsSeekbarVisible().observe(lifecycleOwner, isVisible -> {
            contentSeekBar.setVisibility(isVisible ? VISIBLE : GONE);
        });
        contentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    viewModel.seek(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        viewModel.getIsPlayToggleVisible().observe(lifecycleOwner, isVisible -> {
            playToggle.setVisibility(isVisible ? VISIBLE : GONE);
        });
        viewModel.getIsPlayIcon().observe(lifecycleOwner, isPlay -> {
            playToggle.setImageDrawable(isPlay ? AppCompatResources
                    .getDrawable(getContext(), R.drawable.ic_jw_play)
                                                : AppCompatResources
                    .getDrawable(getContext(), R.drawable.ic_jw_pause));
        });
        playToggle.setOnClickListener(v -> {
            viewModel.togglePlay();
        });

        viewModel.getIsFullscreen().observe(lifecycleOwner, isFullscreen -> {
            fullscreenToggle.setImageDrawable(isFullscreen ? AppCompatResources
                    .getDrawable(getContext(), R.drawable.ic_jw_fullscreen_not)
                                                      : AppCompatResources
                    .getDrawable(getContext(), R.drawable.ic_jw_fullscreen));
        });
        fullscreenToggle.setOnClickListener(v -> {
            viewModel.toggleFullscreen();
        });
    }

}
