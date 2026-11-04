# Vertical Feed Demo

A TikTok/Reels-style vertical video feed built with `ViewPager2` and the JW Player Android SDK's public API.

## What it shows

- A full-screen, edge-to-edge vertical `ViewPager2` (`ORIENTATION_VERTICAL`) with one portrait video per page. The video draws behind the system bars; only the overlay is inset so the title and buttons stay clear of them.
- The current page plays and the previous one pauses (`OnPageChangeCallback#onPageSelected`).
- Each video loops (`PlayerConfig.Builder#repeat(true)`).
- The SDK controls are hidden (`UiConfig.Builder#hideAllControls()`) and the app draws a minimal overlay: a title, a mute button and a thin progress bar driven by `TIME` events.
- Tap anywhere to pause or resume. Mute is shared across pages. The feed **starts muted**, because sound the user did not ask for while scrolling is jarring. One tap unmutes every page.
- Posters (`PlaylistItem.Builder#image`) show before the video renders.
- The Activity is locked to portrait. All players pause when the Activity pauses, and the current page resumes when it comes back (unless the user had paused it).

## How the player pool works

Every `JWPlayerView` is a full player, so pages do **not** each get their own player. `FeedActivity` creates a fixed pool of `MAX_LIVE_PLAYERS = 3` players up front:

- `setOffscreenPageLimit(1)` keeps the current page plus one neighbour on each side laid out. Pages are plain `RecyclerView.Adapter` ViewHolders, not Fragments.
- The RecyclerView item cache inside ViewPager2 is set to 0. A page that leaves the window is recycled right away instead of sitting in the cache with a player it doesn't need.
- When a page is bound it takes a free player from the pool, adds the player's view to its slot, and calls `setup()` with `autostart(false)`. If the same item is bound again without being recycled, `setup()` is not called again.
- When a page is recycled, its player is `stop()`ped, removed from the page and returned to the pool.
- If the current page has no player (for example during a fast swipe), it takes the player from the page furthest away.
- Players are created with `getPlayer(activity)`, so the SDK releases them when the Activity is destroyed.

Neighbouring pages are set up and paused, so their poster is already on screen when you swipe to them. Video buffering starts when the page plays.

## Limitations

- **Every `JWPlayerView` is a complete player**: a WebView, an ExoPlayer instance and a video decoder. That is far heavier than an `ImageView`.
- **Devices have a limited number of hardware video decoders** (shared with other apps) and limited memory. Keeping many players alive leads to decoder errors, black frames, out-of-memory crashes and ANRs. Keep the pool to about three and release players that go off screen, as this sample does. Don't raise `MAX_LIVE_PLAYERS` without testing on low-end devices.
- **Preloading neighbours costs bandwidth.** Each prepared page fetches its manifest and poster even if the user never reaches it.
- **Ads multiply the cost.** An ad schedule on every page means extra requests, extra players and decoders during breaks, and a worse feed. Use frequency limits (for example, an ad every N videos) rather than a pre-roll on every page.
- **Analytics fire per player.** Each page that is set up is a separate player session, so expect setup/view events for pages that were prepared but never watched.
- Test on low-end and older devices, not only on flagships. That is where decoder and memory limits show up first.

## Running

Set your license key as the `JWPLAYER_LICENSE_KEY` environment variable (or edit `app/build.gradle`), then build and run the `app` module.
