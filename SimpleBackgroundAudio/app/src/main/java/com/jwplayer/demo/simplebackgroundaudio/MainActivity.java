package com.jwplayer.demo.simplebackgroundaudio;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.longtailvideo.jwplayer.JWPlayerView;


/**
 * A simple activity that hosts {@link JWPlayerView} inside a container.
 *
 */
public class MainActivity extends AppCompatActivity {
    
    /**
     * The player Layout container
     */
    private RelativeLayout mContainer;
    
    /**
     * A handle to the player view that will live in our service.
     */
    private JWPlayerView mPLayer;
    
    /**
     * Instance of our Bound Service
     */
    private BackgroundAudioService mService;
    
    /**
     * Whether the service has been bound
     */
    private boolean isBound = false;
    
    /**
     * Logging tag
     */
    public static final String TAG = "bgAudioService";
    
    /**
     * This object defines the behavior when the service is connected/disconnected.
     * Keep in mind that bindService() returns immediately, but these callbacks are asynchronous
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Use the Binder interface to get an instance of the service
            mService = ((BackgroundAudioService.ServiceBinder) service).getService();
            //Make the service create a player and get a handle to it
             mPLayer = mService.createPlayer(MainActivity.this);
             //Add the player to our activity inside its container
             if(mPLayer != null) {
                 addPlayerToContainer();
             }
            isBound = true;
            Log.d(TAG, "Service Connected");
        }
    
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isBound = false;
            Log.d(TAG, "Service Disconnected");
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        mContainer = findViewById(R.id.container);
        
        //Create an intent to start the service
        Intent serviceIntent = new Intent(this, BackgroundAudioService.class);
    
        //Bind to the service using the intent, the connection object and the type of binding
        //This usually will be BIND_AUTO_CREATE to create the service if it is not already running
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
    
    }
    
    /**
     * Unbind from the service when the application is terminated
     */
    @Override
    protected void onDestroy() {
        if(isBound){
            //Stop the service when the activity gets terminated
            unbindService(mConnection);
            isBound = false;
        }
        super.onDestroy();
    }
    
    /**
     * This method ads the player to the container. Player size and aspect ratio can be
     * customized here by the Layout params.
     */
    private void addPlayerToContainer(){
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mPLayer, params);
    }
}
