# CNX Ad Server - Best Practice App

Shows how to set up the JW Player Android SDK with JW Player's Ad Server (CNX) for in-app advertising: identity, ad scheduling, event listening, and pausing ads when the player scrolls out of view.

> **Requires JW Player SDK for Android 4.30.0 or later** (the first version that supports Ad Server). This sample runs on Android 8.0 (API 26) or later.

## Before you start

Ads only play when your account is set up for them. None of the following can be checked from the app. Most of them fail the same way: the auction returns no ad, and the event log shows `AD_ERROR 10064: No ad available`.

1. **Your JW Player account is enabled for Ad Server.** Ask your JW Player account manager if unsure.
2. **You have an App Player** in the JW dashboard property you will use: Players → In-app tab. It exists only to give you a player ID for targeting and analytics; styling and player settings come from the SDK, not from the dashboard.
3. **Your Ad Server line items, creatives and targeting rules are in that same property.**
4. **Your app includes the `jwplayer-cnx` module and enables core library desugaring**, which it requires (see `app/build.gradle`). The module brings the Google IMA SDK, which renders the auctions won by IMA ads.
5. **Your app is registered for ad serving.** Registration is not self-service: contact JW Player support to register a new app or confirm an existing registration, and ask which application ID it is registered under. That is the value `REGISTERED_APPLICATION_ID` must hold. An app identity that does not match the registration makes the ad service fail to set up (`AD_ERROR 40100: AdsManager setup failed`), and no ad breaks play.

## Setup

1. Copy `app/cnx.properties.example` to `app/cnx.properties`. It is the **only** place publisher-specific values live, and it is gitignored so your values are never committed.
2. Fill in all three values. Each is documented in the file:

   | Value | What it is | Where to find it |
   | --- | --- | --- |
   | `LICENSE_KEY` | Your JW Player license key | JW dashboard → [your property] → API Credentials → JW Player License Keys |
   | `APP_PLAYER_ID` | The ID of your App Player, from the **same** property (8 letters and digits) | JW dashboard → [your property] → Players → In-app tab → [your App Player] |
   | `REGISTERED_APPLICATION_ID` | The application ID (package name) your app is registered under for ad serving. It is the app identity the ad service checks, so it decides whether ads play | Your ad-serving registration (JW Player support) |

3. Open the `CnxAdServerBPA` directory in Android Studio and run the app.

**The build fails until every value is filled in**, and names each missing or malformed value, where to set it, and where it comes from.

This sample is built as `com.jwplayer.demo.cnxadserverbpa`, which is not registered for ad serving, so it passes `REGISTERED_APPLICATION_ID` to `CnxAdvertisingConfig.Builder.bundleID(...)`. In your own app, the SDK sends your package name automatically, and that is normally the registered ID already. A build with an `applicationIdSuffix` (for example `.debug`) sends the suffixed ID, though, so pass the registered ID to `bundleID(...)` for those builds.

## What this app shows

- **Setup and event listening** (`MainActivity`, the launch screen): the simplest Ad Server setup, with a live log of every ad event, and every player and ad error and warning, under the player. The player is configured in one place, `CnxPlayerConfig`, and events are formatted in `AdEventLog`.
- **Manual and dynamic scheduling** (the ⋮ menu on the launch screen): **Manual** is an explicit pre-roll, mid-roll at 30 seconds and post-roll, with each break auctioned by the ad server, so no VAST tags are needed. **Dynamic** lists which positions may have a break (pre, mid, post) and gives timing rules; the SDK's ad scheduler places the mid-rolls by those rules (none before 30 seconds of content, and at least 60 seconds of content between breaks), and the ad server auctions each break. Mid- and post-rolls play only at the positions you list; the pre-roll is controlled by `forcePreroll`.
- **Pause on scroll / viewability** (`FeedActivity`, **Feed** in the menu): a player inside an article, set up with `autoPauseAdsOnViewability(true)`. Scroll the player out of view during an ad and the ad pauses; scroll it back and it resumes.

## Reading the event log

A no-fill is not a failure of the app. When an auction returns no ad you will see `AD_REQUEST` followed by `AD_ERROR 10064: No ad available`, and content continues. `40100` means the ad service failed to set up for this playback, so none of its ad breaks play. The usual causes are an app identity that doesn't match your registration (check `REGISTERED_APPLICATION_ID`) or a network failure while loading the ad service. Most other account-side misconfigurations (a player ID from a different property than the license key, line items in another property) are not reported as errors: the ad server simply returns no ad, so they look like a `10064` no-fill. If you only ever see `10064`, work through *Before you start*.

If the player never becomes ready and the log shows nothing at all, check logcat for `LICENSE ERROR`: a license key whose edition doesn't include advertising stops the player from setting up, without any player event. The ad error codes are constants in `ErrorCodes`, for example `ErrorCodes.AD_NO_AD_AVAILABLE`.

If you test through a debugging proxy such as Charles or Proxyman, note that apps do not trust user-installed certificates by default. The connection to the ad service then fails, which also shows up as `40100`. To inspect traffic, trust user certificates in a debug-only network security config.

## For production

This app shows the minimum needed for ads to play. Before you ship your own app, also remove `.debug(true)` from your `CnxAdvertisingConfig`.
