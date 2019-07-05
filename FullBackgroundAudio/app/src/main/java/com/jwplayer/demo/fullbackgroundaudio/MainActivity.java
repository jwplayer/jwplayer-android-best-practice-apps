/**
 * Author: Efrain Gonzalez
 * c-egonzalez@jwplayer.com
 */

package com.jwplayer.demo.fullbackgroundaudio;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.longtailvideo.jwplayer.JWPlayerView;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout mContainer;
    private JWPlayerView mPlayer;
    private BackgroundAudioService.ServiceBinder mService;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = (BackgroundAudioService.ServiceBinder) service;
            mService.createPLayer(MainActivity.this);
            mPlayer = mService.getPlayer();
            mContainer.addView(mPlayer, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            Intent startIntent = new Intent(MainActivity.this, BackgroundAudioService.class);
            startIntent.putExtra("ACTION", "ACTION_START");
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
