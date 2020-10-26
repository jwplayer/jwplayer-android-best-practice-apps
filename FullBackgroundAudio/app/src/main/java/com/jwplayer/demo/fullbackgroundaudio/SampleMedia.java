package com.jwplayer.demo.fullbackgroundaudio;

import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.LinkedList;

/**
 * Defines a sample playlist to use for our example.
 * Used as parameter for the PlayerConfig builder.setPlaylist() method.
 *
 */
public class SampleMedia {

    public static final LinkedList<PlaylistItem> SAMPLE = new LinkedList<PlaylistItem>(){
        {
            add(new PlaylistItem.Builder()
                        .file("https://content.jwplatform.com/manifests/mkZVAqxV.m3u8")
                        .image("https://assets-jpcust.jwpsrv.com/thumbs/mkZVAqxV-720.jpg")
                        .mediaId("mkZVAqxV")
                        .description("Caminandes 1: Llama Drama (2013)")
                        .title("Caminandes 1: Llama Drama")
                        .build());
            add(new PlaylistItem.Builder()
                        .file("https://content.jwplatform.com/manifests/t6Kk91mw.m3u8")
                        .image("https://assets-jpcust.jwpsrv.com/thumbs/t6Kk91mw-720.jpg")
                        .mediaId("t6Kk91mw")
                        .description("Caminandes 2: Gran Dillama (2013)")
                        .title("Caminandes 2: Gran Dillama")
                        .build());
            add(new PlaylistItem.Builder()
                        .file("https://content.jwplatform.com/manifests/6QhkxD1P.m3u8")
                        .image("https://assets-jpcust.jwpsrv.com/thumbs/6QhkxD1P-720.jpg")
                        .mediaId("6QhkxD1P")
                        .description("Caminandes 3: Llamigo (2016)")
                        .title("Caminandes 3: Llamigo")
                        .build());
        }
    };
}
