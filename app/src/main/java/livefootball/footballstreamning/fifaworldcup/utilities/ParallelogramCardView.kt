package livefootball.footballstreamning.fifaworldcup.utilities


import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import livefootball.footballstreamning.fifaworldcup.R
import kotlin.math.hypot
import kotlin.math.min

/**
 * A card container that clips its content to a TRUE parallelogram silhouette
 * (both left and right edges slanted, parallel to each other), with rounded
 * corners, an optional stroke outline, and a shadow that follows the same shape.
 *
 * Why this exists instead of MaterialCardView + ShapeAppearanceModel:
 * Android's built-in corner-family shaping (cut / rounded) only supports
 * per-corner cuts, not a full skewed quadrilateral outline, and it isn't
 * consistent pre-API 33 for clipping. This view clips manually via
 * Canvas.clipPath() in dispatchDraw(), which works on all API levels,
 * so the banner image and gradient overlay inside are clipped to the
 * parallelogram shape without distorting the image content itself
 * (the photo is CUT to the shape, not skewed/stretched).
 *
 * Usage in XML:
 * <com.bicodes.footballarena.widgets.ParallelogramCardView
 *     android:layout_width="280dp"
 *     android:layout_height="160dp"
 *     app:skewAmount="18dp"
 *     app:cardCornerRadius="14dp"
 *     app:cardStrokeColor="#4DE0FAFF"
 *     app:cardStrokeWidth="1.5dp"
 *     app:cardElevation="2dp">
 *     <!-- banner ImageView, gradient overlay View, ribbon, title, etc. -->
 * </com.bicodes.footballarena.widgets.ParallelogramCardView>
 */
class ParallelogramCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    // Defaults, overridable via XML attrs
    private var skewPx = 18f * density
    private var cornerRadiusPx = 14f * density
    private var strokeColor = 0x4DE0FAFF
    private var strokeWidthPx = 1.5f * density

    private val clipPath = Path()
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    init {
        // Manual clipping is done in dispatchDraw, so we don't rely on
        // the platform's clip-to-outline for content — only for the shadow.
        setWillNotDraw(false)
        clipToOutline = false

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.ParallelogramCardView)
            skewPx = ta.getDimension(R.styleable.ParallelogramCardView_skewAmount, skewPx)
            cornerRadiusPx = ta.getDimension(R.styleable.ParallelogramCardView_cardCornerRadius, cornerRadiusPx)
            strokeColor = ta.getColor(R.styleable.ParallelogramCardView_cardStrokeColor, strokeColor)
            strokeWidthPx = ta.getDimension(R.styleable.ParallelogramCardView_cardStrokeWidth, strokeWidthPx)
            elevation = ta.getDimension(R.styleable.ParallelogramCardView_cardElevation, elevation)
            ta.recycle()
        }

        strokePaint.color = strokeColor
        strokePaint.strokeWidth = strokeWidthPx

        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                // Parallelogram is convex, so setConvexPath is valid on all
                // API 21+ devices and gives a shadow that follows the shape.
                if (clipPath.isEmpty.not()) {
                    outline.setConvexPath(clipPath)
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildPath(w.toFloat(), h.toFloat())
        invalidateOutline()
    }

    private fun rebuildPath(w: Float, h: Float) {
        // Vertices for a parallelogram where BOTH left and right edges are
        // slanted by the same amount, keeping them parallel:
        //   top-left     -> (skew, 0)
        //   top-right    -> (w, 0)
        //   bottom-right -> (w - skew, h)
        //   bottom-left  -> (0, h)
        val vertices = listOf(
            PointF(skewPx, 0f),
            PointF(w, 0f),
            PointF(w - skewPx, h),
            PointF(0f, h)
        )
        clipPath.reset()
        buildRoundedPolygon(clipPath, vertices, cornerRadiusPx)
    }

    /**
     * Builds a Path that traces the given polygon with each corner replaced
     * by a quadratic-bezier rounded corner, clamped so radius never exceeds
     * half the length of the shorter adjoining edge (prevents overlap on
     * small views).
     */
    private fun buildRoundedPolygon(path: Path, points: List<PointF>, radius: Float) {
        val n = points.size
        for (i in 0 until n) {
            val curr = points[i]
            val prev = points[(i - 1 + n) % n]
            val next = points[(i + 1) % n]

            val prevLen = hypot((curr.x - prev.x).toDouble(), (curr.y - prev.y).toDouble()).toFloat()
            val nextLen = hypot((next.x - curr.x).toDouble(), (next.y - curr.y).toDouble()).toFloat()

            val rStart = min(radius, prevLen / 2f)
            val rEnd = min(radius, nextLen / 2f)

            val startPoint = pointTowards(curr, prev, rStart)
            val endPoint = pointTowards(curr, next, rEnd)

            if (i == 0) path.moveTo(startPoint.x, startPoint.y) else path.lineTo(startPoint.x, startPoint.y)
            path.quadTo(curr.x, curr.y, endPoint.x, endPoint.y)
        }
        path.close()
    }

    /** Returns the point that is `distance` away from [from], moving toward [towards]. */
    private fun pointTowards(from: PointF, towards: PointF, distance: Float): PointF {
        val dx = towards.x - from.x
        val dy = towards.y - from.y
        val len = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        if (len == 0f) return PointF(from.x, from.y)
        return PointF(from.x + dx / len * distance, from.y + dy / len * distance)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        canvas.clipPath(clipPath)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)

        // Stroke drawn AFTER restoring the clip, on top of everything,
        // so the outline reads crisply along the parallelogram edge.
        if (strokeWidthPx > 0f) {
            canvas.drawPath(clipPath, strokePaint)
        }
    }
}