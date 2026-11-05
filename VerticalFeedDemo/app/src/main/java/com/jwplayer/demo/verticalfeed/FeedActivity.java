package com.jwplayer.demo.verticalfeed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.configuration.UiConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.PlayEvent;
import com.jwplayer.pub.api.events.TimeEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A TikTok/Reels-style vertical video feed built on {@link ViewPager2}.
 *
 * <p>The important part is how few players exist. Every {@link JWPlayerView} is a complete player
 * (a WebView, an ExoPlayer and a hardware video decoder), so the feed does <em>not</em> give each
 * page its own player. Instead it keeps a small, fixed {@link #MAX_LIVE_PLAYERS pool}:
 * <ul>
 *     <li>the current page holds a player and plays;</li>
 *     <li>the page above and below (kept by {@code setOffscreenPageLimit(1)}) hold a player that is
 *     set up and paused, so its poster is already showing when you swipe to it;</li>
 *     <li>every other page holds no player at all. When ViewPager2 recycles a page, its player is
 *     stopped and handed back to the pool for the next page.</li>
 * </ul>
 */
public class FeedActivity extends AppCompatActivity {

	/**
	 * The most players that exist at once: the current page plus one neighbour on each side.
	 * Each player costs a WebView, an ExoPlayer, a video decoder and buffer memory. Devices only
	 * have a handful of hardware decoders (shared with every other app), so going beyond about
	 * three causes decoder errors, black frames and out-of-memory crashes on low-end devices.
	 */
	private static final int MAX_LIVE_PLAYERS = 3;

	/**
	 * Feeds start muted: the user did not ask for sound, and autoplaying audio while scrolling is
	 * jarring (and a common app store review complaint). One tap on the speaker unmutes, and the
	 * choice then applies to every page, as users expect.
	 */
	private boolean mMuted = true;

	/** Set when the user taps to pause, so returning to the app does not override their choice. */
	private boolean mUserPaused;

	private int mCurrentPosition = 0;
	private boolean mResumed;
	private Insets mSystemBars = Insets.NONE;

	private final List<FeedItem> mItems = new ArrayList<>();
	/** Every player ever created; never more than {@link #MAX_LIVE_PLAYERS}. */
	private final List<PooledPlayer> mAllPlayers = new ArrayList<>();
	/** Players not attached to any page. */
	private final Deque<PooledPlayer> mFreePlayers = new ArrayDeque<>();
	/** Pages ViewPager2 currently has bound (current page and its neighbours, roughly). */
	private final Set<PageHolder> mBoundPages = new LinkedHashSet<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Draw behind the system bars like a full-screen feed. targetSdk 35+ does this anyway;
		// this makes older Android versions match.
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		setContentView(R.layout.activity_feed);

		// The key comes from the JWPLAYER_LICENSE_KEY environment variable at build time
		// (see app/build.gradle). You can also pass your key here directly.
		new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

		buildFeed();
		createPlayerPool();

		ViewPager2 pager = findViewById(R.id.feed_pager);
		// The video stays full-bleed; only the overlay (title, buttons) is pushed clear of the bars.
		ViewCompat.setOnApplyWindowInsetsListener(pager, (v, windowInsets) -> {
			mSystemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
			for (PageHolder page : mBoundPages) {
				page.applyInsets();
			}
			return windowInsets;
		});

		pager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
		// Keep exactly one page laid out on each side of the current one. Together with the
		// pool size, this is what bounds the number of live players.
		pager.setOffscreenPageLimit(1);
		// ViewPager2 is a RecyclerView inside. By default RecyclerView also keeps a couple of
		// detached pages in a cache without recycling them; those would hold players for nothing.
		// With no cache, a page that leaves the window is recycled at once and its player freed.
		View child = pager.getChildAt(0);
		if (child instanceof RecyclerView) {
			((RecyclerView) child).setItemViewCacheSize(0);
		}
		pager.setAdapter(new FeedAdapter());
		pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				mCurrentPosition = position;
				mUserPaused = false;
				updatePlayback();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mResumed = true;
		updatePlayback();
	}

	@Override
	protected void onPause() {
		// Nothing plays while the feed is not in front of the user.
		mResumed = false;
		for (PooledPlayer pooled : mAllPlayers) {
			pooled.player.pause();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// The players were created with this Activity as their lifecycle owner, so the SDK
		// releases them as the Activity is destroyed. Stop them first so nothing is still loading.
		for (PooledPlayer pooled : mAllPlayers) {
			pooled.player.stop();
		}
		mAllPlayers.clear();
		mFreePlayers.clear();
		mBoundPages.clear();
		super.onDestroy();
	}

	private void buildFeed() {
		// Portrait videos, repeated so there is something to scroll through.
		String[][] media = {
				{"Gihxhhy6", "Portrait clip one"},
				{"klAxrVbn", "Portrait clip two"},
				{"j165loUJ", "Portrait clip three"},
		};
		for (int i = 0; i < 10; i++) {
			String[] m = media[i % media.length];
			mItems.add(new FeedItem(m[0], (i + 1) + ". " + m[1]));
		}
	}

	/** Creates all the players up front, so the count can never grow past the constant. */
	private void createPlayerPool() {
		for (int i = 0; i < MAX_LIVE_PLAYERS; i++) {
			PooledPlayer pooled = new PooledPlayer(new JWPlayerView(this));
			mAllPlayers.add(pooled);
			mFreePlayers.add(pooled);
		}
	}

	/** Plays the current page's player and pauses every other one. */
	private void updatePlayback() {
		PageHolder current = findBoundPage(mCurrentPosition);
		if (current != null && current.player == null) {
			// The pool is empty (a swipe can briefly lay out an extra page): take the player
			// from the page furthest from the one the user is looking at.
			stealPlayerFor(current);
		}
		boolean anyPlaying = false;
		for (PooledPlayer pooled : mAllPlayers) {
			boolean shouldPlay = mResumed && !mUserPaused && pooled.page != null
					&& pooled.page == current;
			if (shouldPlay) {
				pooled.player.play();
				anyPlaying = true;
			} else {
				pooled.player.pause();
			}
		}
		// Keep the screen awake while a video plays, like any video app.
		if (anyPlaying) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	private PageHolder findBoundPage(int position) {
		for (PageHolder page : mBoundPages) {
			if (page.getBindingAdapterPosition() == position) {
				return page;
			}
		}
		return null;
	}

	private void stealPlayerFor(PageHolder target) {
		PageHolder furthest = null;
		int furthestDistance = -1;
		for (PageHolder page : mBoundPages) {
			if (page == target || page.player == null) {
				continue;
			}
			int distance = Math.abs(page.getBindingAdapterPosition() - mCurrentPosition);
			if (distance > furthestDistance) {
				furthest = page;
				furthestDistance = distance;
			}
		}
		if (furthest != null) {
			furthest.releasePlayer();
			target.attachPlayer();
		}
	}

	/** Gives a freed player to a bound page that has none, the current page first. */
	private void attachFreePlayersToWaitingPages() {
		PageHolder current = findBoundPage(mCurrentPosition);
		if (current != null) {
			current.attachPlayer();
		}
		for (PageHolder page : mBoundPages) {
			page.attachPlayer();
		}
	}

	private void toggleMute() {
		mMuted = !mMuted;
		for (PooledPlayer pooled : mAllPlayers) {
			pooled.player.setMute(mMuted);
		}
		for (PageHolder page : mBoundPages) {
			page.updateMuteIcon();
		}
	}

	private void togglePause() {
		mUserPaused = !mUserPaused;
		updatePlayback();
	}

	/** One feed entry: a JW media id and a title. */
	private static class FeedItem {
		final PlaylistItem playlistItem;

		FeedItem(String mediaId, String title) {
			playlistItem = new PlaylistItem.Builder()
					.file("https://cdn.jwplayer.com/manifests/" + mediaId + ".m3u8")
					// The poster shows as soon as the player is set up, before any video renders.
					.image("https://cdn.jwplayer.com/v2/media/" + mediaId + "/poster.jpg")
					.title(title)
					.build();
		}
	}

	/** A player from the pool, and the page it is currently attached to (if any). */
	private class PooledPlayer implements VideoPlayerEvents.OnTimeListener,
			VideoPlayerEvents.OnPlayListener {

		final JWPlayerView view;
		final JWPlayer player;
		PageHolder page;

		PooledPlayer(JWPlayerView view) {
			this.view = view;
			view.setLayoutParams(new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			// Tied to the Activity lifecycle: the SDK releases the player when it is destroyed.
			player = view.getPlayer(FeedActivity.this);
			player.addListeners(this, EventType.TIME, EventType.PLAY);
		}

		void setup(FeedItem item) {
			player.setup(new PlayerConfig.Builder()
					.playlist(Collections.singletonList(item.playlistItem))
					// The feed decides what plays (onPageSelected), not the player.
					.autostart(false)
					// Short-form videos loop until the user swipes away.
					.repeat(true)
					.mute(mMuted)
					// Fill the screen like other vertical feeds; small edges may be cropped.
					.stretching(PlayerConfig.STRETCHING_FILL)
					// We draw our own minimal overlay, so hide every SDK control.
					.uiConfig(new UiConfig.Builder().hideAllControls().build())
					.build());
		}

		@Override
		public void onTime(TimeEvent timeEvent) {
			if (page != null && timeEvent.getDuration() > 0) {
				page.progress.setProgress(
						(int) (1000 * timeEvent.getPosition() / timeEvent.getDuration()));
			}
		}

		@Override
		public void onPlay(PlayEvent playEvent) {
			// Only the current page may play. This catches a play that lands late, after a fast
			// swipe has already moved the user on, so two videos never play at once.
			if (page == null || page.getBindingAdapterPosition() != mCurrentPosition) {
				player.pause();
			}
		}
	}

	private class FeedAdapter extends RecyclerView.Adapter<PageHolder> {

		@NonNull
		@Override
		public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new PageHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.feed_page, parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull PageHolder holder, int position) {
			holder.bind(mItems.get(position));
		}

		@Override
		public void onViewRecycled(@NonNull PageHolder holder) {
			holder.unbind();
		}

		@Override
		public boolean onFailedToRecycleView(@NonNull PageHolder holder) {
			// RecyclerView skips onViewRecycled for a view it cannot recycle (for example one
			// with a running animation). Release the player anyway so it returns to the pool,
			// then let RecyclerView recycle the holder.
			holder.unbind();
			return true;
		}

		@Override
		public int getItemCount() {
			return mItems.size();
		}
	}

	private class PageHolder extends RecyclerView.ViewHolder {
		final ViewGroup playerSlot;
		final View overlay;
		final TextView title;
		final ImageButton muteButton;
		final ProgressBar progress;

		FeedItem item;
		PooledPlayer player;

		PageHolder(View v) {
			super(v);
			playerSlot = v.findViewById(R.id.player_slot);
			overlay = v.findViewById(R.id.overlay);
			title = v.findViewById(R.id.title);
			muteButton = v.findViewById(R.id.mute_button);
			progress = v.findViewById(R.id.progress);
			overlay.setOnClickListener(view -> togglePause());
			muteButton.setOnClickListener(view -> toggleMute());
		}

		void bind(FeedItem newItem) {
			if (newItem != item) {
				item = newItem;
				title.setText(item.playlistItem.getTitle());
				progress.setProgress(0);
			}
			mBoundPages.add(this);
			applyInsets();
			updateMuteIcon();
			attachPlayer();
			updatePlayback();
		}

		void unbind() {
			releasePlayer();
			mBoundPages.remove(this);
			item = null;
			// A player came free: a page still waiting for one can have it now.
			attachFreePlayersToWaitingPages();
			updatePlayback();
		}

		/** Takes a player from the pool, if one is free. Without one the page stays black. */
		void attachPlayer() {
			if (player != null || item == null || mFreePlayers.isEmpty()) {
				return;
			}
			player = mFreePlayers.poll();
			player.page = this;
			playerSlot.addView(player.view);
			// releasePlayer() stops the player, so a player taken from the pool always needs setup().
			player.setup(item);
		}

		/** Stops this page's player and returns it to the pool. */
		void releasePlayer() {
			if (player == null) {
				return;
			}
			// Stop (not just pause) so an off-screen page frees its buffers and decoder.
			player.player.stop();
			player.page = null;
			playerSlot.removeView(player.view);
			mFreePlayers.add(player);
			player = null;
		}

		void applyInsets() {
			overlay.setPadding(mSystemBars.left, mSystemBars.top,
					mSystemBars.right, mSystemBars.bottom);
		}

		void updateMuteIcon() {
			muteButton.setImageResource(mMuted
					? android.R.drawable.ic_lock_silent_mode
					: android.R.drawable.ic_lock_silent_mode_off);
			muteButton.setContentDescription(getString(mMuted ? R.string.unmute : R.string.mute));
		}
	}
}
