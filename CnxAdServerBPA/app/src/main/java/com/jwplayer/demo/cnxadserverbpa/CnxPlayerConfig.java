package com.jwplayer.demo.cnxadserverbpa;

import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.configuration.ads.CnxAdvertisingConfig;
import com.jwplayer.pub.api.configuration.ads.CnxDynamicAdsConfig;
import com.jwplayer.pub.api.media.ads.AdBreak;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;

import java.util.Arrays;
import java.util.Collections;

/**
 * Builds every player configuration in this app, so the CNX setup is written once.
 *
 * <p>All publisher-specific values come from app/cnx.properties, via {@link BuildConfig}. The
 * ad-server identity is the license key, the App Player ID and the registered application ID
 * together; all three must match your account and registration.
 */
final class CnxPlayerConfig {

    /** How ad breaks are placed in the content. */
    enum AdScheduling {
        /**
         * An explicit schedule: a pre-roll, a mid-roll at 30 seconds, and a post-roll. The ad server
         * auctions each break; no VAST tags are needed.
         */
        MANUAL,
        /**
         * A pre-roll, then mid-rolls placed by the SDK's ad scheduler from the timing rules below, and a
         * post-roll. The ad server auctions each break.
         */
        DYNAMIC
    }

    /** No dynamic mid-roll before this much content, and at least this much content between breaks. */
    static final double DYNAMIC_FIRST_MIDROLL_AFTER = 30;
    static final double DYNAMIC_SECONDS_BETWEEN_MIDROLLS = 60;

    private static final String CONTENT_URL =
            "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8";

    private CnxPlayerConfig() {
    }

    /**
     * @param scheduling                how ad breaks are placed
     * @param autoPauseAdsOnViewability whether a playing ad pauses while the player is scrolled out
     *                                  of view, and resumes when it comes back
     */
    static PlayerConfig make(AdScheduling scheduling, boolean autoPauseAdsOnViewability) {
        PlaylistItem item = new PlaylistItem.Builder()
                .file(CONTENT_URL)
                .title("CNX Ad Server")
                .build();

        CnxAdvertisingConfig.Builder advertising = new CnxAdvertisingConfig.Builder()
                // The application ID your app is registered under for ad serving. It is the app
                // identity the ad service checks. Without this override the SDK sends this build's
                // package name to the ad service (including any applicationIdSuffix).
                .bundleID(BuildConfig.REGISTERED_APPLICATION_ID)
                // "strict" is the default, set here only to make it visible. It never pauses an ad that
                // is already playing: that is what autoPauseAdsOnViewability does.
                .viewabilityPolicy("strict")
                .autoPauseAdsOnViewability(autoPauseAdsOnViewability)
                // Logs ad errors from the ad service to the WebView console. Remove before shipping.
                .debug(true);

        switch (scheduling) {
            case MANUAL:
                advertising.schedule(Arrays.asList(
                        new AdBreak.Builder().offset("pre").build(),
                        new AdBreak.Builder().offset("30").build(),
                        new AdBreak.Builder().offset("post").build()));
                break;
            case DYNAMIC:
                CnxDynamicAdsConfig.CnxDynamicAdRules rules = new CnxDynamicAdsConfig.CnxDynamicAdRules.Builder()
                        // The dynamic pre-roll is controlled by forcePreroll.
                        .forcePreroll(true)
                        .secondsOfContentBeforeFirstAd(DYNAMIC_FIRST_MIDROLL_AFTER)
                        .secondsOfContentBetweenAds(DYNAMIC_SECONDS_BETWEEN_MIDROLLS)
                        .build();
                // Mid- and post-rolls play only at the positions listed here; the rules above decide
                // when. A break with no tag or VAST XML is auctioned by the ad server.
                CnxDynamicAdsConfig.CnxDynamicAdBreak auctionedBreak =
                        new CnxDynamicAdsConfig.CnxDynamicAdBreak.Builder().build();
                advertising.dynamicAds(new CnxDynamicAdsConfig.Builder()
                        .rules(rules)
                        .pre(auctionedBreak)
                        .mid(auctionedBreak)
                        .post(auctionedBreak)
                        .build());
                break;
        }

        return new PlayerConfig.Builder()
                .playlist(Collections.singletonList(item))
                // Your App Player's ID, which scopes ad requests to this placement.
                .playerId(BuildConfig.APP_PLAYER_ID)
                .advertisingConfig(advertising.build())
                .autostart(true)
                .build();
    }
}
