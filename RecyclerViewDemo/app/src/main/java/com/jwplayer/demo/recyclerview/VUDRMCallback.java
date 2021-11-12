package com.jwplayer.demo.recyclerview;

import android.annotation.TargetApi;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.longtailvideo.jwplayer.media.drm.MediaDrmCallback;

import java.util.UUID;

@TargetApi(18)
public class VUDRMCallback implements MediaDrmCallback {

    private static final String WIDEVINE_GTS_DEFAULT_BASE_URI =
            "INSERT_CERTIFICATE_URL";

    private final String defaultUri;

    public VUDRMCallback(String token) {
        String params = "?token=" +  Uri.encode(token);
        defaultUri = WIDEVINE_GTS_DEFAULT_BASE_URI + params;
    }

    @Override
    public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest provisionRequest) throws Exception {
        String url = provisionRequest.getDefaultUrl() + "&signedRequest=" + new String(provisionRequest.getData());
        return Util.executePost(url, null, null);
    }

    @Override
    public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest keyRequest) throws Exception {
        String url = keyRequest.getLicenseServerUrl();
        if (TextUtils.isEmpty(url)) {
            url = defaultUri;
        }
        return Util.executePost(url, keyRequest.getData(), null);
    }
}
