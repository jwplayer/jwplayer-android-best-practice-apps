# OfflineDownloadsDemo

Offline downloads, including Widevine DRM content, managed by the **SDK** with `OfflineDownloadManager` (from `OfflineDownloadFactory`): choose the video and audio renditions, download, then play without a network connection. The SDK runs the download service and stores the offline licence.

Download failures are reported to `MediaDownloadResultListener#onMediaDownloadFailed` with an exception that describes the cause. On Android 13+ the app requests `POST_NOTIFICATIONS` so the download progress notification is shown.

If your app already runs its own Media3 download service, see [OfflineDelegateDemo](../OfflineDelegateDemo).

## Setup

1. Set `JWPLAYER_LICENSE_KEY` in your environment (or edit `app/build.gradle`).
2. Set `JWPLAYER_V1_API_SECRET` to your property's **V1 API secret** (JW Player dashboard, API credentials). The demo uses it to sign the Delivery API URL for the DRM-protected media (`/v2/media/{mediaId}/drm/{policyId}`), which requires a signed JWT. Change `MEDIA_ID` and `POLICY_ID` in `MainActivity` to your own media and DRM policy. Without a secret the app shows a message instead of loading content.

**Signing on the device is for demonstration only.** Anyone can extract a secret shipped in an APK. A real app asks its own backend for the signed URL and never embeds the API secret.
