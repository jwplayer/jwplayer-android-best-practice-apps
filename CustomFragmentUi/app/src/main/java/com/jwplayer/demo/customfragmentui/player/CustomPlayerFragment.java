package com.jwplayer.demo.customfragmentui.player;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.jwplayer.demo.customfragmentui.KeepScreenOnHandler;
import com.jwplayer.demo.customfragmentui.R;
import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.PlayerState;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.configuration.UiConfig;
import com.jwplayer.pub.api.configuration.ads.VmapAdvertisingConfig;
import com.jwplayer.pub.view.JWPlayerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A Fragment that owns a {@link JWPlayerView} and replaces the default controls with
 * {@link CustomPlayerView}.
 *
 * <ul>
 *     <li>The player is created with {@code getPlayer(getViewLifecycleOwner())}, so it follows the
 *     fragment's <em>view</em> lifecycle and is released when the view is destroyed.</li>
 *     <li>The default controls are hidden with {@link UiConfig.Builder#hideAllControls()} and the
 *     custom UI is added on top of the player.</li>
 *     <li>The fragment keeps a no-argument constructor so the FragmentManager can recreate it.</li>
 * </ul>
 *
 * If the host Activity implements {@link JWPlayer.PlayerInitializationListener} it is told about
 * the player once it exists.
 */
public class CustomPlayerFragment extends Fragment {

    private JWPlayer mJWPlayer;
    private CustomPlayerViewModel mCustomPlayerViewModel;

    public CustomPlayerFragment() {
        super(R.layout.fragment_player);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        JWPlayerView playerView = view.findViewById(R.id.fragment_player);
        mJWPlayer = playerView.getPlayer(getViewLifecycleOwner());

        if (requireActivity() instanceof JWPlayer.PlayerInitializationListener) {
            ((JWPlayer.PlayerInitializationListener) requireActivity()).onPlayerInitialized(mJWPlayer);
        }

        // Keep the screen on while content or an ad is playing.
        new KeepScreenOnHandler(mJWPlayer, requireActivity().getWindow());
        mJWPlayer.setup(buildConfig());

        // Add the custom UI on top of the player.
        mCustomPlayerViewModel = new CustomPlayerViewModel(mJWPlayer);
        CustomPlayerView customPlayerView = new CustomPlayerView(requireContext());
        customPlayerView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        playerView.addView(customPlayerView);
        customPlayerView.bind(mCustomPlayerViewModel, getViewLifecycleOwner());
    }

    private static PlayerConfig buildConfig() {
        return new PlayerConfig.Builder()
                .playlistUrl("https://cdn.jwplayer.com/v2/playlists/9L8aDjaU")
                .advertisingConfig(new VmapAdvertisingConfig.Builder()
                        // Google IMA sample VMAP tag: pre-, mid- and post-roll.
                        .tag("https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpremidpost&ciu_szs=300x250&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&cmsid=496&vid=short_onecue&correlator=")
                        .build())
                // Hide the built-in controls; CustomPlayerView replaces them.
                .uiConfig(new UiConfig.Builder()
                        .hideAllControls()
                        .build())
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        // The player pauses ads when the app goes to the background; resume the ad on return so
        // the custom UI (which has no ad "play" button) isn't left stuck.
        if (mCustomPlayerViewModel != null && mJWPlayer != null
                && mCustomPlayerViewModel.isAdPlaying() && mJWPlayer.getState() == PlayerState.PAUSED) {
            mCustomPlayerViewModel.togglePlay();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mJWPlayer = null;
        mCustomPlayerViewModel = null;
    }

    public boolean getFullscreen() {
        return mJWPlayer != null && mJWPlayer.getFullscreen();
    }

    public void setFullscreen(boolean fullscreen, boolean allowRotation) {
        if (mJWPlayer != null) {
            mJWPlayer.setFullscreen(fullscreen, allowRotation);
        }
    }
}
