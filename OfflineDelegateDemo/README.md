# OfflineDelegateDemo

Offline downloads, including DRM content, managed by the **app**: the app runs its own Media3 `DownloadService` (`DemoDownloadService`, `DownloadTracker`) and connects it to the player through the SDK's `OfflineDelegate` hook (`OfflineDelegateImpl`).

Use this approach if your app already has its own download pipeline. Otherwise, let the SDK manage downloads: see [OfflineDownloadsDemo](../OfflineDownloadsDemo).
