package com.jwplayer.demo.recyclerview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.PlayerState;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.CompleteEvent;
import com.jwplayer.pub.api.events.ErrorEvent;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.PauseEvent;
import com.jwplayer.pub.api.events.PlayEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shows several players in a scrolling {@link RecyclerView}.
 *
 * <ul>
 *     <li>Each video ViewHolder owns exactly one {@link JWPlayerView}. RecyclerView only creates as
 *     many ViewHolders as it needs to fill the screen (plus a small cache), so the number of
 *     players stays bounded however long the list is.</li>
 *     <li>A player is only set up again when its ViewHolder is bound to a <em>different</em> item,
 *     and it is stopped when its ViewHolder is recycled.</li>
 *     <li>Only one player plays at a time: when one starts, the others are paused.</li>
 *     <li>{@link RecyclerViewFullscreenHandler} moves the player out of its list cell into the
 *     window for fullscreen, and back again afterwards.</li>
 * </ul>
 */
public class MainActivity extends AppCompatActivity {

	// Back leaves fullscreen. Enabled only while the player is fullscreen, so otherwise Back
	// behaves normally. Uses the OnBackPressedDispatcher rather than onKeyDown(KEYCODE_BACK).
	private final OnBackPressedCallback mExitFullscreenOnBack = new OnBackPressedCallback(false) {
		@Override
		public void handleOnBackPressed() {
			if (mFullscreenPlayer != null) {
				mFullscreenPlayer.setFullscreen(false, false);
			}
		}
	};

	private static final int TYPE_TEXT = 0;
	private static final int TYPE_VIDEO = 1;

	/** Players that currently belong to a ViewHolder. Bounded by the number of ViewHolders. */
	private final List<JWPlayer> mPlayers = new ArrayList<>();
	private JWPlayer mFullscreenPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout);

		// targetSdk 35+ draws edge-to-edge, so keep the list clear of the system bars.
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recycler_view), (v, windowInsets) -> {
			Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
			return windowInsets;
		});

		// The key comes from the JWPLAYER_LICENSE_KEY environment variable at build time
		// (see app/build.gradle). You can also pass your key here directly.
		new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

		List<Object> items = new ArrayList<>();
		items.add("This demo places JWPlayerViews in a RecyclerView. Only one video plays at a time: "
				+ "starting one pauses the others. Tap fullscreen to move a player out of the list. "
				+ "Scroll around and try it out!");
		items.add(video("https://cdn.jwplayer.com/manifests/Gihxhhy6.m3u8", "Video 1"));
		items.add(video("https://cdn.jwplayer.com/manifests/klAxrVbn.m3u8", "Video 2"));
		items.add(video("https://cdn.jwplayer.com/manifests/j165loUJ.m3u8", "Video 3"));
		items.add("Keep scrolling: the ViewHolders above are recycled and their players reused.");
		items.add(video("https://cdn.jwplayer.com/manifests/Gihxhhy6.m3u8", "Video 4"));
		items.add(video("https://cdn.jwplayer.com/manifests/klAxrVbn.m3u8", "Video 5"));
		items.add(video("https://cdn.jwplayer.com/manifests/j165loUJ.m3u8", "Video 6"));

		RecyclerView recyclerView = findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(new Adapter(items));

		getOnBackPressedDispatcher().addCallback(this, mExitFullscreenOnBack);
	}

	private static PlaylistItem video(String file, String title) {
		return new PlaylistItem.Builder().file(file).title(title).build();
	}

	void setFullscreenPlayer(JWPlayer player) {
		mFullscreenPlayer = player;
		mExitFullscreenOnBack.setEnabled(player != null);
	}

	/** Pauses every player except {@code active}, and keeps the screen on while it plays. */
	private class PlaybackCoordinator implements VideoPlayerEvents.OnPlayListener,
			VideoPlayerEvents.OnPauseListener,
			VideoPlayerEvents.OnCompleteListener,
			VideoPlayerEvents.OnErrorListener {

		private final JWPlayer mPlayer;

		PlaybackCoordinator(JWPlayer player) {
			mPlayer = player;
		}

		@Override
		public void onPlay(PlayEvent playEvent) {
			for (JWPlayer other : mPlayers) {
				if (other != mPlayer) {
					other.pause();
				}
			}
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		@Override
		public void onPause(PauseEvent pauseEvent) {
			clearKeepScreenOn();
		}

		@Override
		public void onComplete(CompleteEvent completeEvent) {
			clearKeepScreenOn();
		}

		@Override
		public void onError(ErrorEvent errorEvent) {
			clearKeepScreenOn();
		}

		private void clearKeepScreenOn() {
			// Another player's PLAY may already have set the flag again; only clear it if nothing plays.
			for (JWPlayer player : mPlayers) {
				if (player.getState() == PlayerState.PLAYING) {
					return;
				}
			}
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private final List<Object> mData;

		Adapter(List<Object> data) {
			mData = Collections.unmodifiableList(data);
		}

		@Override
		public int getItemViewType(int position) {
			return mData.get(position) instanceof PlaylistItem ? TYPE_VIDEO : TYPE_TEXT;
		}

		@Override
		public int getItemCount() {
			return mData.size();
		}

		@NonNull
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			if (viewType == TYPE_VIDEO) {
				return new VideoViewHolder(inflater.inflate(R.layout.recycler_video_cell, parent, false));
			}
			return new TextViewHolder(inflater.inflate(R.layout.recycler_text_cell, parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
			Object item = mData.get(position);
			if (holder instanceof VideoViewHolder) {
				((VideoViewHolder) holder).bind((PlaylistItem) item);
			} else {
				((TextViewHolder) holder).textView.setText((String) item);
			}
		}

		@Override
		public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
			// A cell scrolled off-screen is first kept in RecyclerView's view cache, un-recycled,
			// so onViewRecycled may not run for a while. Pause here so it stops playing as soon
			// as it leaves the screen. The player keeps its position, paused, so scrolling back shows
			// it where it stopped.
			if (holder instanceof VideoViewHolder) {
				JWPlayer player = ((VideoViewHolder) holder).player;
				if (player != mFullscreenPlayer) {
					player.pause();
				}
			}
		}

		@Override
		public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
			if (holder instanceof VideoViewHolder) {
				((VideoViewHolder) holder).unbind();
			}
		}
	}

	private static class TextViewHolder extends RecyclerView.ViewHolder {
		final TextView textView;

		TextViewHolder(View v) {
			super(v);
			textView = v.findViewById(R.id.text_view);
		}
	}

	private class VideoViewHolder extends RecyclerView.ViewHolder {
		final JWPlayer player;
		/** The item the player is currently set up with, or null if it has been stopped. */
		private PlaylistItem mBoundItem;

		VideoViewHolder(View v) {
			super(v);
			ViewGroup container = v.findViewById(R.id.player_container);
			JWPlayerView playerView = v.findViewById(R.id.player_view);
			// The player is created once per ViewHolder and tied to the Activity lifecycle,
			// which releases it when the Activity is destroyed.
			player = playerView.getPlayer(MainActivity.this);
			player.setFullscreenHandler(new RecyclerViewFullscreenHandler(
					MainActivity.this, this, container, playerView, player));
			PlaybackCoordinator coordinator = new PlaybackCoordinator(player);
			player.addListeners(coordinator, EventType.PLAY, EventType.PAUSE,
					EventType.COMPLETE, EventType.ERROR);
			mPlayers.add(player);
		}

		void bind(PlaylistItem item) {
			if (item == mBoundItem) {
				// Rebound to the same item (e.g. after notifyItemChanged): keep playback as is.
				return;
			}
			mBoundItem = item;
			player.setup(new PlayerConfig.Builder()
					.playlist(Collections.singletonList(item))
					.autostart(false)
					.build());
		}

		void unbind() {
			// The ViewHolder is going back to the pool: stop the player so an off-screen video
			// doesn't keep playing or buffering. bind() sets it up again with the new item.
			// If this player is still fullscreen, exit first: that moves it back into its cell
			// and lifts the RecyclerView layout suppression set on entering fullscreen.
			if (player == mFullscreenPlayer) {
				player.setFullscreen(false, false);
			}
			player.stop();
			mBoundItem = null;
		}
	}
}
