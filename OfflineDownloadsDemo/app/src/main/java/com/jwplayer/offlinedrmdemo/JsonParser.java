package com.jwplayer.offlinedrmdemo;

import com.jwplayer.pub.api.configuration.DrmConfig;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {
    private static final String PARAM_PLAYLIST = "playlist";

    public static final String PARAM_TITLE = "title";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_FILE = "file";
    public static final String PARAM_IMAGE = "image";
    public static final String PARAM_MEDIAID = "mediaid";
    public static final String PARAM_SOURCES = "sources";
    public static final String PARAM_DRM = "drm";
    public static final String PARAM_WIDEVINE = "widevine";
    public static final String PARAM_URL = "url";

    public static PlaylistItem parseJson(String jsonString) {
        try {
            JSONObject dapi = new JSONObject(jsonString);
            JSONArray array = dapi.getJSONArray(PARAM_PLAYLIST);
            JSONObject json = array.getJSONObject(0);

            String title = json.optString(PARAM_TITLE, null);
            String description = json.optString(PARAM_DESCRIPTION, null);
            String image = json.optString(PARAM_IMAGE, null);

            // Mandatory fields for Offline DRM
            String mediaId = json.optString(PARAM_MEDIAID, null);
            JSONArray sourcesJson = json.getJSONArray(PARAM_SOURCES);
            String file = null;
            DrmConfig drmConfig = null;
            for (int i = 0; i < sourcesJson.length(); i++) {
                JSONObject source = sourcesJson.getJSONObject(i);
                if (source.has(PARAM_DRM)) {
                    JSONObject drmJson = source.getJSONObject(PARAM_DRM);
                    if (drmJson.has(PARAM_WIDEVINE)) {
                        JSONObject widevine = drmJson.getJSONObject(PARAM_WIDEVINE);
                        String url = widevine.optString(PARAM_URL);
                        drmConfig = new DrmConfig.Builder()
                                .licenseUri(url)
                                .build();
                        file = source.getString(PARAM_FILE);
                    }
                }
            }
            return new PlaylistItem.Builder()
                    .title(title)
                    .description(description)
                    .file(file)
                    .image(image)
                    .mediaId(mediaId)
                    .drmConfig(drmConfig)
                    .build();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
