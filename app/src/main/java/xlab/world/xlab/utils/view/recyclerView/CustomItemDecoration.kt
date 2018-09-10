package xlab.world.xlab.utils.view.recyclerView

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View


class CustomItemDecoration : RecyclerView.ItemDecoration {

    private var left: Int = 0
    private var right: Int = 0
    private var top: Int = 0
    private var bottom: Int = 0

    constructor(context: Context, offset: Float = 0f) {
        val metrics = context.resources.displayMetrics
        val offSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, offset, metrics).toInt()
        this.left = offSetDIP
        this.right = offSetDIP
        this.top = offSetDIP
        this.bottom = offSetDIP
    }

    constructor(context: Context, left: Float = 0f, right: Float = 0f, top: Float = 0f, bottom: Float = 0f) {
        val metrics = context.resources.displayMetrics
        this.left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, left, metrics).toInt()
        this.right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, right, metrics).toInt()
        this.top = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, top, metrics).toInt()
        this.bottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottom, metrics).toInt()
    }

    override
    fun getItemOffsets(outRect: Rect, view: View, recyclerView: RecyclerView, state: RecyclerView.State?) {
        outRect.left = left
        outRect.top = top
        outRect.right = right
        outRect.bottom = bottom
    }
}
