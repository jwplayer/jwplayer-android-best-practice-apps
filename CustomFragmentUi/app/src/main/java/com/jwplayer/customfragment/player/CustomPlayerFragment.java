package com.jwplayer.customfragment.player;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.jwplayer.customfragment.R;
import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.PlayerState;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.configuration.UiConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.view.JWPlayerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class CustomPlayerFragment extends Fragment implements VideoPlayerEvents.OnFullscreenListener {

    private final PlayerConfig mConfig;

    private CustomPlayerViewModel mCustomPlayerViewModel;
    private JWPlayerView mJWPlayerView;
    private JWPlayer mJWPlayer;
    private JWPlayer.PlayerInitializationListener mPlayerInitListener;

    public CustomPlayerFragment(PlayerConfig config) {
        super(R.layout.fragment_player);
        mConfig = config;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        mJWPlayerView = view.findViewById(R.id.fragment_player);
        mJWPlayer = mJWPlayerView.getPlayer();

        AppCompatActivity appCompatActivity = (AppCompatActivity) this.getActivity();

        mPlayerInitListener.onPlayerInitialized(mJWPlayer);
        mJWPlayer.setup(hideUi(mConfig));
        mJWPlayer.addListener(EventType.FULLSCREEN, this);
        setupCustomUi(appCompatActivity);

        return view;
    }

    private void setupCustomUi(AppCompatActivity activity) {
        mCustomPlayerViewModel = new CustomPlayerViewModel(mJWPlayer);
        CustomPlayerView customPlayerView = new CustomPlayerView(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                                                       FrameLayout.LayoutParams.MATCH_PARENT);
        customPlayerView.setLayoutParams(params);
        mJWPlayerView.addView(customPlayerView);
        customPlayerView.bind(mCustomPlayerViewModel, this);
    }

    private PlayerConfig hideUi(PlayerConfig config) {
        return new PlayerConfig.Builder(config)
                .uiConfig(new UiConfig.Builder()
                                  .hideAllControls()
                                  .build())
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCustomPlayerViewModel == null || mJWPlayer == null) {
            return;
        }
        if (mCustomPlayerViewModel.isAdPlaying() && mJWPlayer.getState() == PlayerState.PAUSED) {
            mCustomPlayerViewModel.togglePlay();
        }
    }

    @Override
    public void onFullscreen(FullscreenEvent fullscreenEvent) {
        Activity activity = getActivity();
        boolean fullscreen = fullscreenEvent.getFullscreen();

        if (activity == null) {
            return;
        }

        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            if (fullscreen) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
        } else if (activity instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
            if (appCompatActivity.getSupportActionBar() != null) {
                if (fullscreen) {
                    appCompatActivity.getSupportActionBar().hide();
                } else {
                    appCompatActivity.getSupportActionBar().show();
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        // TODO: Uncomment to go to fullscreen automatically when rotating the device
//        if (mJWPlayer != null && !mJWPlayer.isInPictureInPictureMode()) {
//            boolean isFullscreen = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
//            mJWPlayer.setFullscreen(isFullscreen, true);
//        }
        super.onConfigurationChanged(newConfig);
    }

    public boolean getFullscreen() {
        if (mJWPlayer == null) {
            return false;
        }
        return mJWPlayer.getFullscreen();
    }

    public void setFullscreen(boolean fullscreen, boolean allowRotation) {
        if (mJWPlayer != null) {
            mJWPlayer.setFullscreen(fullscreen, allowRotation);
        }
    }

    public void setPlayerInitListener(JWPlayer.PlayerInitializationListener initListener) {
        mPlayerInitListener = initListener;
    }
}
