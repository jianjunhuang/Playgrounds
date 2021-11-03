package xyz.juncat.refreshlayout

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.animation.addListener
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import com.jianjun.base.ext.dp
import com.jianjun.base.ext.invisible
import com.jianjun.base.ext.measureExactly
import com.jianjun.base.ext.visible

class RefreshLayout : FrameLayout, NestedScrollingParent3 {

    private val headerSize = 50f.dp.toInt()
    var isLoading = false
        private set
    private val header = ImageView(context).apply {
        invisible()
        scaleType = ImageView.ScaleType.FIT_XY
        setImageResource(R.drawable.ic_launcher_background)
        this@RefreshLayout.addView(this, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
    private var targetView: View? = null
    private val parentHelper = NestedScrollingParentHelper(this)
    private var showHeaderAnimator: ValueAnimator = ValueAnimator.ofFloat()
    private var dismissHeaderAnimator: ValueAnimator = ValueAnimator.ofFloat()
    var refreshCallback: OnRefreshCallback? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        header.measureExactly(headerSize, headerSize)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val headerLeft = (width - header.measuredWidth) / 2
        header.layout(headerLeft, 0, headerLeft + headerSize, headerSize)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewGroup.SCROLL_AXIS_VERTICAL && type == ViewCompat.TYPE_TOUCH
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
        showHeader(target, type)
        targetView = target
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        //下滑
        if (dyUnconsumed < 0 && !isStarted()) {
            if (!target.canScrollVertically(dyUnconsumed) && type == ViewCompat.TYPE_TOUCH) {
                target.translationY -= dyUnconsumed
                consumed[1] = dyUnconsumed
            }
        }

    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {

    }

    private fun isStarted() =
        isLoading || showHeaderAnimator.isRunning || dismissHeaderAnimator.isRunning

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {

        //上滑
        if (!isStarted() && dy > 0 && target.translationY > 0) {
            val scroll = if (dy + target.translationY < 0) {
                target.translationY
            } else {
                dy
            }
            target.translationY -= scroll.toFloat()
            consumed[1] = scroll.toInt()
        }

    }

    private fun showHeader(target: View, type: Int) {
        if (showHeaderAnimator.isRunning) {
            return
        }
        if (type == ViewCompat.TYPE_TOUCH) {
            if (target.translationY > header.height) {
                val initTransientY = target.translationY
                showHeaderAnimator.setFloatValues(0f, target.translationY - header.height)
                showHeaderAnimator.apply {
                    removeAllUpdateListeners()
                    removeAllListeners()
                    duration = 600
                    addListener(onStart = {
                        header.scaleX = 0f
                        header.scaleY = 0f
                        header.visible()
                    }, onEnd = {
                        isLoading = true
                        refreshCallback?.onRefreshTriggered()
                    })
                    addUpdateListener {
                        target.translationY = initTransientY - it.animatedValue as Float
                        val progress = it.animatedFraction
                        header.scaleX = progress
                        header.scaleY = progress
                    }
                }.start()
            } else {
                if (translationY != 0f) {
                    showHeaderAnimator.setFloatValues(target.translationY, 0f)
                    showHeaderAnimator.apply {
                        removeAllUpdateListeners()
                        removeAllListeners()
                        duration = 300
                        addUpdateListener {
                            target.translationY = it.animatedValue as Float
                        }
                    }.start()
                }
            }

        }
    }


    fun hideRefreshHeader() {
        targetView?.let { target ->
            if (dismissHeaderAnimator.isRunning) return
            dismissHeaderAnimator.setFloatValues(target.translationY, 0f)
            dismissHeaderAnimator.apply {
                removeAllUpdateListeners()
                removeAllListeners()
                duration = 300
                startDelay = 600
                addUpdateListener {
                    target.translationY = it.animatedValue as Float
                }
                addListener(onStart = {
                    header.invisible()
                }, onEnd = {
                    isLoading = false
                })
            }.start()
        }
    }

    companion object {
        private const val TAG = "RefreshLayout"
    }

    interface OnRefreshCallback {
        fun onRefreshTriggered()
    }
}