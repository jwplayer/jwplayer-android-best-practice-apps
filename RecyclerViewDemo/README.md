# RecyclerView Demo

Shows several JW Player instances in a scrolling `RecyclerView` — the recommended pattern for
players in a feed or list.

## What it shows

- **One player per ViewHolder.** Each video cell owns a single `JWPlayerView`, created once in
  the ViewHolder constructor with `JWPlayerView.getPlayer(lifecycleOwner)`. RecyclerView only
  creates enough ViewHolders to fill the screen, so the number of players stays bounded no
  matter how long the list is.
- **Correct recycling.** `onBindViewHolder` only calls `JWPlayer.setup()` when the cell is bound
  to a different item, and `onViewRecycled` calls `JWPlayer.stop()` so off-screen cells don't
  keep playing or buffering.
- **One video at a time.** A `PLAY` listener pauses every other player, and the screen is kept
  on only while something is playing.
- **Fullscreen from a list cell.** `RecyclerViewFullscreenHandler` is a custom
  `FullscreenHandler` (set with `JWPlayer.setFullscreenHandler()`) that moves the
  `JWPlayerView` out of its cell into the window's content view, hides the system bars and
  switches to landscape, then puts it back on exit. The ViewHolder is marked non-recyclable
  while fullscreen. Back exits fullscreen.

## SDK APIs

`JWPlayerView`, `JWPlayer.setup(PlayerConfig)`, `JWPlayer.stop()`, `JWPlayer.pause()`,
`JWPlayer.addListeners(...)` with `EventType.PLAY/PAUSE/COMPLETE/ERROR`,
`JWPlayer.setFullscreenHandler(FullscreenHandler)`, `JWPlayer.setFullscreen(...)`.

## Setup

Set your JW Player license key as an environment variable before building; Gradle writes it into
`BuildConfig.JWPLAYER_LICENSE_KEY`:

```
export JWPLAYER_LICENSE_KEY=<your license key>
./gradlew :app:installDebug
```
