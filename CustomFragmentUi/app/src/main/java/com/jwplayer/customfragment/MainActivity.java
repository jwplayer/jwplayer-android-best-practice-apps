package com.jwplayer.customfragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;

import com.jwplayer.customfragment.callbacks.CallbackFragment;
import com.jwplayer.customfragment.player.CustomPlayerFragment;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.configuration.UiConfig;
import com.jwplayer.pub.api.configuration.ads.VmapAdvertisingConfig;
import com.jwplayer.pub.api.license.LicenseUtil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentManager;


public class MainActivity extends AppCompatActivity {

    private static final String PLAYER_FRAGMENT_TAG = "PlayerFragment";
    private static final String CALLBACK_FRAGMENT_TAG = "CallbackFragment";
    private ConstraintLayout mRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(isLandscape()? R.layout.activity_main_land : R.layout.activity_main);
        mRoot = findViewById(R.id.root);

        // TODO: Add your license key
        new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

        CallbackFragment callbackFragment = new CallbackFragment();
        CustomPlayerFragment customPlayerFragment = new CustomPlayerFragment(getConfig());
        customPlayerFragment.setPlayerInitListener(callbackFragment);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
          .setReorderingAllowed(true)
          .add(R.id.player_container, customPlayerFragment, PLAYER_FRAGMENT_TAG)
          .add(R.id.callback_container, callbackFragment, CALLBACK_FRAGMENT_TAG)
          .commit();

        fm.executePendingTransactions();

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLayout(newConfig);
    }

    private void updateLayout(Configuration newConfig){
        // Handle orientation changes and change the app layout
        boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this,
                            isLandscape ? R.layout.activity_main_land : R.layout.activity_main);
        constraintSet.applyTo(mRoot);
    }

    private boolean isLandscape(){
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Exit fullscreen when the user pressed the Back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            CustomPlayerFragment customPlayerFragment = getPlayerFragment();
            if (customPlayerFragment.isResumed() && customPlayerFragment.getFullscreen()) {
                customPlayerFragment.setFullscreen(false, true);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private CustomPlayerFragment getPlayerFragment() {
        return (CustomPlayerFragment) getSupportFragmentManager().findFragmentByTag(PLAYER_FRAGMENT_TAG);
    }

    private PlayerConfig getConfig(){
        return new PlayerConfig.Builder()
                .playlistUrl(
                        "https://cdn.jwplayer.com/v2/media/1sc0kL2N?format=json")
                .advertisingConfig(
                        new VmapAdvertisingConfig.Builder()
                                .tag("https://s3.amazonaws.com/george.success.jwplayer.com/demos/vmap_midroll_preroll.xml")
                                .build())
                .uiConfig(new UiConfig.Builder()
                                  .hideAllControls()
                                  .build())
                .build();
    }
}
