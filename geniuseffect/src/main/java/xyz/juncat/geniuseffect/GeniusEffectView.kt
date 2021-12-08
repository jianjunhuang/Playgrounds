package xyz.juncat.geniuseffect

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class GeniusEffectView : View {

    private val bmp by lazy {
        val option = BitmapFactory.Options()
        option.inSampleSize = 2
        BitmapFactory.decodeResource(resources, R.drawable.unsplash, option)
    }
    private val dstBmpRect = RectF()
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 18f
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        dstBmpRect.set(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
        val offsetX = (width - bmp.width) / 2f
        dstBmpRect.offset(offsetX, 0f)
    }

    private val meshWidth = 10
    private val meshHeight = 10
    private val origin = FloatArray((meshWidth + 1) * (meshHeight + 1) * 2)

    //偏移后的点
    private val vert = FloatArray((meshWidth + 1) * (meshHeight + 1) * 2)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(bmp, null, dstBmpRect, null)
        initVerts(bmp, origin, meshWidth, meshHeight)
        initVerts(bmp, vert, meshWidth, meshHeight)
        canvas?.save()
        canvas?.translate(dstBmpRect.left, 0f)
        drawPoint(canvas, origin)
        canvas?.restore()

        canvas?.save()
        canvas?.translate(dstBmpRect.left, dstBmpRect.height() + 60)
        canvas?.drawBitmapMesh(bmp, meshWidth, meshHeight, vert, 0, null, 0, null)
        drawPoint(canvas, vert)
        canvas?.restore()
    }

    private fun offsetPointer(floatArray: FloatArray, pos: Int, offsetX: Float, offsetY: Float) {
        val xIndex = pos * 2
        floatArray[xIndex] += offsetX
        floatArray[xIndex + 1] += offsetY
    }

    private fun drawPoint(canvas: Canvas?, array: FloatArray) {
        canvas?.drawPoints(array, pointPaint)
    }

    private fun initVerts(b: Bitmap, floatArray: FloatArray, w: Int, h: Int) {
        val bW = b.width.toFloat()
        val bH = b.height.toFloat()
        val divW = bW / w
        val divH = bH / h
        var index = 0
        for (j in 0 until h + 1) {
            val y = j * divH
            for (i in 0 until w + 1) {
                val x = i * divW
                floatArray[index++] = x
                floatArray[index++] = y
            }
        }
    }
}