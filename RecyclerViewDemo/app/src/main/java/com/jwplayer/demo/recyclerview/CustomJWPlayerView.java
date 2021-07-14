package com.jwplayer.demo.recyclerview;

import android.content.Context;
import android.util.AttributeSet;

import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.PlayEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.view.JWPlayerView;

public class CustomJWPlayerView extends JWPlayerView implements VideoPlayerEvents.OnPlayListener {

	interface ActivePlayerListener{
		void onPlayerActive(JWPlayerView activePlayer);
	}

	private ActivePlayerListener mActivePlayerListener;

	public CustomJWPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (!isInEditMode()) {
			getPlayer().addListener(EventType.PLAY, this);
		}
	}

	public void setActivePlayerListener(ActivePlayerListener listener){
		mActivePlayerListener = listener;
	}

	@Override
	public void onPlay(PlayEvent playEvent) {
		mActivePlayerListener.onPlayerActive(this);
	}
}
