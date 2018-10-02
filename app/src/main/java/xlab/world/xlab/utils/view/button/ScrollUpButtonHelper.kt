package xlab.world.xlab.utils.view.button

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import xlab.world.xlab.R

class ScrollUpButtonHelper(private val smoothScroll: Boolean,
                           private val scrollUpBtn: ImageView): View.OnClickListener {

    private var recyclerView: RecyclerView? = null

    init {
        setupView()
    }

    fun handle(recyclerView: RecyclerView) {
        if (this.recyclerView == null) {
            this.recyclerView = recyclerView
            setupListener(this.recyclerView!!)
        } else {
            throw RuntimeException("recyclerView is not null. You need to create a unique recyclerView for every time")
        }
    }

    private fun setupView() {
        scrollUpBtn.visibility = View.GONE
        scrollUpBtn.setOnClickListener(this)
    }

    private var scrollState = -1
    private fun setupListener(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy < 0 && scrollState == 1) {
                    scrollUpBtn.visibility = View.VISIBLE
                    scrollState = -1
                } else if (dy > 0 && scrollState == -1) {
                    scrollUpBtn.visibility = View.GONE
                    scrollState = 1
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.scrollUpBtn -> { // 스크롤 업
                    if (smoothScroll)
                        recyclerView!!.smoothScrollToPosition(0)
                    else
                        recyclerView!!.scrollToPosition(0)
                    scrollUpBtn.visibility = View.GONE
                }
            }
        }
    }
}