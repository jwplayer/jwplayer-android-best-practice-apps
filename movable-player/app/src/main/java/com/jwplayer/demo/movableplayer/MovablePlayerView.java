package com.jwplayer.demo.movableplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;

import androidx.core.view.MotionEventCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class MovablePlayerView extends JWPlayerView {


	private boolean mAllowDrag = false;

	private SpringAnimation mSpringAnimationY;
	private SpringAnimation mSpringAnimationX;
	private float mLastTouchX;
	private float mLastTouchY;
	private float mPosX;
	private float mPosY;

	// The ‘active pointer’ is the one currently moving our object.
	private final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;

	public MovablePlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (!isInEditMode()) {
			setupView();
		}
	}

	public MovablePlayerView(Context context,
							 PlayerConfig playerConfig) {
		super(context, playerConfig);
		if (!isInEditMode()) {
			setupView();
		}
	}

	public void toggleDrag() {
		mAllowDrag = !mAllowDrag;
		if (mAllowDrag) {
			this.setScaleX(0.75f);
			this.setScaleY(0.75f);
		} else {
			this.setScaleX(1f);
			this.setScaleY(1f);
			resetToZero();
		}
	}

	private void setupView() {
		mSpringAnimationY = new SpringAnimation(this, DynamicAnimation.TRANSLATION_Y, 0);
		mSpringAnimationY.getSpring().setStiffness(SpringForce.STIFFNESS_MEDIUM);
		mSpringAnimationY.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);

		mSpringAnimationX = new SpringAnimation(this, DynamicAnimation.TRANSLATION_X, 0);
		mSpringAnimationX.getSpring().setStiffness(SpringForce.STIFFNESS_MEDIUM);
		mSpringAnimationX.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY
		);
	}

	private void resetToZero() {
		mSpringAnimationY.animateToFinalPosition(0);
		mSpringAnimationX.animateToFinalPosition(0);
	}


	private void handleDragMotion(MotionEvent ev) {
		if (mAllowDrag) {
			final int action = ev.getActionMasked();

			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					final int pointerIndex = ev.getActionIndex();
					final float x = ev.getX();
					final float y = ev.getY();

					// Remember where we started (for dragging)
					mLastTouchX = x;
					mLastTouchY = y;
					// Save the ID of this pointer (for dragging)
					mActivePointerId = ev.getPointerId(pointerIndex);
					break;
				}

				case MotionEvent.ACTION_MOVE: {
					// Find the index of the active pointer and fetch its position
					final int pointerIndex = ev.findPointerIndex(mActivePointerId);

					final float x = ev.getX(pointerIndex);
					final float y = ev.getY(pointerIndex);

					// Calculate the distance moved
					final float dx = x - mLastTouchX;
					final float dy = y - mLastTouchY;

					mPosX += dx;
					mPosY += dy;
					mSpringAnimationY.animateToFinalPosition(mPosY);
					mSpringAnimationX.animateToFinalPosition(mPosX);
					invalidate();

					// Remember this touch position for the next move event
					mLastTouchX = x;
					mLastTouchY = y;

					break;
				}

				case MotionEvent.ACTION_UP:

				case MotionEvent.ACTION_CANCEL: {
					mActivePointerId = INVALID_POINTER_ID;
					break;
				}

				case MotionEvent.ACTION_POINTER_UP: {

					final int pointerIndex = MotionEventCompat.getActionIndex(ev);
					final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

					if (pointerId == mActivePointerId) {
						// This was our active pointer going up. Choose a new
						// active pointer and adjust accordingly.
						final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
						mLastTouchX = ev.getX(newPointerIndex);
						mLastTouchY = ev.getY(newPointerIndex);
						mActivePointerId = ev.getPointerId(newPointerIndex);
					}
					break;
				}
			}
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		handleDragMotion(ev);
		return super.onInterceptTouchEvent(ev);
	}

}
