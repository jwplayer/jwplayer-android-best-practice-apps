# JW Player SDK for Android - DRM Demo

Plays Widevine DRM-protected content with the JW Player SDK for Android.

## What it shows

The simplest correct path, used by default:

```java
new PlaylistItem.Builder()
        .file(streamURL)
        .drmConfig(new DrmConfig.Builder()
                .licenseUri(licenseURL)
                .build())
        .build();
```

With a `DrmConfig`, the SDK requests the Widevine licence itself. JW Player Studio DRM content
needs nothing more: you do **not** need a custom `MediaDrmCallback`.

`MainActivity.buildItemWithCustomCallback(...)` keeps an optional alternative: a custom
`MediaDrmCallback` (`WidevineCallback`) for licence servers that need extra request headers or a
custom request/response format. Set `USE_CUSTOM_CALLBACK = true` in `MainActivity` to use it. If a
`PlaylistItem` has both, the callback wins.

## SDK APIs

`PlaylistItem.Builder.drmConfig(DrmConfig)`, `DrmConfig.Builder.licenseUri(...)`,
`PlaylistItem.Builder.mediaDrmCallback(MediaDrmCallback)` (optional),
`PlaylistItem.Builder.httpHeaders(...)`, `JWPlayerView.getPlayerAsync(...)`.

## Setup

1. Check the [requirements](https://docs.jwplayer.com/players/docs/android-get-started#requirements)
   for the JW Player Android SDK.
2. Set your JW Player license key as an environment variable before building; Gradle writes it
   into `BuildConfig.JWPLAYER_LICENSE_KEY`:

   ```
   export JWPLAYER_LICENSE_KEY=<your license key>
   ```
3. Fill in the two placeholders in `MainActivity.java`. The app shows
   "Stream and license URL are required" until you do.
   - **streamURL** — the DRM-protected media URL (for Widevine, usually a DASH `.mpd`).
   - **licenseURL** — the Widevine licence server URL for that media.

   For content hosted on JW Player with Studio DRM, request the media from the Delivery API with
   your DRM policy ([`/v2/media/{media_id}/drm/{policy_id}`](https://docs.jwplayer.com/platform/reference/get_v2-media-media-id-drm-policy-id),
   signed as described in the [Studio DRM docs](https://docs.jwplayer.com/platform/reference/studio-drm-standalone-getting-started)).
   In the response, use the Widevine source's `file` as **streamURL** and its
   `drm.widevine.url` as **licenseURL**.

   Alternatively, pass that signed Delivery API URL straight to
   `PlayerConfig.Builder().playlistUrl(...)`: the SDK reads the `drm.widevine.url` from the
   response and builds the `DrmConfig` for you.
4. Run on a device with Widevine [secure decoders](https://docs.jwplayer.com/players/docs/android-play-drm-protected-content#supported-drm-provider)
   (emulators usually can't play DRM content).

## More information

- [Android: apply Studio DRM with JW Platform](https://docs.jwplayer.com/players/docs/android-apply-studio-drm-with-jw-platform)
- [Add custom HTTP headers](https://docs.jwplayer.com/players/docs/android-add-custom-http-headers)
- [JW Delivery API](https://docs.jwplayer.com/platform/reference/delivery-api-getting-started)
