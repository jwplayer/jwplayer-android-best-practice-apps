package com.jwplayer.offlinedrmdemo;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Fetches the Delivery API playlist JSON off the main thread with {@link HttpURLConnection}.
 */
public class JsonDownloader extends AsyncTask<String, Void, String> {

    private final JsonDownloadListener mListener;

    public interface JsonDownloadListener {
        void onJsonDownloadComplete(String playlistJson);
    }

    public JsonDownloader(JsonDownloadListener listener) {
        this.mListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(params[0]).openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "";
            }
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line).append('\n');
                }
            }
            return body.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if (mListener != null) {
            mListener.onJsonDownloadComplete(s);
        }
    }
}
