package com.jwplayer.demo.recyclerview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.PlayerState;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class MainActivity extends AppCompatActivity implements
													VideoPlayerEvents.OnFullscreenListener,
													CustomJWPlayerView.ActivePlayerListener {

	private ArrayList<JWPlayerView> mPlayers = new ArrayList<>();
	private JWPlayerView mActivePlayer;
	private KeepScreenOnHandler mKeepScreenOnHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout);

		// INFO: Overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license here
		// [OR] change in app-level build.gradle
		// [OR] set JWPLAYER_LICENSE_KEY as environment variable
		new LicenseUtil().setLicenseKey(this, BuildConfig.JWPLAYER_LICENSE_KEY);

		mKeepScreenOnHandler = new KeepScreenOnHandler(getWindow());

		ArrayList<ItemBase> items = new ArrayList<>();
		items.add(new TextItem("Hi,\nIn this demo we showcase how you can place a JWPlayerView into a RecyclerView and how to implement mutually exclusive playback, so when you press play on one video the other video pauses.\nScroll around and test it out!"));
		items.add(new VideoItem(new PlaylistItem.Builder()
										.file("https://content.jwplatform.com/manifests/3BSFM9FJ.m3u8")
										.image("https://content.jwplatform.com/thumbs/3BSFM9FJ-720.jpg")
										.title("Tears of steel")
										.mediaId("3BSFM9FJ")
										.build()));
		items.add(new VideoItem(new PlaylistItem.Builder()
										.file("https://content.jwplatform.com/manifests/30eyfBgl.m3u8")
										.image("https://content.jwplatform.com/thumbs/30eyfBgl-720.jpg")
										.title("JW Player Promo")
										.mediaId("30eyfBgl")
										.build()));
		items.add(new VideoItem(new PlaylistItem.Builder()
										.file("https://content.jwplatform.com/manifests/tx2vPRG5.m3u8")
										.image("https://content.jwplatform.com/thumbs/tx2vPRG5-720.jpg")
										.mediaId("tx2vPRG5")
										.title("Big buck bunny")
										.build()));
		items.add(new VideoItem(new PlaylistItem.Builder()
										.file("https://content.jwplatform.com/manifests/1sc0kL2N.m3u8")
										.image("https://content.jwplatform.com/thumbs/1sc0kL2N-720.jpg")
										.mediaId("1sc0kL2N")
										.title("Press Play")
										.build()));
		items.add(new VideoItem(new PlaylistItem.Builder()
										.file("https://content.jwplatform.com/manifests/mFq72HEY.m3u8")
										.image("https://content.jwplatform.com/thumbs/mFq72HEY-720.jpg")
										.mediaId("mFq72HEY")
										.title("Jellyfish")
										.build()));

		RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(items);

		RecyclerView recyclerView = findViewById(R.id.recycler_view);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(recyclerViewAdapter);

	}

	@Override
	public void onFullscreen(FullscreenEvent fullscreenEvent) {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			if (fullscreenEvent.getFullscreen()) {
				actionBar.hide();
			} else {
				actionBar.show();
			}
		}
	}

	@Override
	public void onPlayerActive(JWPlayerView activePlayer) {
		mActivePlayer = activePlayer;
		mKeepScreenOnHandler.addListeners(mActivePlayer.getPlayer());

		for(JWPlayerView playerView : mPlayers){
			// If a player was playing, then it was previously set as the active player, pause() and remove listeners
			JWPlayer player = playerView.getPlayer();
			if (player.getState() == PlayerState.PLAYING && !playerView.equals(mActivePlayer)) {
				player.pause();
				mKeepScreenOnHandler.removeListeners(player);
			}
		}
	}


	public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private ArrayList<ItemBase> mData;

		private class TextViewHolder extends RecyclerView.ViewHolder {
			public TextView textView;

			public TextViewHolder(View v) {
				super(v);
				textView = v.findViewById(R.id.text_view);
			}
		}

		private class VideoViewHolder extends RecyclerView.ViewHolder {
			public JWPlayerView playerView;
			public JWPlayer player;

			public VideoViewHolder(View v) {
				super(v);
				playerView = v.findViewById(R.id.player_view);
				player = playerView.getPlayer();
			}
		}

		public RecyclerViewAdapter(ArrayList<ItemBase> data) {
			super();
			mData = data;
		}

		@Override
		public int getItemViewType(int position) {
			if (mData.get(position) instanceof TextItem) {
				return 0;
			} else if (mData.get(position) instanceof VideoItem) {
				return 1;
			}
			return -1;
		}

		@Override
		public int getItemCount() {
			return mData.size();
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v;
			switch (viewType) {
				case 0:
					v = LayoutInflater.from(parent.getContext())
									  .inflate(R.layout.recycler_text_cell, parent, false);
					return new TextViewHolder(v);
				case 1:
					v = LayoutInflater.from(parent.getContext())
									  .inflate(R.layout.recycler_video_cell, parent, false);
					CustomJWPlayerView playerView = v.findViewById(R.id.player_view);
					JWPlayer player = playerView.getPlayer();
					player.addListener(EventType.FULLSCREEN,MainActivity.this);
					playerView.setActivePlayerListener(MainActivity.this);
					mPlayers.add(playerView);
					return new VideoViewHolder(v);
			}
			return null;
		}

		@Override
		public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
			switch (holder.getItemViewType()) {
				case 0:
					TextItem textItem = (TextItem)mData.get(position);
					TextViewHolder textViewHolder = (TextViewHolder)holder;
					textViewHolder.textView.setText(textItem.text);
					break;
				case 1:
					VideoItem videoItem = (VideoItem)mData.get(position);
					VideoViewHolder videoViewHolder = (VideoViewHolder)holder;
					List<PlaylistItem> playlist = new ArrayList<>();
					playlist.add(videoItem.playlistItem);

					PlayerConfig playerConfig = new PlayerConfig.Builder()
							.playlist(playlist)
							.build();
					videoViewHolder.player.setup(playerConfig);
					break;
			}
		}
	}


	class ItemBase {

	}

	class TextItem extends ItemBase {
		public String text;

		TextItem(String text) {
			this.text = text;
		}
	}

	class VideoItem extends ItemBase {
		public PlaylistItem playlistItem;

		VideoItem(PlaylistItem playlistItem) {
			this.playlistItem = playlistItem;
		}
	}

}
