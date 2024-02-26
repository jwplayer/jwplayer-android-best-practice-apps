package com.jwplayer.drmdemo.java;

import android.os.Parcel;
import android.text.TextUtils;

import androidx.media3.exoplayer.drm.ExoMediaDrm;

import com.jwplayer.drmdemo.utils.Util;
import com.jwplayer.pub.api.media.drm.MediaDrmCallback;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class WidevineCallback implements MediaDrmCallback {

    private final String defaultUri;
    private Map<String, String> requestProperties = null;

    public WidevineCallback(String drmAuthUrl, Map<String, String> properties) {
        defaultUri = drmAuthUrl;
        requestProperties = properties;
    }

    protected WidevineCallback(Parcel in) {
        defaultUri = in.readString();
    }

    public static final Creator<WidevineCallback> CREATOR = new Creator<WidevineCallback>() {
        @Override
        public WidevineCallback createFromParcel(Parcel in) {
            return new WidevineCallback(in);
        }

        @Override
        public WidevineCallback[] newArray(int size) {
            return new WidevineCallback[size];
        }
    };

    @Override
    public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest request) throws IOException {
        String url = request.getDefaultUrl() + "&signedRequest=" + new String(request.getData());
        return Util.executePost(url, null, requestProperties);
    }

    @Override
    public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest request) throws IOException {
        String url = request.getLicenseServerUrl();
        if (TextUtils.isEmpty(url)) {
            url = defaultUri;
        }
        return Util.executePost(url, request.getData(), requestProperties);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(defaultUri);
    }
}