package xlab.world.xlab.utils.view.popupWindow

import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import xlab.world.xlab.R

class GoodsSortPopupWindow(rootView: View, val listener: Listener): PopupWindow(rootView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT), View.OnClickListener {

    interface Listener {
        fun onChangeSort(sortType: Int)
    }

    init {
        onBindView(rootView)
        onBindEvent(rootView)
        isFocusable = true
    }

    private lateinit var currentSort: TextView

    private fun onBindView(rootView: View) {
        currentSort = rootView.findViewById(R.id.textViewCurrentSort)
    }

    private fun onBindEvent(rootView: View) {
        rootView.findViewById<LinearLayout>(R.id.currentSortLayout).setOnClickListener(this)
        rootView.findViewById<TextView>(R.id.sortMatchBtn).setOnClickListener(this)
        rootView.findViewById<TextView>(R.id.sortDownPriceBtn).setOnClickListener(this)
        rootView.findViewById<TextView>(R.id.sortUpPriceBtn).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.currentSortLayout -> {
                    dismiss()
                }
                R.id.sortMatchBtn,
                R.id.sortDownPriceBtn,
                R.id.sortUpPriceBtn -> {
                    listener.onChangeSort((v.tag as String).toInt())
                    currentSort.setText((v as TextView).text.toString(), TextView.BufferType.SPANNABLE)
                    dismiss()
                }
            }
        }
    }
}