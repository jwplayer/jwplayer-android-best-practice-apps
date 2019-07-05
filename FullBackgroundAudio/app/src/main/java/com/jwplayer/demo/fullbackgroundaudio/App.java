package com.jwplayer.demo.fullbackgroundaudio;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    //Notification related properties
    public static final String CHANNEL_ID = "JW";
    private static final String CHANNEL_NAME = "JW Notifications";
    private static final String CHANNEL_DESCRIPTION = "This is the default notification channel for the JW Player";
    private static final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_LOW;
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

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
