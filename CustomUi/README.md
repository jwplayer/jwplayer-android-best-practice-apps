# Custom UI

Shows how to replace parts of the JW Player SDK's built-in UI with your own views, in Kotlin.

This app replaces the former Java `CustomUi` and Kotlin `KotlinCustomUi` samples, which showed the same thing in two languages.

## What it shows

- `JWPlayerViewExample` creates the player with `JWPlayerView.getPlayerAsync(...)`, which ties the player to the Activity lifecycle (no manual `onPause`/`onDestroy` handling needed).
- The SDK UI is kept, but the built-in Next Up card is hidden with `UiConfig.Builder().displayAllControls().hide(UiGroup.NEXT_UP)`.
- `MyControls` is a `ViewGroup` added on top of the `JWPlayerView`. It binds to the SDK's UI view models (`JWPlayer.getViewModelForUiGroup`) so your views stay in sync with the player.
- `MyNextUpView` is a custom Next Up card driven by the `NEXT_UP` view model (poster loaded with Picasso).
- `KeepScreenOnHandler` keeps the screen awake while content or ads play.
- Rotating to landscape enters fullscreen; Back leaves fullscreen.

## Setup

The license key is read at build time from the `JWPLAYER_LICENSE_KEY` environment variable and exposed as `BuildConfig.JWPLAYER_LICENSE_KEY`:

```sh
export JWPLAYER_LICENSE_KEY=<your license key>
./gradlew :app:installDebug
```

For more information see the [Android developer guide](https://docs.jwplayer.com/players/docs/android-overview).
