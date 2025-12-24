package com.jwplayer.imaadcompanions;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.configuration.ads.ima.ImaCompanionSlot;
import com.jwplayer.pub.api.configuration.ads.ima.ImaVmapAdvertisingConfig;
import com.jwplayer.pub.api.events.AdCompanionsEvent;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.ads.AdCompanion;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdvertisingEvents.OnAdCompanionsListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Handle status bar insets
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, windowInsets) -> {
            Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBars.top, 0, 0);
            return windowInsets;
        });

        // Handle navigation bar insets on bottom content
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content), (v, windowInsets) -> {
            Insets navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navigationBars.bottom);
            return windowInsets;
        });

        new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);
        JWPlayerView view = findViewById(R.id.player);

        JWPlayer player = view.getPlayer(this);
        List<ImaCompanionSlot> list = new ArrayList<>();
        list.add(new ImaCompanionSlot(findViewById(R.id.companionAdSlotOne), 300, 250));
        list.add(new ImaCompanionSlot(findViewById(R.id.companionAdSlotTwo), 728, 90));

        ImaVmapAdvertisingConfig imaAdvertisingConfig = new ImaVmapAdvertisingConfig.Builder()
                .tag("https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpreonly&ciu_szs=300x250%2C728x90&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&correlator=")
                .companionSlots(list)
                .build();

        PlayerConfig config = new PlayerConfig.Builder()
                .playlistUrl("https://cdn.jwplayer.com/v2/media/1sc0kL2N?format=json")
                .advertisingConfig(imaAdvertisingConfig)
                .build();
        player.addListener(EventType.AD_COMPANIONS, this);
        player.setup(config);
    }

    @Override
    public void onAdCompanions(AdCompanionsEvent adCompanionsEvent) {
        List<AdCompanion> companions = adCompanionsEvent.getCompanions();
        StringBuilder builder = new StringBuilder();
        for (AdCompanion companion : companions) {
            builder.append("{");
            builder.append("\n");
            builder.append("   creativeViews=");
            builder.append(Arrays.toString(companion.getCreativeViews().toArray(new String[0])));
            builder.append("\n");
            builder.append("   height=");
            builder.append(companion.getHeight());
            builder.append("\n");
            builder.append("   width=");
            builder.append(companion.getWidth());
            builder.append("\n");
            builder.append("   click=");
            builder.append(companion.getClick());
            builder.append("\n");
            builder.append("   resource=");
            builder.append(companion.getResource());
            builder.append("\n");
            builder.append("   type=");
            builder.append(companion.getType());
            builder.append("\n");
            builder.append("},");
        }
        Log.d("PlayerEvent", "onAdCompanions(AdCompanionsEvent), tag: " + adCompanionsEvent.getTag() +
                ",\ncompanions: \n" + builder);
    }
}