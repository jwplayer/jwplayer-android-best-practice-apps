package com.jwplayer.offlinedelegate.exo;

import android.content.Context;

import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadCursor;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.jwplayer.pub.api.offline.OfflineDelegate;

import java.io.IOException;

public class OfflineDelegateImpl extends OfflineDelegate {

    public OfflineDelegateImpl(Context applicationContext) {
        super(applicationContext);
    }

    @Override
    public DataSource.Factory getCacheDataSourceFactory(String mediaId) {
        return DemoUtil.getDataSourceFactory(a);
    }

    @Override
    public Download getDownload(String mediaId) {
        DownloadManager manager = DemoUtil.getDownloadManager(a);
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
