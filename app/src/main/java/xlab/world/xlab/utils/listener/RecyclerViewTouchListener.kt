package xlab.world.xlab.utils.listener

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class RecyclerViewTouchListener(context: Context,
                                private val recyclerView: RecyclerView,
                                private val clickListener: Listener): RecyclerView.OnItemTouchListener {

    interface Listener {
        fun onClick(view: View, position: Int)
        fun onLongClick(view: View, position: Int)
    }

    private var gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent?) {
                e?.let { motionEvent ->
                    val child = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
                    child?.let { c ->
                        clickListener.onLongClick(c, recyclerView.getChildAdapterPosition(c))
                    }
                }
            }
        })
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val child = recyclerView.findChildViewUnder(e.x, e.y)
        child?.let { c ->
            if (gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(c, recyclerView.getChildAdapterPosition(c))
            }
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }
}