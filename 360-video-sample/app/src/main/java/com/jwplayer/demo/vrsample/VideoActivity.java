package com.jwplayer.demo.vrsample;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.longtailvideo.jwplayer.vr.JWVrVideoView;
import com.longtailvideo.jwplayer.vr.utils.MediaUrlUtil;

public class VideoActivity extends AppCompatActivity implements JWVrVideoView.EventListener {

	private static final String TAG = "VideoActivity";

	public static final String CONTENT_TYPE_EXTRA = "content_type";
	public static final String STEREO_MODE_EXTRA = "stereo_mode";
	public static final int UNKNOWN = -1;

	private CoordinatorLayout mCoordinatorLayout;

	/**
	 * The view that the VideoRender renders too.
	 */
	private JWVrVideoView mVrVideoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		//noinspection ConstantConditions
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Start in portrait
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

		// Initialize the JWPlayerVrView
		mVrVideoView = (JWVrVideoView) findViewById(R.id.vr_view);
		mVrVideoView.post(new Runnable() {
			@Override
			public void run() {
				mVrVideoView.setLayoutParams(
						new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
								calculateHeight(mVrVideoView.getWidth()))); // Force 16:9 aspect ratio
			}
		});

		// Set this activity as event listener for the VrVideoView.
		mVrVideoView.setEventListener(this);
	}


	@Override
	protected void onPause() {
		super.onPause();
		mVrVideoView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		Uri contentUri = intent.getData();
		int contentType = intent.getIntExtra(CONTENT_TYPE_EXTRA,
				MediaUrlUtil.inferContentType(contentUri));
		if (contentType == UNKNOWN) {
			contentType = MediaUrlUtil.inferContentType(contentUri);
		}
		@JWVrVideoView.StereoMode int stereoMode = intent.getIntExtra(STEREO_MODE_EXTRA, JWVrVideoView.STEREO_MODE_MONO);
		if (!mVrVideoView.hasRenderer()) {
			//noinspection WrongConstant
			mVrVideoView.load(contentUri, stereoMode != UNKNOWN
					? stereoMode : JWVrVideoView.STEREO_MODE_MONO, contentType, true);
		}
		mVrVideoView.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mVrVideoView.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (mVrVideoView.isFullscreen()) {
			mVrVideoView.toggleFullscreen(false);
		} else {
			super.onBackPressed();
		}
	}

	/*
	 * JWVrVideoView.EventListener implementation.
	 */

	@Override
	public void onCardboardTrigger() {
		if (mVrVideoView.isPlaying() && mVrVideoView.getPosition() > 2000) {
			mVrVideoView.pause();
		} else {
			mVrVideoView.play();
		}
	}

	@Override
	public void onBackButton() {
		mVrVideoView.toggleFullscreen(false);
	}

	@Override
	public void onFullscreenToggled() {
		toggleActionBar();
		mCoordinatorLayout.setFitsSystemWindows(!mCoordinatorLayout.getFitsSystemWindows());
	}

	@Override
	public void onVRModeToggled(boolean enabled) {

	}

	@Override
	public void onStateChanged(@JWVrVideoView.PlayerState int newState,
							   @JWVrVideoView.PlayerState int oldState) {
		Log.i(TAG, "onStateChanged(newState, oldState), newState: "
				+ stateToString(newState) + ", oldState: " + stateToString(oldState));
	}


	private void toggleActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			if (actionBar.isShowing()) {
				actionBar.hide();
			} else {
				actionBar.show();
			}
		}
	}

	private int calculateHeight(int width) {
		return (width / 16) * 9; // 16:9 aspect ratio
	}


	private String stateToString(@JWVrVideoView.PlayerState int state) {
		switch (state) {
			case JWVrVideoView.STATE_IDLE:
				return "idle";
			case JWVrVideoView.STATE_BUFFERING:
				return "buffering";
			case JWVrVideoView.STATE_COMPLETE:
				return "complete";
			case JWVrVideoView.STATE_PAUSED:
				return "paused";
			case JWVrVideoView.STATE_PLAYING:
				return "playing";
		}
		return "";
	}
}
