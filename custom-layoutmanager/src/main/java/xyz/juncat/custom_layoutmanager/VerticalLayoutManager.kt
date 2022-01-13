package xyz.juncat.custom_layoutmanager

import android.graphics.PointF
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import kotlin.math.abs

class VerticalLayoutManager : RecyclerView.LayoutManager(),
    RecyclerView.SmoothScroller.ScrollVectorProvider {
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler?, state: RecyclerView.State?): Int {
        Log.i(TAG, "scrollVerticallyBy: ")
        var consumed = dy
        recycler?.let {
            consumed = fill(it, dy > 0, dy)
            offsetChildrenVertical(-consumed)
            recyclerChildView(dy > 0, it)
        }
        return consumed
    }

    private fun recyclerChildView(fillEnd: Boolean, recycler: Recycler) {
        Log.i(TAG, "recyclerChildView: ")
        if (fillEnd) {
            for (i in 0 until childCount) {
                val view: View = getChildAt(i) ?: break
                val needRecycler = getDecoratedBottom(view) < paddingTop
                if (needRecycler) {
                    removeAndRecycleView(view, recycler)
                } else {
                    return
                }
            }
        } else {
            for (i in childCount - 1 downTo 0) {
                val view: View = getChildAt(i) ?: break
                val needRecycler = getDecoratedTop(view) > height - paddingBottom
                if (needRecycler) {
                    removeAndRecycleView(view, recycler)
                } else {
                    return
                }
            }
        }
    }

    private fun fill(recycler: RecyclerView.Recycler, fillEnd: Boolean, dy: Int): Int {
        Log.i(TAG, "fill: ")
        if (childCount == 0) return 0

        var fillPos = RecyclerView.NO_POSITION
        var availableSpace = abs(dy)
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0

        //calculate the starting fill position
        if (fillEnd) {
            val anchorView = getChildAt(childCount - 1) ?: return 0
            val anchorPosition = getPosition(anchorView)
            val anchorTop = getDecoratedBottom(anchorView)
            fillPos = anchorPosition + 1
            top = anchorTop
            //超出合理范围
            if (fillPos >= itemCount && anchorTop - availableSpace < height) {
                return anchorTop - height
            }
            if (anchorTop - availableSpace > height) {
                return dy
            }

        } else {
            val anchorView = getChildAt(0) ?: return 0
            val anchorPosition = getPosition(anchorView)
            val anchorBottom = getDecoratedTop(anchorView)

            bottom = anchorBottom
            fillPos = anchorPosition - 1
            if (fillPos < 0 && anchorBottom + availableSpace > 0) {
                return bottom
            }
            if (anchorBottom + availableSpace < 0) {
                return dy
            }
        }

        //fill
        while (availableSpace > 0 && (fillPos in 0 until itemCount)) {
            val itemView = recycler.getViewForPosition(fillPos)
            if (fillEnd) {
                addView(itemView)
            } else {
                addView(itemView, 0)
            }
            measureChildWithMargins(itemView, 0, 0)
            if (fillEnd) {
                bottom = top + getDecoratedMeasuredHeight(itemView)
            } else {
                top = bottom - getDecoratedMeasuredHeight(itemView)
            }
            right = left + getDecoratedMeasuredWidth(itemView)
            layoutDecoratedWithMargins(itemView, left, top, right, bottom)
            if (fillEnd) {
                top += getDecoratedMeasuredHeight(itemView)
                fillPos++
            } else {
                bottom -= getDecoratedMeasuredHeight(itemView)
                fillPos--
            }
            if (fillPos in 0 until itemCount) {
                availableSpace -= getDecoratedMeasuredHeight(itemView)
            }
        }
        return dy
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        Log.i(TAG, "onLayoutChildren: ")
        if (recycler == null || state == null) return
        if (itemCount <= 0) return

        var itemSpaceRemain = height - paddingTop - paddingBottom
        var itemTop = paddingTop
        var currentPos = 0
        var fixOffset = 0

        if (childCount != 0) {
            val firstChild = getChildAt(0) ?: return
            currentPos = getPosition(firstChild)
            fixOffset = getDecoratedTop(firstChild)
        }
        if (pendingScrollPosition != RecyclerView.NO_POSITION) {
            currentPos = pendingScrollPosition
        }

        detachAndScrapAttachedViews(recycler)

        var index = 0
        while (itemSpaceRemain > 0 && currentPos < state.itemCount) {
            val itemView = recycler.getViewForPosition(currentPos)
            addView(itemView)
            val marginHorizontal = index * 20
            measureChildWithMargins(itemView, marginHorizontal, 0)
            val itemLeft = (width - getDecoratedMeasuredWidth(itemView)) / 2
            val itemRight = itemLeft + getDecoratedMeasuredWidth(itemView)
            val itemBottom = itemTop + getDecoratedMeasuredHeight(itemView)
            layoutDecorated(itemView, itemLeft, itemTop, itemRight, itemBottom)
            itemSpaceRemain -= (itemBottom - itemTop)
            itemTop = itemBottom
            currentPos++
            index++
        }

        offsetChildrenVertical(fixOffset)
    }

    private var pendingScrollPosition = RecyclerView.NO_POSITION

    override fun scrollToPosition(position: Int) {
        if (position !in 0..itemCount) return
        pendingScrollPosition = position
        requestLayout()
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        if (recyclerView == null) return
        if (position !in 0..itemCount) return
        val linearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
        //call scrollVerticallyBy
    }

    companion object {
        private const val TAG = "CustomLayoutManager"
    }

    private val pointF = PointF()
    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (childCount == 0) return null
        val firstView = getChildAt(0) ?: return null
        val firstPos = getPosition(firstView)
        if (targetPosition < firstPos) {
            pointF.y = -1f
        } else {
            pointF.y = 1f
        }
        Log.i(TAG, "computeScrollVectorForPosition: ${pointF.y}")
        return pointF
    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        pendingScrollPosition = RecyclerView.NO_POSITION
    }
}