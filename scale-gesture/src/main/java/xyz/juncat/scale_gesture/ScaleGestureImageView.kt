package xyz.juncat.scale_gesture

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ScaleGestureImageView : AppCompatImageView {

    private val drawableRectF = RectF()
    private var initScale = -1f
    var maxScale = MAX_SCALE
    var minScale = MIN_SCALE
    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (!isPointInImage(e1?.x ?: 0f, e1?.y ?: 0f)
                    && !isPointInImage(e2?.x ?: 0f, e2?.y ?: 0f)
                ) return false

                val scale = getScale()
                val rect = getDrawRectF()
                var dx = -distanceX / scale
                var dy = -distanceY / scale
                dx = if (rect.width() <= width) {
                    when {
                        rect.left + dx < 0 -> -rect.left
                        rect.right + dx > width -> width - rect.right
                        else -> dx
                    }
                } else {
                    when {
                        rect.left + dx > 0 -> -rect.left
                        rect.right + dx < width -> width - rect.right
                        else -> dx
                    }
                }

                dy = if (rect.height() <= height) {
                    when {
                        rect.top + dy < 0 -> -rect.top
                        rect.bottom + dy > height -> height - rect.bottom
                        else -> dy
                    }
                } else {
                    when {
                        rect.top + dy > 0 -> rect.top
                        rect.bottom + dy < height -> rect.bottom
                        else -> dy
                    }
                }
                imageMatrix.preTranslate(dx, dy)
                invalidate()
                return true
            }
        })

    private val pointArray = floatArrayOf(0f, 0f)
    private val invertMatrix = Matrix()
    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                if (detector == null) return false
                if (initScale == -1f) {
                    initScale = getScale()
                }
                val scale = detector.scaleFactor
                val fx = detector.focusX
                val fy = detector.focusY
                imageMatrix.invert(invertMatrix)
                pointArray[0] = fx
                pointArray[1] = fy
                invertMatrix.mapPoints(pointArray)

                val curScale = getScale()
                val theoryScale = curScale * scale
                val max = maxScale * initScale
                val min = minScale * initScale
                val realScale = if (scale > 1f && theoryScale > max) {
                    max / curScale
                } else if (scale < 1f && theoryScale < min) {
                    min / curScale
                } else {
                    scale
                }
                imageMatrix.preScale(realScale, realScale, pointArray[0], pointArray[1])
                invalidate()
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                if (detector == null) return false
                return isPointInImage(detector.focusX, detector.focusY)
            }
        })

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    private val values = FloatArray(9)
    fun getScale(): Float {
        imageMatrix.getValues(values)
        return values[Matrix.MSCALE_X]
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
        color = Color.RED
    }

    private fun getDrawRectF(): RectF {
        drawableRectF.set(drawable.bounds)
        imageMatrix.mapRect(drawableRectF)
        return drawableRectF
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRect(getDrawRectF(), borderPaint)
    }

    companion object {
        private const val TAG = "ScaleGestureImageView"
        val MAX_SCALE = 2.0f //最大缩放比例
        val MIN_SCALE = 0.5f // 最小缩放比例
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        resetInitScale()
    }

    private fun resetInitScale() {
        initScale = -1f
    }

    private fun isPointInImage(x: Float, y: Float): Boolean {
        return getDrawRectF().contains(x, y)
    }
}