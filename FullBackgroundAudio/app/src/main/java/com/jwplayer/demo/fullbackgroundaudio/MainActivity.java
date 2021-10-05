/*
 * Author: Efrain Gonzalez
 * c-egonzalez@jwplayer.com
 */

package com.jwplayer.demo.fullbackgroundaudio;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.view.JWPlayerView;

/**
 * A simple activity that holds a JWPlayer view. This activity connects to the background
 * service, binds to it to get the player instance and then it starts the service so it can keep
 * running independently from the activity's life cycle.
 */
public class MainActivity extends AppCompatActivity {

    private RelativeLayout mContainer;
    private JWPlayerView mPlayerView;
    private BackgroundAudioService.ServiceBinder mService;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = (BackgroundAudioService.ServiceBinder) service;
            mService.createPlayer(MainActivity.this);
            mPlayerView = mService.getPlayerView();
            mContainer.addView(mPlayerView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            Intent startIntent = new Intent(MainActivity.this, BackgroundAudioService.class);
            startIntent.putExtra(BackgroundAudioService.ACTION, BackgroundAudioService.ACTION_START);
            startForegroundService(startIntent);
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainer = findViewById(R.id.container);

        LicenseUtil.setLicenseKey(this, YOUR_LICENSE_KEY);

        bindService(new Intent(this, BackgroundAudioService.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if(mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
