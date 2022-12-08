# JW Player SDK for Android - DRM Demo

This application contains an example implementation of DRM using the JW Player SDK for Android.

#### Application Usage instructions:

- Confirm the [requirements](https://docs.jwplayer.com/players/docs/android-get-started#requirements) are met to use the JW Android SDK.
- Clone the repository - `git@github.com:jwplayer/jwplayer-android-best-practice-apps.git`
- Open Android Studio and select: `Open an existing Android Studio project`
- Navigate to the `jwplayer-android-best-practice-apps` directory and select the `DrmDemo` folder
- Update application to use your license key.
  - Replace `YOUR_LICENSE_KEY` with your license key in MainActivity.java `new LicenseUtil().setLicenseKey(this, YOUR_LICENSE_KEY);`
- Replace placeholders or add values to map(s) in MainActivity.java to configure the player
  - Stream and License URL for Studio DRM could be attained [here](https://docs.jwplayer.com/platform/reference/get_v2-media-media-id-drm-policy-id)
  - **streamURL** The DRM protected media URL you intend to play
  - **licenseURL** The DRM license URL passed into the callback to unlock the protected content
  - **requestProperties** Additional properties to pass to your DRM License server via Map (add your values as needed)
  - **httpHeaders** For self hosted content requiring [header authorization](https://docs.jwplayer.com/players/docs/android-add-custom-http-headers) via Map (add your values as needed)
- Select a device to run this application on that has [secure decoders](https://docs.jwplayer.com/players/docs/android-play-drm-protected-content#supported-drm-provider)

The demo application should now [build and run](https://developer.android.com/studio/run).

#### Important links

- JW Deliver API documentation can be found [here](https://docs.jwplayer.com/platform/reference/delivery-api-getting-started)
- JW Studio DRM documentation can be found [here](https://docs.jwplayer.com/platform/reference/studio-drm-standalone-getting-started)
- Android DRM documentation can be found [here](https://docs.jwplayer.com/players/docs/android-apply-studio-drm-with-jw-platform)
- JW Android SDK documentation can be found [here](https://docs.jwplayer.com/players/docs/android-overview)
