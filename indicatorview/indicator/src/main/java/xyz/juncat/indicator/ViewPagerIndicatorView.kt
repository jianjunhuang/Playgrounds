package xyz.juncat.indicatorview

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2

/**
 * Selected rect background's size is equal to the item size.
 */
class ViewPagerIndicatorView<D, V : View> : ViewGroup {

    private val itemViews = ArrayList<V>()
    var selectedPos = 0
        private set
    private var viewPager: ViewPager2? = null
    private val selectedRectF = RectF()
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val selectedRadius = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 16f,
        Resources.getSystem().displayMetrics
    )

    var adapter: IndicatorAdapter<D, V>? = null
        set(value) {
            if (value != null) {
                field = value

                removeAllViews()
                itemViews.clear()

                for (index in 0 until value.getItemCount()) {
                    val itemView = value.createItemView(index)
                    addView(itemView)
                    itemViews.add(itemView)
                }

                requestLayout()
            }
        }

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
        itemViews.forEachIndexed { index, item ->
            measureChild(item, widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (itemViews.size < 0) return
        val centerPos = (width / itemViews.size)
        itemViews.forEachIndexed { index, item ->
            val cPos = centerPos / 2 + index * centerPos
            val itemLeft = cPos - item.measuredWidth / 2
            val itemTop = (height - item.measuredHeight) / 2
            item.layout(
                itemLeft,
                itemTop,
                itemLeft + item.measuredWidth,
                itemTop + item.measuredHeight
            )
        }
        if (itemViews.size > 0) {
            updateSelectedRectF(selectedPos)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawRoundRect(selectedRectF, selectedRadius, selectedRadius, selectedPaint)
        super.onDraw(canvas)
    }

    var isScrolling = false
        private set

    private var lastOffsetPixels = 0
    fun registerViewPager2(viewPager2: ViewPager2) {
        viewPager = viewPager2
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                when (state) {
                    ViewPager2.SCROLL_STATE_IDLE -> {
                        isScrolling = false
                        adapter?.onItemViewEndScroll(
                            selectedPos,
                            itemViews[selectedPos]
                        )
                    }
                    else -> {
                        if (!isScrolling) {
                            isScrolling = true
                            adapter?.onItemViewStartScroll(
                                selectedPos,
                                itemViews[selectedPos]
                            )
                        }
                    }
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                Log.i(
                    TAG,
                    "onPageScrolled: $position, $selectedPos, $positionOffset, $positionOffsetPixels"
                )

                movingSelectedRectF(position, positionOffset, positionOffsetPixels)

            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                adapter?.onItemViewSelected(position, itemViews[position], true)
                adapter?.onItemViewSelected(position, itemViews[selectedPos], false)
                updateSelectedRectF(position)
                selectedPos = position
            }
        })
    }

    private fun movingSelectedRectF(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int
    ) {
        val pageSize = itemViews.size
        var current = selectedPos
        var offset = positionOffset
        if (position % pageSize == pageSize - 1) {
            if (positionOffset < 0.5) {
                current = position
                offset = 0f
            } else {
                current = 0
                offset = 0f
            }
        } else {
            current = position
            offset = positionOffset
        }
        val next = (current + 1) % itemViews.size
        val rectLeft =
            itemViews[current].left + (itemViews[next].left - itemViews[current].left) * offset
        selectedRectF.offsetTo(rectLeft, selectedRectF.top)
        postInvalidate()
    }

    private fun updateSelectedRectF(pos: Int) {
        itemViews[pos].let {
            selectedRectF.set(
                it.left.toFloat(),
                it.top.toFloat(),
                it.right.toFloat(),
                it.bottom.toFloat()
            )
            postInvalidate()
        }
    }

    abstract class IndicatorAdapter<D, V : View> {
        abstract fun createItemView(pos: Int): V
        abstract fun onItemViewSelected(pos: Int, itemView: V, selected: Boolean)
        abstract fun onItemViewStartScroll(
            pos: Int,
            currentItem: V
        )

        abstract fun onItemViewEndScroll(
            pos: Int,
            currentItem: V
        )

        abstract fun onItemViewChanging(
            pos: Int,
            positionOffset: Float,
            currentItem: V
        )

        abstract fun getItemCount(): Int
        abstract fun getItem(pos: Int): D
        private fun registerIndicator() {

        }
    }

    companion object {
        private const val TAG = "ViewPagerIndicatorView"
    }
}