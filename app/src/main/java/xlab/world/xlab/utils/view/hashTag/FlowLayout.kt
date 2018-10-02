package xlab.world.xlab.utils.view.hashTag

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * FlowLayout
 *
 * @link https://github.com/hongyangAndroid/FlowLayout
 * @license https://github.com/hongyangAndroid/FlowLayout/blob/master/LICENSE
 * @package flowlayout-lib/FlowLayout
 */
class FlowLayout: ViewGroup {

    private val mAllViews: ArrayList<ArrayList<View>> = ArrayList()
    private val mLineHeight: ArrayList<Int> = ArrayList()

    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs, 0)
    constructor(context: Context): super(context, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)

        var width = 0
        var height = 0
        var lineWidth = 0
        var lineHeight = 0

        (0 until childCount).forEach { i ->
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            val lp = child.layoutParams as MarginLayoutParams

            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
            val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

            if (lineWidth + childWidth > sizeWidth - paddingLeft - paddingRight) {
                width = Math.max(width, lineWidth)
                lineWidth = childWidth
                height += lineHeight
                lineHeight = childHeight
            } else {
                lineWidth += childWidth
                lineHeight = Math.max(lineHeight, childHeight)
            }

            if (i == childCount - 1) {
                width = Math.max(lineWidth, width)
                height += lineHeight
            }
        }

        val measureWidth =
                if (modeWidth == MeasureSpec.EXACTLY) sizeWidth
                else width + paddingLeft + paddingRight
        val measureHeight =
                if (modeHeight == MeasureSpec.EXACTLY) sizeHeight
                else height + paddingTop + paddingBottom

        setMeasuredDimension(measureWidth, measureHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mAllViews.clear()
        mLineHeight.clear()

        var lineWidth = 0
        var lineHeight = 0
        var lineViews: ArrayList<View> = ArrayList()

        (0 until childCount).forEach { i ->
            val child = getChildAt(i)
            val lp = child.layoutParams as MarginLayoutParams

            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            if (childWidth + lineWidth + lp.leftMargin + lp.rightMargin >
                    width - paddingLeft - paddingRight) {
                mLineHeight.add(lineHeight)
                mAllViews.add(lineViews)

                lineWidth = 0
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin
                lineViews = ArrayList()
            }

            lineWidth += childWidth + lp.leftMargin + lp.rightMargin
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin)
            lineViews.add(child)
        }

        mLineHeight.add(lineHeight)
        mAllViews.add(lineViews)

        var left = paddingLeft
        var top = paddingTop

        (0 until mAllViews.size).forEach { i ->
            lineViews = mAllViews[i]
            lineHeight = mLineHeight[i]

            (0 until lineViews.size).forEach { j ->
                val child = lineViews[j]
                if (child.visibility != View.GONE) {
                    val lp = child.layoutParams as MarginLayoutParams

                    val lc = left + lp.leftMargin
                    val tc = top + lp.topMargin
                    val rc = lc + child.measuredWidth
                    val bc = tc + child.measuredHeight

                    child.layout(lc, tc, rc, bc)

                    left += child.measuredWidth + lp.leftMargin + lp.rightMargin
                }
            }
            left = paddingLeft
            top += lineHeight
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return ViewGroup.MarginLayoutParams(context, attrs)
    }

}