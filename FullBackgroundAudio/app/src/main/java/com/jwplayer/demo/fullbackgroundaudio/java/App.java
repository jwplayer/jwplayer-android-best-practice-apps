package com.jwplayer.demo.fullbackgroundaudio.java;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * This class represents the Application, it is referenced in the manifest file under the
 * <application/> tag.
 *
 * The onCreate() method gets called when the user fist starts the app, before any activity is
 * created. That's why this is the best place to set up our notifications.
 *
 *
 */


public class App extends Application {
    //Notification related properties
    public static final String CHANNEL_ID = "JWPlayer";
    private static final String CHANNEL_NAME = "JWPlayer Notifications";
    private static final String CHANNEL_DESCRIPTION = "This is the default notification channel for the JW Player";
    private static final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_LOW;
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    
    /**
     * Starting with Android Oreo (API level 26) Notification channels are required to display
     * notifications. Several channels can be created for different types or categories of
     * notifications. We only need a single channel to display our player notification.
     *
     * First, we check if the device version is higher than O, otherwise there is no need to create
     * and register the channel.
     *
     * @see
     * <a href="https://developer.android.com/training/notify-user/channels">Notification Channels</a>
     *
     * @see <a href="https://developer.android.com/training/notify-user/channels#importance">Channel importance</a>
     */
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Create a notification channel
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                                                                  CHANNEL_IMPORTANCE);
            channel.setDescription(CHANNEL_DESCRIPTION);

            //Register channel with the System.
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
