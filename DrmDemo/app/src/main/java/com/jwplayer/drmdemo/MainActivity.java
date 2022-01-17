package com.jwplayer.drmdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private JWPlayer player;
    private EditText streamURL;
    private EditText authURL;
    private JWPlayerView playerView;

    private QrCodeController qrCodeController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.jwplayerview);
        streamURL = findViewById(R.id.stream_url);
        authURL = findViewById(R.id.auth_url);
        ViewGroup root = findViewById(R.id.root);

        qrCodeController = new QrCodeController(this, streamURL, authURL, root);

        new LicenseUtil().setLicenseKey(this, "YOUR_LICENSE_KEY");

        playerView.getPlayerAsync(this, this, jwPlayer -> player = jwPlayer);

        findViewById(R.id.stream_qr).setOnClickListener(v -> {
            qrCodeController.readStreamQr();
        });

        findViewById(R.id.auth_qr).setOnClickListener(v -> {
            qrCodeController.readAuthQr();
        });

        findViewById(R.id.setup).setOnClickListener(v -> {
            String file = streamURL.getText().toString();
            String auth = authURL.getText().toString();

            if (file.isEmpty() || !Util.isValidURL(file)) {
                Toast.makeText(this, "Stream URL must be a valid URL", Toast.LENGTH_SHORT).show();
                return;
            }
            if (auth.isEmpty() || !Util.isValidURL(auth)) {
                Toast.makeText(this, "Authentication URL must be a valid URL", Toast.LENGTH_SHORT)
                     .show();
                return;
            }

            List<PlaylistItem> playlist = new ArrayList<>();
            playlist.add(new PlaylistItem.Builder()
                                 .file(file)
                                 .mediaDrmCallback(new WidevineCallback(auth))
                                 .build());
            player.setup(new PlayerConfig.Builder()
                                 .playlist(playlist)
                                 .build());

            playerView.setVisibility(View.VISIBLE);
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        qrCodeController.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        qrCodeController.onActivityResult(requestCode, resultCode, data);
    }
}