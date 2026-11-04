package com.jwplayer.demo.recyclerview;

import android.content.pm.ActivityInfo;
import android.view.ViewGroup;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jwplayer.pub.api.JWPlayer;
import com.jwplayer.pub.api.fullscreen.FullscreenHandler;
import com.jwplayer.pub.view.JWPlayerView;

/**
 * A {@link FullscreenHandler} for a player inside a RecyclerView cell.
 *
 * The default handler resizes the player inside its parent, which in a list would only fill the
 * cell. This handler instead moves the {@link JWPlayerView} from its cell into the window's
 * content view, hides the system bars and switches to landscape; on exit it puts everything back.
 * While fullscreen, the ViewHolder is marked non-recyclable so the cell isn't reused underneath.
 */
class RecyclerViewFullscreenHandler implements FullscreenHandler {

	private final MainActivity mActivity;
	private final RecyclerView.ViewHolder mViewHolder;
	private final ViewGroup mCellContainer;
	private final JWPlayerView mPlayerView;
	private final JWPlayer mPlayer;
	private final ViewGroup mWindowContent;

	RecyclerViewFullscreenHandler(MainActivity activity, RecyclerView.ViewHolder viewHolder,
								  ViewGroup cellContainer, JWPlayerView playerView, JWPlayer player) {
		mActivity = activity;
		mViewHolder = viewHolder;
		mCellContainer = cellContainer;
		mPlayerView = playerView;
		mPlayer = player;
		mWindowContent = activity.findViewById(android.R.id.content);
	}

	@Override
	public void onFullscreenRequested() {
		mViewHolder.setIsRecyclable(false);
		setSystemBarsVisible(false);
		mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

		mCellContainer.removeView(mPlayerView);
		mWindowContent.addView(mPlayerView, new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mActivity.setFullscreenPlayer(mPlayer);
	}

	@Override
	public void onFullscreenExitRequested() {
		setSystemBarsVisible(true);
		mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

		mWindowContent.removeView(mPlayerView);
		mCellContainer.addView(mPlayerView, new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mActivity.setFullscreenPlayer(null);
		mViewHolder.setIsRecyclable(true);
	}

	private void setSystemBarsVisible(boolean visible) {
		WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
				mActivity.getWindow(), mActivity.getWindow().getDecorView());
		if (visible) {
			controller.show(WindowInsetsCompat.Type.systemBars());
		} else {
			controller.setSystemBarsBehavior(
					WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
			controller.hide(WindowInsetsCompat.Type.systemBars());
		}
	}

	@Override
	public void onAllowRotationChanged(boolean allowRotation) {
	}

	@Override
	public void onAllowFullscreenPortraitChanged(boolean allowFullscreenPortrait) {
	}

	@Override
	public void updateLayoutParams(ViewGroup.LayoutParams layoutParams) {
	}

	@Override
	public void setUseFullscreenLayoutFlags(boolean flags) {
	}
}
