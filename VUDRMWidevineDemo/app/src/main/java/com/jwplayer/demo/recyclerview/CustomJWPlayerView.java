package com.jwplayer.demo.recyclerview;

import android.content.Context;
import android.util.AttributeSet;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.events.PlayEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

public class CustomJWPlayerView extends JWPlayerView implements VideoPlayerEvents.OnPlayListener {

	interface ActivePlayerListener{
		void onPlayerActive(JWPlayerView activePlayer);
	}

	private ActivePlayerListener mActivePlayerListener;

	public CustomJWPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (!isInEditMode()) {
			this.addOnPlayListener(this);
		}
	}

	public CustomJWPlayerView(Context context,
							  PlayerConfig playerConfig) {
		super(context, playerConfig);
		this.addOnPlayListener(this);
	}

	public void setActivePlayerListener(ActivePlayerListener listener){
		mActivePlayerListener = listener;
	}

	@Override
	public void onPlay(PlayEvent playEvent) {
		mActivePlayerListener.onPlayerActive(this);
	}
}
