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
 * While fullscreen, the ViewHolder is marked non-recyclable so the cell isn't reused underneath,
 * and the RecyclerView's layout is suppressed: the rotation to landscape would otherwise re-lay
 * out the list and could drop the cell (it may fall outside the new viewport, and a
 * non-recyclable holder is not cached), so on exit the player would be put back into a detached,
 * invisible cell. With layout suppressed the cell stays attached; on exit layout resumes and the
 * cell is scrolled back into view.
 */
class RecyclerViewFullscreenHandler implements FullscreenHandler {

	private final MainActivity mActivity;
	private final RecyclerView.ViewHolder mViewHolder;
	private final ViewGroup mCellContainer;
	private final JWPlayerView mPlayerView;
	private final JWPlayer mPlayer;
	private final ViewGroup mWindowContent;
	private final RecyclerView mRecyclerView;

	RecyclerViewFullscreenHandler(MainActivity activity, RecyclerView.ViewHolder viewHolder,
								  ViewGroup cellContainer, JWPlayerView playerView, JWPlayer player) {
		mActivity = activity;
		mViewHolder = viewHolder;
		mCellContainer = cellContainer;
		mPlayerView = playerView;
		mPlayer = player;
		mWindowContent = activity.findViewById(android.R.id.content);
		mRecyclerView = activity.findViewById(R.id.recycler_view);
	}

	@Override
	public void onFullscreenRequested() {
		mViewHolder.setIsRecyclable(false);
		mRecyclerView.suppressLayout(true);
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

		int position = mViewHolder.getBindingAdapterPosition();
		mRecyclerView.suppressLayout(false);
		if (position != RecyclerView.NO_POSITION) {
			mRecyclerView.scrollToPosition(position);
		}
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
