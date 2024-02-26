package com.jwplayer.offlinedrmdemo.utils;

import android.os.AsyncTask;
import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JsonDownloader extends AsyncTask<String, Void, String> {


    private final OkHttpClient client;

    private WeakReference<JsonDownloadListener> mOfflineDrmActivity;

    public interface JsonDownloadListener {
        void onJsonDownloadComplete(String playlistJson);
    }

    public JsonDownloader(JsonDownloadListener listener, OkHttpClient client) {
        this.mOfflineDrmActivity = new WeakReference<>(listener);
        this.client = client;
    }

    @Override
    protected String doInBackground(String... params) {
        String url = params[0];
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            mOfflineDrmActivity.get().onJsonDownloadComplete(s);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
