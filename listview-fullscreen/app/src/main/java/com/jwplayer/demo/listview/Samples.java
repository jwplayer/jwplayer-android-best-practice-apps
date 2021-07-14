package com.jwplayer.demo.listview;


import com.jwplayer.pub.api.media.playlists.PlaylistItem;

public class Samples {
    public static final PlaylistItem[] PLAYLIST = new PlaylistItem[]{
            new PlaylistItem.Builder()
                    .title("VP9 DASH")
                    .description("WebM with VP9 video and Opus audio playing in adaptive DASH.")
                    .file("http://demo.jwplayer.com/vp9-dash/jwpmobile-vp9/jwmobile-vp9.mpd")
                    .build(),
            new PlaylistItem.Builder()
                    .title("Sintel")
                    .description("DASH")
                    .file("http://playertest.longtailvideo.com/android/dash/sintel/sintel.mpd")
                    .build(),
            new PlaylistItem.Builder()
                    .title("BipBop")
                    .description("4x3 Apple HLS")
                    .file("https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/"
                            + "bipbop_4x3_variant.m3u8")
                    .build(),
            new PlaylistItem.Builder()
                    .title("BipBop")
                    .description("16x9 Apple HLS")
                    .file("https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/"
                            + "bipbop_16x9_variant.m3u8")
                    .build(),
            new PlaylistItem.Builder()
                    .title("BipBop")
                    .description("Adaptive HLS stream")
                    .file("http://playertest.longtailvideo.com/adaptive/bipbop/bipbopall.m3u8")
                    .build(),
            new PlaylistItem.Builder()
                    .title("VP9 DASH")
                    .description("WebM with VP9 video and Opus audio playing in adaptive DASH.")
                    .file("http://demo.jwplayer.com/vp9-dash/jwpmobile-vp9/jwmobile-vp9.mpd")
                    .build(),
            new PlaylistItem.Builder()
                    .title("Sintel")
                    .description("DASH")
                    .file("http://playertest.longtailvideo.com/android/dash/sintel/sintel.mpd")
                    .build(),
            new PlaylistItem.Builder()
                    .title("BipBop")
                    .description("4x3 Apple HLS")
                    .file("https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/"
                            + "bipbop_4x3_variant.m3u8")
                    .build(),
            new PlaylistItem.Builder()
                    .title("BipBop")
                    .description("16x9 Apple HLS")
                    .file("https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/"
                            + "bipbop_16x9_variant.m3u8")
                    .build(),
            new PlaylistItem.Builder()
                    .title("BipBop")
                    .description("Adaptive HLS stream")
                    .file("http://playertest.longtailvideo.com/adaptive/bipbop/bipbopall.m3u8")
                    .build()
    };

    private Samples() {
    }
}
