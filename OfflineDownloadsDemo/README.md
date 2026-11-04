# OfflineDownloadsDemo

Offline downloads, including Widevine DRM content, managed by the **SDK** with `OfflineDownloadManager` (from `OfflineDownloadFactory`): choose the video and audio renditions, download, then play without a network connection. The SDK runs the download service and stores the offline licence.

Download failures are reported to `MediaDownloadResultListener#onMediaDownloadFailed` with an exception that describes the cause. On Android 13+ the app requests `POST_NOTIFICATIONS` so the download progress notification is shown.

If your app already runs its own Media3 download service, see [OfflineDelegateDemo](../OfflineDelegateDemo).
