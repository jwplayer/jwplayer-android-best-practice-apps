package com.jwplayer.demo.recyclerview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.core.PlayerState;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;

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

		mKeepScreenOnHandler = new KeepScreenOnHandler(getWindow());

		ArrayList<ItemBase> items = new ArrayList<>();
		items.add(new TextItem("Hi,\nIn this demo we showcase how you can place a JWPlayerView into a RecyclerView and how to implement mutually exclusive playback, so when you press play on one video the other video pauses.\nScroll around and test it out!"));

		// TODO: Enter your content configs -->
		// Adds item with no DRM
		items.add(new VideoItem(new PlaylistItem.Builder()
										.file(INSERT_CONTENT_URL)
										.title(INSERT_TITLE)
										.build()));
		// Adds item with DRM
		items.add(new VideoItem(new PlaylistItem.Builder()
										.file(INSERT_CONTENT_URL)
										.title(INSERT_TITLE)
										.mediaDrmCallback(new VUDRMCallback(INSERT_TOKEN))
										.build()));

		RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(items);

		RecyclerView recyclerView = findViewById(R.id.recycler_view);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(recyclerViewAdapter);

	}


	@Override
	protected void onStop() {
		super.onStop();
		for (JWPlayerView player : mPlayers) {
			player.onStop();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		for (JWPlayerView player : mPlayers) {
			player.onStart();
		}
	}

	@Override
	protected void onResume() {
		// Let JW Player know that the app has returned from the background
		super.onResume();
		for (JWPlayerView player : mPlayers) {
			player.onResume();
		}
	}

	@Override
	protected void onPause() {
		// Let JW Player know that the app is going to the background
		super.onPause();
		for (JWPlayerView player : mPlayers) {
			player.onPause();
		}
	}

	@Override
	protected void onDestroy() {
		// Let JW Player know that the app is being destroyed
		super.onDestroy();
		for (JWPlayerView player : mPlayers) {
			player.onDestroy();
		}
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
		mKeepScreenOnHandler.addListeners(mActivePlayer);

		for(JWPlayerView player : mPlayers){
			// If a player was playing, then it was previously set as the active player, pause() and remove listeners
			if (player.getState() == PlayerState.PLAYING && !player.equals(mActivePlayer)) {
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

			public VideoViewHolder(View v) {
				super(v);
				playerView = v.findViewById(R.id.player_view);
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
					playerView.addOnFullscreenListener(MainActivity.this);
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
					videoViewHolder.playerView.load(videoItem.playlistItem);
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
