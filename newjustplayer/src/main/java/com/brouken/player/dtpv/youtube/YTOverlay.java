package com.brouken.player.dtpv.youtube;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.SeekParameters;

import com.brouken.player.PlayerActivity;
import com.brouken.player.R;
import com.brouken.player.dtpv.DoubleTapPV;
import com.brouken.player.dtpv.PlayerDoubleTapListenerInterface;
import com.brouken.player.dtpv.SeekListenerInterface;
import com.brouken.player.dtpv.youtube.views.CircClipTapView;
import com.brouken.player.dtpv.youtube.views.SecondView;

/**
 * Overlay for [DoubleTapPV] to create a similar UI/UX experience like the official
 * YouTube Android app.
 * <p>
 * The overlay has the typical YouTube scaling circle animation and provides some configurations
 * which can't be accomplished with the regular Android Ripple (I didn't find any options in the
 * documentation ...).
 */
public final class YTOverlay extends ConstraintLayout implements PlayerDoubleTapListenerInterface {

    private final AttributeSet attrs;

    public YTOverlay(@NonNull Context context) {
        this(context, null);
        // Hide overlay initially when added programmatically
        setVisibility(View.INVISIBLE);
    }

    public YTOverlay(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        playerViewRef = -1;

        LayoutInflater.from(context).inflate(R.layout.yt_overlay, this, true);

        // Initialize UI components
        initializeAttributes();
        ((SecondView) findViewById(R.id.seconds_view)).setForward(true);
        changeConstraints(true);

        // This code snippet is executed when the circle scale animation is finished
        ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).setPerformAtEnd(
                new Runnable() {
                    @Override
                    public void run() {
                        if (performListener != null)
                            performListener.onAnimationEnd();

                        SecondView secondView = findViewById(R.id.seconds_view);
                        secondView.setVisibility(View.INVISIBLE);
                        secondView.setSeconds(0);
                        secondView.stop();
                    }
                }
        );
    }

    private int playerViewRef;

    // Player behaviors
    private DoubleTapPV playerView;
    private ExoPlayer player;

    /**
     * Sets all optional XML attributes and defaults
     */
    private void initializeAttributes() {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs,
                    com.github.vkay94.dtpv.R.styleable.YouTubeOverlay, 0, 0);

            // PlayerView => see onAttachToWindow
            playerViewRef = a.getResourceId(com.github.vkay94.dtpv.R.styleable.YouTubeOverlay_yt_playerView, -1);

            // Durations
            setAnimationDuration((long) a.getInt(
                    com.github.vkay94.dtpv.R.styleable.YouTubeOverlay_yt_animationDuration, 650));

            seekSeconds = a.getInt(
                    com.github.vkay94.dtpv.R.styleable.YouTubeOverlay_yt_seekSeconds, 10);

            setIconAnimationDuration((long) a.getInt(
                    com.github.vkay94.dtpv.R.styleable.YouTubeOverlay_yt_iconAnimationDuration, 750));

            // Arc size
            setArcSize((float) a.getDimensionPixelSize(
                    com.github.vkay94.dtpv.R.styleable.YouTubeOverlay_yt_arcSize,
                    getContext().getResources().getDimensionPixelSize(com.github.vkay94.dtpv.R.dimen.dtpv_yt_arc_size))
            );

            // Colors
            setTapCircleColor(a.getColor(
                    com.github.vkay94.dtpv.R.styleable.YouTubeOverlay_yt_tapCircleColor,
                    ContextCompat.getColor(getContext(), com.github.vkay94.dtpv.R.color.dtpv_yt_tap_circle_color))
            );

            setCircleBackgroundColor(a.getColor(
                    com.github.vkay94.dtpv.R.styleable.YouTubeOverlay_yt_backgroundCircleColor,
                    ContextCompat.getColor(getContext(), com.github.vkay94.dtpv.R.color.dtpv_yt_background_circle_color))
            );

            // Seconds TextAppearance
            setTextAppearance(a.getResourceId(
                    com.github.vkay94.dtpv.R.styleable.YouTubeOverlay_yt_textAppearance,
                    com.github.vkay94.dtpv.R.style.YTOSecondsTextAppearance)
            );

            this.setIcon(a.getResourceId(
                    com.github.vkay94.dtpv.R.styleable.YouTubeOverlay_yt_icon,
                    com.github.vkay94.dtpv.R.drawable.play_triangle)
            );

            a.recycle();
        } else {
            // Set defaults
            setArcSize((float) getContext().getResources().getDimensionPixelSize(com.github.vkay94.dtpv.R.dimen.dtpv_yt_arc_size));
            setTapCircleColor(ContextCompat.getColor(getContext(), com.github.vkay94.dtpv.R.color.dtpv_yt_tap_circle_color));
            setCircleBackgroundColor(ContextCompat.getColor(getContext(), com.github.vkay94.dtpv.R.color.dtpv_yt_background_circle_color));
            setAnimationDuration(650L);
            setIconAnimationDuration(750L);
            seekSeconds = 10;
            setTextAppearance(com.github.vkay94.dtpv.R.style.YTOSecondsTextAppearance);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // If the PlayerView is set by XML then call the corresponding setter method
        if (playerViewRef != -1)
            playerView((DoubleTapPV) ((View) getParent()).findViewById(playerViewRef));
    }

    /**
     * Obligatory call if playerView is not set via XML!
     * <p>
     * Links the DoubleTapPV to this view for recognizing the tapped position.
     *
     * @param playerView PlayerView which triggers the event
     */
    public YTOverlay playerView(DoubleTapPV playerView) {
        this.playerView = playerView;
        return this;
    }

    /**
     * Obligatory call! Needs to be called whenever the Player changes.
     * <p>
     * Performs seekTo-calls on the ExoPlayer's Player instance.
     *
     * @param player PlayerView which triggers the event
     */
    public YTOverlay player(ExoPlayer player) {
        this.player = player;
        return this;
    }

        /*
        Properties
     */

    private SeekListenerInterface seekListenerInterface;

    /**
     * Optional: Sets a listener to observe whether double tap reached the start / end of the video
     */
    public YTOverlay seekListener(SeekListenerInterface listener) {
        seekListenerInterface = listener;
        return this;
    }

    private PerformListener performListener;

    /**
     * Sets a listener to execute some code before and after the animation
     * (for example UI changes (hide and show views etc.))
     */
    public YTOverlay performListener(PerformListener listener) {
        performListener = listener;
        return this;
    }

    /**
     * Forward / rewind duration on a tap in seconds.
     */
    private int seekSeconds;

    public final int getSeekSeconds() {
        return seekSeconds;
    }

    public YTOverlay seekSeconds(int seconds) {
        seekSeconds = seconds;
        return this;
    }

    /**
     * Color of the scaling circle on touch feedback.
     */
    public int getTapCircleColor() {
        return ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).getCircleColor();
    }

    private void setTapCircleColor(int value) {
        ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).setCircleColor(value);
    }

    public YTOverlay tapCircleColorRes(@ColorRes int resId) {
        setTapCircleColor(ContextCompat.getColor(getContext(), resId));
        return this;
    }

    public YTOverlay tapCircleColorInt(@ColorInt int color) {
        setTapCircleColor(color);
        return this;
    }

    /**
     * Color of the clipped background circle
     */
    public final int getCircleBackgroundColor() {
        return ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).getCircleBackgroundColor();
    }

    private final void setCircleBackgroundColor(int value) {
        ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).setCircleBackgroundColor(value);
    }

    public YTOverlay circleBackgroundColorRes(@ColorRes int resId) {
        setCircleBackgroundColor(ContextCompat.getColor(getContext(), resId));
        return this;
    }

    public YTOverlay circleBackgroundColorInt(@ColorInt int color) {
        setCircleBackgroundColor(color);
        return this;
    }

    /**
     * Duration of the circle scaling animation / speed in milliseconds.
     * The overlay keeps visible until the animation finishes.
     */
    private long animationDuration;

    public final long getAnimationDuration() {
        return ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).getAnimationDuration();
    }

    private void setAnimationDuration(long value) {
        ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).setAnimationDuration(value);
    }

    public YTOverlay animationDuration(long duration) {
        setAnimationDuration(duration);
        return this;
    }

    /**
     * Size of the arc which will be clipped from the background circle.
     * The greater the value the more roundish the shape becomes
     */
    private float arcSize;

    public final float getArcSize() {
        return ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).getArcSize();
    }

    private void setArcSize(float value) {
        ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).setArcSize(value);
    }

    public YTOverlay arcSize(@DimenRes int resId) {
        setArcSize(getContext().getResources().getDimension(resId));
        return this;
    }

    public YTOverlay arcSize(float px) {
        setArcSize(px);
        return this;
    }

    /**
     * Duration the icon animation (fade in + fade out) for a full cycle in milliseconds.
     */
    private long iconAnimationDuration = 750;

    public final long getIconAnimationDuration() {
        return ((SecondView) findViewById(R.id.seconds_view)).getCycleDuration();
    }

    private void setIconAnimationDuration(long value) {
        ((SecondView) findViewById(R.id.seconds_view)).setCycleDuration(value);
        iconAnimationDuration = value;
    }

    public YTOverlay iconAnimationDuration(long duration) {
        setIconAnimationDuration(duration);
        return this;
    }

    /**
     * One of the three forward icons which will be animated above the seconds indicator.
     * The rewind icon will be the 180° mirrored version.
     * <p>
     * Keep in mind that padding on the left and right of the drawable will be rendered which
     * could result in additional space between the three icons.
     */
    private int icon;

    public final int getIcon() {
        return ((SecondView) findViewById(R.id.seconds_view)).getIcon();
    }

    private void setIcon(int value) {
        ((SecondView) findViewById(R.id.seconds_view)).setIcon(value);
        this.icon = value;
    }

    public YTOverlay icon(@DrawableRes int resId) {
        setIcon(resId);
        return this;
    }

    /**
     * Text appearance of the *xx seconds* text.
     */
    private int textAppearance;

    public final int getTextAppearance() {
        return textAppearance;
    }

    private final void setTextAppearance(int value) {
        TextViewCompat.setTextAppearance(((SecondView) findViewById(R.id.seconds_view)).getTextView(), value);
        textAppearance = value;
    }

    public final YTOverlay textAppearance(@StyleRes int resId) {
        setTextAppearance(resId);
        return this;
    }

    /**
     * TextView view for *xx seconds*.
     * <p>
     * In case of you'd like to change some specific attributes of the TextView in runtime.
     */
    public final TextView getSecondsTextView() {
        return ((SecondView) findViewById(R.id.seconds_view)).getTextView();
    }

    @Override
    public void onDoubleTapStarted(float posX, float posY) {

        if (PlayerActivity.locked)
            return;

        if (player != null && player.getCurrentPosition() >= 0L && playerView != null && playerView.getWidth() > 0) {
            if (posX >= playerView.getWidth() * 0.35 && posX <= playerView.getWidth() * 0.65) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                    if (playerView.isControllerFullyVisible())
                        playerView.hideController();
                }
                return;
            }
        }

        //super.onDoubleTapStarted(posX, posY);
    }

    @Override
    public void onDoubleTapProgressUp(float posX, float posY) {

        if (PlayerActivity.locked)
            return;

        // Check first whether forwarding/rewinding is "valid"
        if (player == null || player.getMediaItemCount() < 1 || player.getCurrentPosition() < 0 || playerView == null || playerView.getWidth() < 0)
            return;

        long current = player.getCurrentPosition();
        // Rewind and start of the video (+ 0.5 sec tolerance)
        if (posX < playerView.getWidth() * 0.35 && current <= 500)
            return;

        // Forward and end of the video (- 0.5 sec tolerance)
        if (posX > playerView.getWidth() * 0.65 && current >= (player.getDuration() - 500))
            return;

        // YouTube behavior: show overlay on MOTION_UP
        // But check whether the first double tap is in invalid area
        if (getVisibility() != View.VISIBLE) {
            if (posX < playerView.getWidth() * 0.35 || posX > playerView.getWidth() * 0.65) {
                if (performListener != null)
                    performListener.onAnimationStart();
                SecondView secondView = findViewById(R.id.seconds_view);
                secondView.setVisibility(View.VISIBLE);
                secondView.start();
            } else
                return;
        }

        if (posX < playerView.getWidth() * 0.35) {

            // First time tap or switched
            SecondView secondView = findViewById(R.id.seconds_view);
            if (secondView.isForward()) {
                changeConstraints(false);
                secondView.setForward(false);
                secondView.setSeconds(0);
            }

            // Cancel ripple and start new without triggering overlay disappearance
            // (resetting instead of ending)
            ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).resetAnimation(new Runnable() {
                @Override
                public void run() {
                    ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).updatePosition(posX, posY);
                }
            });
            rewinding();
        } else if (posX > playerView.getWidth() * 0.65) {

            // First time tap or switched
            SecondView secondView = findViewById(R.id.seconds_view);
            if (!secondView.isForward()) {
                changeConstraints(true);
                secondView.setForward(true);
                secondView.setSeconds(0);
            }

            // Cancel ripple and start new without triggering overlay disappearance
            // (resetting instead of ending)
            ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).resetAnimation(new Runnable() {
                @Override
                public void run() {
                    ((CircClipTapView) findViewById(R.id.circle_clip_tap_view)).updatePosition(posX, posY);
                }
            });
            forwarding();
        } else {
            // Middle area tapped: do nothing
            //
            // playerView?.cancelInDoubleTapMode()
            // circle_clip_tap_view.endAnimation()
            // triangle_seconds_view.stop()
        }
    }

    /**
     * Seeks the video to desired position.
     * Calls interface functions when start reached ([SeekListenerInterface.onVideoStartReached])
     * or when end reached ([SeekListenerInterface.onVideoEndReached])
     *
     * @param newPosition desired position
     */
    private void seekToPosition(long newPosition) {
        if (player == null || playerView == null)
            return;

        player.setSeekParameters(SeekParameters.EXACT);

        // Start of the video reached
        if (newPosition <= 0) {
            player.seekTo(0);

            if (seekListenerInterface != null)
                seekListenerInterface.onVideoStartReached();
            return;
        }

        // End of the video reached
        long total = player.getDuration();
        if (newPosition >= total) {
            player.seekTo(total);

            if (seekListenerInterface != null)
                seekListenerInterface.onVideoEndReached();
            return;
        }

        // Otherwise
        playerView.keepInDoubleTapMode();
        player.seekTo(newPosition);
    }

    private void forwarding() {
        SecondView secondView = findViewById(R.id.seconds_view);
        secondView.setSeconds(secondView.getSeconds() + seekSeconds);
        seekToPosition(player != null ? player.getCurrentPosition() + (long) (this.seekSeconds * 1000) : null);
    }

    private void rewinding() {
        SecondView secondView = findViewById(R.id.seconds_view);
        secondView.setSeconds(secondView.getSeconds() + seekSeconds);
        seekToPosition(player != null ? player.getCurrentPosition() - (long) (this.seekSeconds * 1000) : null);
    }

    private void changeConstraints(boolean forward) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) findViewById(R.id.root_constraint_layout));
        SecondView secondView = findViewById(R.id.seconds_view);
        if (forward) {
            constraintSet.clear(secondView.getId(), ConstraintSet.START);
            constraintSet.connect(secondView.getId(), ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
        } else {
            constraintSet.clear(secondView.getId(), ConstraintSet.END);
            constraintSet.connect(secondView.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.START);
        }
        //secondView.start();
        constraintSet.applyTo((ConstraintLayout) findViewById(R.id.root_constraint_layout));
    }

    public interface PerformListener {
        void onAnimationStart();

        void onAnimationEnd();
    }
}
