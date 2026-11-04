# Styling Demo

Restyles the JW Player SDK's built-in controls with Android resource overrides, without writing
any custom UI code.

## What it shows

The app defines resources with the **same names** as the SDK's own, and Android's resource merging
makes the app's versions win:

- **Colors** (`res/values/colors.xml`): `jw_seekbar_progress`, `jw_labels_primary`,
  `jw_controls_overlay`, `jw_surface_secondary`, ... recolor the controls, seek bar and overlays.
- **Styles** (`res/values/styles.xml`): `JWText`, `JWIcon`, `JWRadio`, `JWText.OverlayTitle`, ...
  change fonts (the bundled `console` font), text sizes and icon tints.
- **Dimensions** (`res/values/dimens.xml`): `jw_large_icon`, `jw_text_*` resize icons and text.
- **Drawables** (`res/drawable/ic_jw_*.xml`): replace individual icons, e.g. the play button.
- **Strings** (`res/values/strings.xml`, `res/values-en/strings.xml`): replace UI text such as
  `jwplayer_just_watched`, per locale.

The SDK's resources (in the `jwplayer-common` / `jwplayer-core` AARs) are the reference for which
names you can override. The Chromecast module is included so the cast UI picks up the same styling.

## SDK APIs

Resource overrides of SDK resource names; `JWPlayerView.getPlayer(LifecycleOwner)`,
`PlayerConfig.Builder().playlistUrl(...)`, `JWPlayer.setFullscreen(...)`.

## Setup

Set your JW Player license key as an environment variable before building; Gradle writes it into
`BuildConfig.JWPLAYER_LICENSE_KEY`:

```
export JWPLAYER_LICENSE_KEY=<your license key>
./gradlew :app:installDebug
```
