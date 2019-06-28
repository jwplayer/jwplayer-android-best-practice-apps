package com.jwplayer.demo.simplebackgroundaudio;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;

public class BackgroundAudioService extends Service {
    
    
    /**
     * Our actual Player, that lives inside this service, so it is independent from the activity
     * life cycle.
     */
    private JWPlayerView mPlayer;
    
    /**
     * A simple configuration with a Sample media source
     */
    private PlayerConfig mConfig;
    
    /**
     * A binder interface to allow interaction between the Service and the component (the
     * activity that will show the player) bound to it.
     */
    private IBinder mBinder = new ServiceBinder();
    
    /**
     * Set up the player configuration
     */
    @Override
    public void onCreate() {
        //Create a player config using sample media sources
        mConfig = new PlayerConfig.Builder().playlist(SampleMedia.SAMPLE).build();
    }
    
    /**
     * This method is called when a component binds to this service
     * @param intent
     * @return the binder interface that we can use to get an instance of this
     * service and call its public methods
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    /**
     * Called when a component unbinds. Used to free resources.
     * @param intent
     * @return Whether the service allows rebinding
     */
    @Override
    public boolean onUnbind(Intent intent) {
        mPlayer.onDestroy();
        return false;
    }
    
    /**
     * Custom Interface to provide interaction between the binding activity and this service.
     * Binding activity must call getService() to get an instance of this class and call its public
     * methods.
     */
    public class ServiceBinder extends Binder {
        BackgroundAudioService getService(){
            return BackgroundAudioService.this;
        }
    }
    
    /**
     * Creates a JWPlayer inside this service
     * @param context the activity where this player will be hosted
     * @return the JWPlayer instance
     */
    public JWPlayerView createPlayer(Context context){
        if(context instanceof Activity){
            mPlayer = new JWPlayerView(context, mConfig);
            return mPlayer;
        }
        return null;
    }
    
}
