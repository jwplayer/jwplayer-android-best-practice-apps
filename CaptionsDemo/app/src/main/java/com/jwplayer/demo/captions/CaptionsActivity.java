package com.jwplayer.demo.captions;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.CaptionPosition;
import com.jwplayer.pub.api.configuration.CaptionsConfig;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.CaptionsChangedEvent;
import com.jwplayer.pub.api.events.CaptionsListEvent;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.captions.Caption;
import com.jwplayer.pub.api.media.captions.CaptionType;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Captions with the JW Player SDK: sideloaded tracks, in-manifest subtitles, caption styling
 * and placement, and a custom track picker driven by the captions events.
 */
public class CaptionsActivity extends AppCompatActivity implements
        VideoPlayerEvents.OnCaptionsListListener,
        VideoPlayerEvents.OnCaptionsChangedListener {

    private JWPlayer player;
    private Spinner picker;
    private ArrayAdapter<String> pickerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captions);

        // INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
        // [OR] change in app-level build.gradle
        // [OR] set JWPLAYER_LICENSE_KEY as environment variable
        new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

        picker = findViewById(R.id.track_picker);
        pickerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new ArrayList<>());
        pickerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        picker.setAdapter(pickerAdapter);
        picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // The spinner position is the index in the player's captions list, so it maps
                // straight onto setCurrentCaptions. Skip it when the player already has that
                // track, which is the case when the spinner was moved by onCaptionsChanged.
                if (player != null && player.getCurrentCaptions() != position) {
                    player.setCurrentCaptions(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        JWPlayerView playerView = findViewById(R.id.jwplayer);
        player = playerView.getPlayer(this);

        // CAPTIONS_LIST fires for every playlist item once its tracks are known (sideloaded
        // and in-manifest alike); CAPTIONS_CHANGED fires whenever the active track changes,
        // including from the player's own captions menu. Listening to both keeps the picker
        // in sync whichever UI the viewer uses.
        player.addListener(EventType.CAPTIONS_LIST, this);
        player.addListener(EventType.CAPTIONS_CHANGED, this);

        player.setup(new PlayerConfig.Builder()
                .playlist(createPlaylist())
                .captionsConfig(createCaptionsConfig())
                .build());
    }

    @Override
    public void onCaptionsList(CaptionsListEvent event) {
        // On Android the list always starts with an "Off" entry, so index 0 turns captions off.
        List<String> labels = new ArrayList<>();
        for (Caption caption : event.getCaptions()) {
            labels.add(caption.getLabel());
        }
        pickerAdapter.clear();
        pickerAdapter.addAll(labels);
        pickerAdapter.notifyDataSetChanged();
        picker.setSelection(event.getCurrentCaptionIndex(), false);
    }

    @Override
    public void onCaptionsChanged(CaptionsChangedEvent event) {
        int index = event.getCurrentTrack();
        if (index >= 0 && index < pickerAdapter.getCount()) {
            picker.setSelection(index, false);
        }
    }

    /**
     * Styling and placement for every caption track. With the default override strategy
     * ({@link CaptionsConfig#OVERRIDE_STRATEGY_VIDEO_OVERRIDE_ONLY}) these values apply only
     * where the viewer has not chosen a style in the system caption settings, so accessibility
     * choices win. {@link CaptionsConfig#OVERRIDE_STRATEGY_ALWAYS} would force these values.
     */
    private static CaptionsConfig createCaptionsConfig() {
        CaptionPosition position = new CaptionPosition.Builder()
                .width(80)                                   // box is 80% of the video width
                .alignment(CaptionPosition.ALIGNMENT_CENTER) // text centred in the box
                .linePercentage(85)                          // top of the box 85% down
                .build();

        return new CaptionsConfig.Builder()
                .fontSize(16)                                // in sp
                .color(Color.YELLOW)
                .backgroundColor(Color.argb(160, 0, 0, 0))   // translucent box behind the text
                .edgeStyle(CaptionsConfig.EDGE_STYLE_DROP_SHADOW)
                .position(position)
                .overrideStrategy(CaptionsConfig.OVERRIDE_STRATEGY_VIDEO_OVERRIDE_ONLY)
                .build();
    }

    private static List<PlaylistItem> createPlaylist() {
        List<PlaylistItem> playlist = new ArrayList<>();

        // 1. A sideloaded WebVTT track, marked as the default so it is on at start.
        playlist.add(new PlaylistItem.Builder()
                .file("https://cdn.jwplayer.com/manifests/NFl4Yrhl.m3u8")
                .title("Sideloaded WebVTT")
                .image("https://cdn.jwplayer.com/v2/media/NFl4Yrhl/poster.jpg")
                .tracks(Collections.singletonList(new Caption.Builder()
                        .file("https://cdn.jwplayer.com/tracks/EcLZ5shp.vtt")
                        .kind(CaptionType.CAPTIONS)
                        .label("English")
                        .isDefault(true)
                        .build()))
                .build());

        // 2. Two sideloaded languages. The SDK also accepts SRT files for sideloaded tracks.
        List<Caption> tracks = new ArrayList<>();
        tracks.add(new Caption.Builder()
                .file("https://cdn.jwplayer.com/tracks/a2J6cY6H.srt")
                .kind(CaptionType.CAPTIONS)
                .label("English")
                .build());
        tracks.add(new Caption.Builder()
                .file("https://cdn.jwplayer.com/tracks/0gyZJgjM.srt")
                .kind(CaptionType.CAPTIONS)
                .label("Español")
                .build());
        playlist.add(new PlaylistItem.Builder()
                .file("https://cdn.jwplayer.com/manifests/QUxmKbSS.m3u8")
                .title("Two sideloaded languages")
                .image("https://cdn.jwplayer.com/v2/media/QUxmKbSS/poster.jpg")
                .tracks(tracks)
                .build());

        // 3. Subtitles declared in the HLS manifest: no tracks needed, the player finds them.
        playlist.add(new PlaylistItem.Builder()
                .file("https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8")
                .title("In-manifest subtitles")
                .build());

        return playlist;
    }
}
