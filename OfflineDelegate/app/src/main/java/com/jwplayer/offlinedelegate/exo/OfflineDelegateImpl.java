package com.jwplayer.offlinedelegate.exo;

import android.content.Context;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadCursor;
import androidx.media3.exoplayer.offline.DownloadManager;

import com.jwplayer.pub.api.offline.OfflineDelegate;

import java.io.IOException;

@UnstableApi public class OfflineDelegateImpl extends OfflineDelegate {

    public OfflineDelegateImpl(Context applicationContext) {
        super(applicationContext);
    }

    @Override
    public DataSource.Factory getCacheDataSourceFactory(String mediaId) {
        return DemoUtil.getDataSourceFactory(mApplicationContext);
    }

    @Override
    public Download getDownload(String mediaId) {
        DownloadManager manager = DemoUtil.getDownloadManager(mApplicationContext);
        try (DownloadCursor loadedDownloads = manager.getDownloadIndex().getDownloads()) {
            while (loadedDownloads.moveToNext()) {
                Download download = loadedDownloads.getDownload();
                if (download.request.id.equals(mediaId)){
                    return download;
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }
}
