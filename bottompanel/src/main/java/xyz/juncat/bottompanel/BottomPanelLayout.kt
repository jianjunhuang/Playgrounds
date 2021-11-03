package xyz.juncat.bottompanel

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.jianjun.base.ext.*
import com.jianjun.base.view.CustomViewGroup
import java.lang.IllegalArgumentException
import kotlin.math.abs

/**
 * always match_parent
 *
 * childCount <= 1, like ScrollView
 */
class BottomPanelLayout : CustomViewGroup {

    /**
     *
     * if true => Children will invisible and translate to bottom, show by method [showPanelWithAnimation]
     */
    var showWithAnimate = true

    /**
     * 用于控制是否绘制子 item
     */
    private var itemInvisible = showWithAnimate

    /**
     * panel height percentage
     */
    private var panelPercent = 0.6f
    private var panelPaddingTop = 30f.dp.toInt()
    private val dimColor = Color.parseColor("#99000000")
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFFFF")
        style = Paint.Style.FILL
        alpha = 51
    }

    /**
     * Panel's top ,change in [onLayout]
     */
    private var panelTop = 0

    /**
     * Record panel offset when [scrollPanelBackground] called
     */
    private var outerOffset = 0

    /**
     * Change when [scrollPanelBackground] called.
     * For touch control
     */
    private val curPanelTop: Int
        get() {
            return panelTop + outerOffset
        }

    private val panelDrawable by lazy(LazyThreadSafetyMode.NONE) {
        ContextCompat.getDrawable(context, R.drawable.bg_panel_layout)
    }
    private val indicatorBarRct = RectF(0f, 0f, 50f.dp, 6f.dp)

    /**
     * touch-scrolled area.
     * Is the same as [panelRct] in default.
     */
    private val touchRct = Rect()

    /**
     * for panelDrawable
     */
    private val panelRct = Rect()

    private var closeThreshold = 0
    var penalCloseCallback: (() -> Unit)? = null
    var dimClickedCallback: (() -> Unit)? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (childCount > 1) {
            throw IllegalArgumentException("childCount always <= 1, now childCount == $childCount")
        }
        if (itemInvisible)
            children.iterator().forEach {
                it.invisible()
            }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec.measureSize, heightMeasureSpec.measureSize)
        val h = measuredHeight - (measuredHeight - measuredHeight * panelPercent + panelPaddingTop)
        children.forEach {
            it.measure(widthMeasureSpec, h.toInt().toExactlySpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        panelTop = (height - height * panelPercent).toInt()
        val offsetY = panelTop + panelPaddingTop
        children.forEach {
            it.layout(0, offsetY, width, height)
        }
        if (!changed) return
        panelRct.set(0, panelTop, width, height)
        closeThreshold = panelRct.centerY()
        panelDrawable?.setBounds(panelRct)
        val indicatorLeft = (width - indicatorBarRct.width()) / 2f
        val indicatorTop = panelTop + 10f.dp
        indicatorBarRct.offsetTo(indicatorLeft, indicatorTop)
        touchRct.set(panelRct.left, panelRct.top, panelRct.right, height)
    }

    override fun onDraw(canvas: Canvas?) {
        drawDim(canvas)
        drawPanel(canvas)
        super.onDraw(canvas)
        drawIndicator(canvas)
    }

    private fun drawIndicator(canvas: Canvas?) {
        if (itemInvisible) return
        val radius = indicatorBarRct.height() / 2f
        canvas?.drawRoundRect(indicatorBarRct, radius, radius, backgroundPaint)
    }

    private fun drawPanel(canvas: Canvas?) {
        if (itemInvisible) return
        canvas?.let {
            panelDrawable?.bounds = panelRct
            panelDrawable?.draw(it)
        }
    }

    private fun drawDim(canvas: Canvas?) {
        canvas?.drawColor(dimColor)
    }

    private var isScrolling = false
    private val config = ViewConfiguration.get(context)
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null || animator.isRunning)
            return super.onInterceptTouchEvent(ev)
        val x = ev.x
        val y = ev.y
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastY = y
                isScrolling = false
                if (y >= touchRct.top && y <= touchRct.bottom) {
                    lastX = x
                    lastY = y
                    isTouched = true
                }
                //touch dim area
                if (y < touchRct.top) {
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                //don't interceptor when user click
                isScrolling = (abs(y - lastY) > config.scaledTouchSlop)
                lastY = y
            }
            MotionEvent.ACTION_UP -> {
                if (isScrolling) {
                    isScrolling = false
                    return true
                }
            }
        }
        return isScrolling
    }

    private var lastX = 0f
    private var lastY = 0f
    private var isTouched = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || animator.isRunning)
            return super.onTouchEvent(event)
        val x = event.x
        val y = event.y
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (y >= touchRct.top && y <= touchRct.bottom) {
                    lastX = x
                    lastY = y
                    isTouched = true
                    return true
                }
                if (y < touchRct.top) {
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isTouched && panelRct.bottom >= (height + outerOffset)) {
                    var dy = y - lastY
                    //limit scroll distance
                    if (panelRct.bottom + dy < (height + outerOffset)) {
                        dy += (height + outerOffset) - (panelRct.bottom + dy)
                    }
                    scrollPanel(dy)
                    lastX = x
                    lastY = y
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.actionMasked == MotionEvent.ACTION_UP && !isTouched && y <= panelRct.top) {
                    dimClickedCallback?.invoke()
                    return true
                }
                isTouched = false
                if (panelRct.top <= closeThreshold) {
                    //reset
                    startOffsetAnimation(
                        0f,
                        (curPanelTop - panelRct.top).toFloat(),
                        onUpdate = {
                            scrollPanel(it)
                        })
                } else {
                    //to the bottom
                    startCloseAnimate()
                }
            }
        }
        return super.onTouchEvent(event)
    }


    private val animator = ValueAnimator.ofFloat()
    private var lastAnimatorValue = 0f
    private fun startOffsetAnimation(
        start: Float,
        end: Float,
        onUpdate: ((offset: Float) -> Unit),
        onEnd: (() -> Unit)? = null
    ) {
        if (animator.isRunning) return
        lastAnimatorValue = start
        animator.removeAllUpdateListeners()
        animator.removeAllListeners()
        animator.setFloatValues(start, end)
        animator.duration = 300
        animator.addUpdateListener {
            val v = it.animatedValue as Float
            onUpdate.invoke(v - lastAnimatorValue)
            lastAnimatorValue = v
        }
        animator.addListener(onEnd = {
            onEnd?.invoke()
        })
        animator.start()
    }

    fun startCloseAnimate() {
        //to the bottom
        val end = height - panelRct.top.toFloat()
        startOffsetAnimation(0f, end, onUpdate = {
            scrollPanel(it)
        }) {
            penalCloseCallback?.invoke()
        }
    }

    fun scrollPanel(dy: Float) {
        scrollPanelBackgroundInternal(dy.toInt(), false)
        children.firstNotNullOf {
            it.translationY += (dy)
        }
    }

    /**
     * scroll panel background and indicatorBar ,not include children view.
     * You can shrink panel, and hide children header view
     */
    fun scrollPanelBackground(
        offsetY: Int, animate: Boolean, animateEnd: (() -> Unit)? = null
    ) {
        outerOffset += offsetY
        scrollPanelBackgroundInternal(offsetY, animate, animateEnd)
    }

    private fun scrollPanelBackgroundInternal(
        offsetY: Int,
        animate: Boolean,
        animateEnd: (() -> Unit)? = null
    ) {
        if (animate) {
            startOffsetAnimation(0f, offsetY.toFloat(), onUpdate = { offset ->
                panelRct.offset(0, offset.toInt())
                indicatorBarRct.offset(0f, offset)
                touchRct.offset(0, offset.toInt())
                invalidate()
            }, onEnd = {
                animateEnd?.invoke()
            })
        } else {
            panelRct.offset(0, offsetY)
            indicatorBarRct.offset(0f, offsetY.toFloat())
            touchRct.offset(0, offsetY)
            invalidate()
            animateEnd?.invoke()
        }
    }

    fun showPanelWithAnimation() {
        fun postAnimation() {
            postOnAnimation {
                //hide panel first
                val offsetY = panelTop.toFloat()
                scrollPanel(offsetY)
                //set item visible
                itemInvisible = false
                children.forEach {
                    it.visible()
                }
                postInvalidate()
                startOffsetAnimation(0f, -offsetY, onUpdate = {
                    scrollPanel(it)
                }, onEnd = {
//                    scrollPanel(panelTop - pan)
                })
            }

        }
        if (animator.isRunning) {
            animator.addListener(onEnd = {
                postAnimation()
            })
            return
        }
        postAnimation()
    }

    companion object {
        private const val TAG = "BottomPanelLayout"
    }
}