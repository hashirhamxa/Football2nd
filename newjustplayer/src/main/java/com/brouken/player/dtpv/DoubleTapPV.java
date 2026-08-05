package com.brouken.player.dtpv;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;

import com.brouken.player.CustomPlayerView;

/**
 * Custom player class for Double-Tapping listening
 */
public class DoubleTapPV extends CustomPlayerView {

    private final GestureDetectorCompat gestureDetector;
    private final DoubleTapPV.DoubleTapGestureListener gestureListener;

    private PlayerDoubleTapListenerInterface controller;

    private final PlayerDoubleTapListenerInterface getController() {
        return gestureListener.getControls();
    }

    private final void setController(PlayerDoubleTapListenerInterface value) {
        gestureListener.setControls(value);
        controller = value;
    }

    private int controllerRef;

    public DoubleTapPV(Context context) {
        this(context, null);
    }

    public DoubleTapPV(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleTapPV(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        controllerRef = -1;

        gestureListener = new DoubleTapGestureListener(this);
        gestureDetector = new GestureDetectorCompat(context, gestureListener);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, com.github.vkay94.dtpv.R.styleable.DoubleTapPlayerView, 0, 0);
            controllerRef = a != null ? a.getResourceId(com.github.vkay94.dtpv.R.styleable.DoubleTapPlayerView_dtpv_controller, -1) : -1;
            if (a != null) {
                a.recycle();
            }
        }

        isDoubleTapEnabled = true;
        doubleTapDelay = 700L;
    }

    /**
     * If this field is set to `true` this view will handle double tapping, otherwise it will
     * handle touches the same way as the original [PlayerView][com.google.android.exoplayer2.ui.PlayerView] does
     */
    private boolean isDoubleTapEnabled;

    public final boolean isDoubleTapEnabled() {
        return isDoubleTapEnabled;
    }

    public final void setDoubleTapEnabled(boolean var1) {
        isDoubleTapEnabled = var1;
    }

    /**
     * Time window a double tap is active, so a followed tap is calling a gesture detector
     * method instead of normal tap (see [PlayerView.onTouchEvent])
     */
    private long doubleTapDelay;

    public final long getDoubleTapDelay() {
        return gestureListener.getDoubleTapDelay();
    }

    public final void setDoubleTapDelay(long value) {
        gestureListener.setDoubleTapDelay(value);
        doubleTapDelay = value;
    }

    /**
     * Sets the [PlayerDoubleTapListenerInterface] which handles the gesture callbacks.
     * <p>
     * Primarily used for [YTOverlay][com.github.vkay94.dtpv.youtube.YTOverlay]
     */
    public final DoubleTapPV controller(PlayerDoubleTapListenerInterface controller) {
        setController(controller);
        return this;
    }

    /**
     * Returns the current state of double tapping.
     */
    public final boolean isInDoubleTapMode() {
        return gestureListener.isDoubleTapping();
    }

    /**
     * Resets the timeout to keep in double tap mode.
     * <p>
     * Called once in [PlayerDoubleTapListenerInterface.onDoubleTapStarted]. Needs to be called
     * from outside if the double tap is customized / overridden to detect ongoing taps
     */
    public final void keepInDoubleTapMode() {
        gestureListener.keepInDoubleTapMode();
    }

    /**
     * Cancels double tap mode instantly by calling [PlayerDoubleTapListenerInterface.onDoubleTapFinished]
     */
    public final void cancelInDoubleTapMode() {
        gestureListener.cancelInDoubleTapMode();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isDoubleTapEnabled) {
            boolean consumed = gestureDetector.onTouchEvent(ev);

            // Do not trigger original behavior when double tapping
            // otherwise the controller would show/hide - it would flack
            if (!consumed)
                return super.onTouchEvent(ev);

            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // If the PlayerView is set by XML then call the corresponding setter method
        if (controllerRef != -1) {
            try {
                View view = ((View) getParent()).findViewById(this.controllerRef);
                if (view instanceof PlayerDoubleTapListenerInterface) {
                    controller((PlayerDoubleTapListenerInterface) view);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("DoubleTapPV", "controllerRef is either invalid or not PlayerDoubleTapListenerInterface: ${e.message}");
            }
        }
    }

    /**
     * Gesture Listener for double tapping
     * <p>
     * For more information which methods are called in certain situations look for
     * [GestureDetector.onTouchEvent][android.view.GestureDetector.onTouchEvent],
     * especially for ACTION_DOWN and ACTION_UP
     */
    private static final class DoubleTapGestureListener extends GestureDetector.SimpleOnGestureListener {
        private final Handler mHandler;
        private final Runnable mRunnable;

        private PlayerDoubleTapListenerInterface controls;
        private boolean isDoubleTapping;
        private long doubleTapDelay;

        public final boolean isDoubleTapping() {
            return isDoubleTapping;
        }

        public final void setDoubleTapping(boolean var1) {
            isDoubleTapping = var1;
        }

        public final long getDoubleTapDelay() {
            return doubleTapDelay;
        }

        public final void setDoubleTapDelay(long var1) {
            doubleTapDelay = var1;
        }

        private final CustomPlayerView rootView;

        public final PlayerDoubleTapListenerInterface getControls() {
            return controls;
        }

        public final void setControls(PlayerDoubleTapListenerInterface var1) {
            controls = var1;
        }

        private static final String TAG = ".DTGListener";
        private static boolean DEBUG = false;

        /**
         * Resets the timeout to keep in double tap mode.
         * <p>
         * Called once in [PlayerDoubleTapListenerInterface.onDoubleTapStarted]. Needs to be called
         * from outside if the double tap is customized / overridden to detect ongoing taps
         */
        public final void keepInDoubleTapMode() {
            isDoubleTapping = true;
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, doubleTapDelay);
        }

        /**
         * Cancels double tap mode instantly by calling [PlayerDoubleTapListenerInterface.onDoubleTapFinished]
         */
        public final void cancelInDoubleTapMode() {
            mHandler.removeCallbacks(mRunnable);
            isDoubleTapping = false;
            if (controls != null)
                controls.onDoubleTapFinished();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // Used to override the other methods
            if (isDoubleTapping) {
                if (controls != null)
                    controls.onDoubleTapProgressDown(e.getX(), e.getY());
                return true;
            }
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isDoubleTapping) {
                if (DEBUG)
                    Log.d(TAG, "onSingleTapUp: isDoubleTapping = true");
                if (controls != null)
                    controls.onDoubleTapProgressUp(e.getX(), e.getY());
                return true;
            }
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // Ignore this event if double tapping is still active
            // Return true needed because this method is also called if you tap e.g. three times
            // in a row, therefore the controller would appear since the original behavior is
            // to hide and show on single tap
            if (isDoubleTapping)
                return true;
            if (DEBUG)
                Log.d(TAG, "onSingleTapConfirmed: isDoubleTap = false");
            //return rootView.performClick()
            return rootView.handlingTap();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // First tap (ACTION_DOWN) of both taps
            if (DEBUG)
                Log.d(TAG, "onDoubleTap");
            if (!isDoubleTapping) {
                isDoubleTapping = true;
                keepInDoubleTapMode();
                if (controls != null)
                    controls.onDoubleTapStarted(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            // Second tap (ACTION_UP) of both taps
            if (e.getActionMasked() == MotionEvent.ACTION_UP && isDoubleTapping) {
                if (DEBUG)
                    Log.d(TAG, "onDoubleTapEvent, ACTION_UP");
                if (controls != null)
                    controls.onDoubleTapProgressUp(e.getX(), e.getY());
                return true;
            }
            return super.onDoubleTapEvent(e);
        }

        public DoubleTapGestureListener(CustomPlayerView rootView) {
            super();
            this.rootView = rootView;
            mHandler = new Handler();
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    if (DEBUG)
                        Log.d(TAG, "Runnable called");
                    setDoubleTapping(false);
                    DoubleTapGestureListener.this.setDoubleTapping(false);
                    if (getControls() != null)
                        getControls().onDoubleTapFinished();
                }
            };
            doubleTapDelay = 650L;
        }
    }
}
