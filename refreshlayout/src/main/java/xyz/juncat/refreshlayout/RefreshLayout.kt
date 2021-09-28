package xyz.juncat.refreshlayout

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.animation.addListener
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.core.view.children
import com.jianjun.base.ext.invisible
import com.jianjun.base.ext.visible
import com.jianjun.base.view.CustomViewGroup

class RefreshLayout : FrameLayout, NestedScrollingParent3 {

    private val header = ImageView(context).apply {
        invisible()
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        setBackgroundResource(R.drawable.ic_launcher_background)
        setImageResource(R.drawable.ic_launcher_foreground)
        this@RefreshLayout.addView(this, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    private val parentHelper = NestedScrollingParentHelper(this)

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
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        header.layout(0, 0, width, header.measuredHeight)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewGroup.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
        if (type == ViewCompat.TYPE_TOUCH) {
            if (target.translationY > header.height) {
                val initTransientY = target.translationY
                ValueAnimator.ofFloat(0f, target.translationY - header.height)
                    .apply {
                        duration = 600
                        addListener(onStart = {
                            header.scaleX = 0f
                            header.scaleY = 0f
                            header.visible()
                        }, onEnd = {
                            ValueAnimator.ofFloat(target.translationY, 0f).apply {
                                duration = 300
                                startDelay = 600
                                addUpdateListener {
                                    target.translationY = it.animatedValue as Float
                                }
                                addListener(onStart = {
                                    header.invisible()
                                })
                            }.start()
                        })
                        addUpdateListener {
                            target.translationY = initTransientY -  it.animatedValue as Float
                            val progress = it.animatedFraction
                            header.scaleX = progress
                            header.scaleY = progress
                        }
                    }.start()
            } else {
                ValueAnimator.ofFloat(target.translationY, 0f).apply {
                    duration = 300
                    addUpdateListener {
                        target.translationY = it.animatedValue as Float
                    }
                }.start()
            }

        }
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
        Log.i(TAG, "onNestedScroll: ")
        //下滑
        if (dyUnconsumed < 0) {
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
        Log.i(TAG, "onNestedScroll: ")

    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        Log.i(TAG, "onNestedPreScroll: $dy")

        //上滑
        if (dy > 0 && target.translationY > 0) {
            val scroll = if (dy + target.translationY < 0) {
                target.translationY
            } else {
                dy
            }
            target.translationY -= scroll.toFloat()
            consumed[1] = scroll.toInt()
        }

    }

    companion object {
        private const val TAG = "RefreshLayout"
    }
}