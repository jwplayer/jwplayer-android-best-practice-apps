package com.jwplayer.demo.movableplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * A layout for Movable Views.
 */
public class MovableFrameLayout extends FrameLayout {

    /**
     * Whether this View is movable.
     */
    private boolean mMovable = false;

    public MovableFrameLayout(Context context) {
        super(context);
    }

    public MovableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MovableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MovableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setMovable(boolean movable) {
        mMovable = movable;
    }

    public boolean isMovable() {
        return mMovable;
    }

    /**
     * Intercept touch events (such as MotionEvent.ACTION_MOVE) on this layout if we want to be movable.
     * @see {@link FrameLayout#onInterceptTouchEvent(MotionEvent)}.
     *
     * @param ev the MotionEvent we may want to intercept.
     * @return true if we are intercepting this TouchEvent.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mMovable || super.onInterceptTouchEvent(ev);
    }
}
