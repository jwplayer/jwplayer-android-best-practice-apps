package com.jwplayer.demo.backgroundaudio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Picks how the background-audio notification is customised, then opens the player.
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.mode_buttons).setOnClickListener(v -> open(PlayerActivity.MODE_BUTTONS));
        findViewById(R.id.mode_legacy).setOnClickListener(v -> open(PlayerActivity.MODE_LEGACY));
        findViewById(R.id.mode_default).setOnClickListener(v -> open(PlayerActivity.MODE_DEFAULT));

        // Android 13+: recommended. The SDK's media-session notification is exempt from the
        // runtime permission, but other app notifications need it and some OEM builds hide media
        // notifications without it. Background playback works either way.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
        }
    }

    private void open(String mode) {
        boolean sdkAudioFocus = ((CheckBox) findViewById(R.id.sdk_audio_focus)).isChecked();
        startActivity(new Intent(this, PlayerActivity.class)
                .putExtra(PlayerActivity.EXTRA_MODE, mode)
                .putExtra(PlayerActivity.EXTRA_SDK_AUDIO_FOCUS, sdkAudioFocus));
    }
}
