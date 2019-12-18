package com.jwplayer.demo.recyclerview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class MainActivity extends AppCompatActivity implements
													VideoPlayerEvents.OnFullscreenListener {

	private ArrayList<JWPlayerView> mPlayers = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		String promoUrl = "http://content.jwplatform.com/manifests/Y5UQq0fG.m3u8";
		ArrayList<ItemBase> items = new ArrayList<>();
		items.add(new TextItem("Item 1"));
		items.add(new TextItem("Item 2"));
		items.add(new VideoItem(promoUrl));
		items.add(new TextItem("Item 4"));
		items.add(new VideoItem(promoUrl));
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

	//	@Override
	//	public boolean onKeyDown(int keyCode, KeyEvent event) {
	//		// If we are in fullscreen mode, exit fullscreen mode when the user uses the back button.
	//		if (keyCode == KeyEvent.KEYCODE_BACK) {
	//			if (mPlayerView.getFullscreen()) {
	//				mPlayerView.setFullscreen(false, true);
	//				return true;
	//			}
	//		}
	//		return super.onKeyDown(keyCode, event);
	//	}

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


	public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private ArrayList<ItemBase> mData;

		private class TextViewHolder extends RecyclerView.ViewHolder {
			public TextView textView;

			public TextViewHolder(View v) {
				super(v);
				textView = (TextView)v.findViewById(R.id.text_view);
			}
		}

		private class VideoViewHolder extends RecyclerView.ViewHolder {
			public JWPlayerView playerView;

			public VideoViewHolder(View v) {
				super(v);
				playerView = (JWPlayerView)v.findViewById(R.id.player_view);
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
									  .inflate(R.layout.fullscreen_test_text_cell, parent, false);
					return new TextViewHolder(v);
				case 1:
					v = LayoutInflater.from(parent.getContext())
									  .inflate(R.layout.fullscreen_test_video_cell, parent, false);
					JWPlayerView playerView = v.findViewById(R.id.player_view);
					new KeepScreenOnHandler(playerView, getWindow());
					playerView.addOnFullscreenListener(MainActivity.this);
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
					PlayerConfig playerConfig = new PlayerConfig.Builder().file(videoItem.url)
																		  .build();
					videoViewHolder.playerView.setup(playerConfig);
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
		public String url;

		VideoItem(String url) {
			this.url = url;
		}
	}

}
