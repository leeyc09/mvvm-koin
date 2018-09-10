package xlab.world.xlab.utils.view.tabLayout

import android.content.Context
import android.support.v4.view.ViewPager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import xlab.world.xlab.R
import xlab.world.xlab.utils.span.FontForegroundColorSpan

class TabLayoutHelper(private val context: Context,
                      private val defaultSelectFont: FontForegroundColorSpan,
                      private val defaultUnSelectFont: FontForegroundColorSpan,
                      private val listener: Listener?,
                      private val useDefaultEvent: Boolean) {

    private var frameLayout: FrameLayout? = null
    private lateinit var viewGroup: ViewGroup
    private val tabList: ArrayList<TabData> = ArrayList()
    private val defaultTabLayout: Int = R.layout.tab_layout_default
    private var viewPager: ViewPager? = null
    var currentTabIndex = -1

    interface Listener {
        fun onDoubleClick(index: Int)
    }

    fun handle(layout: FrameLayout, viewPager: ViewPager) {
        if (frameLayout == null) {
            frameLayout = layout
            this.viewPager = viewPager
            // setup view
            viewGroup = LinearLayout(context)
            viewGroup.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            (viewGroup as LinearLayout).orientation = LinearLayout.HORIZONTAL
            (viewGroup as LinearLayout).gravity = Gravity.BOTTOM
            frameLayout!!.addView(viewGroup)

            if (useDefaultEvent)
                bindEvent()
        }else {
            throw RuntimeException("HorizontalScrollView is not null. You need to create a unique TabLayoutHelper for every HorizontalScrollView")
        }
    }

    private fun bindEvent() {
        viewPager?.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
                changeSelectedTab(position)
            }
        })
    }

    fun addTab(tabName: String, tabLayout: Int?,
               selectFont: FontForegroundColorSpan?, unSelectFont: FontForegroundColorSpan?,
               extraData: Any?): Boolean {
        return createTab(tabName, tabLayout ?: defaultTabLayout,
                selectFont ?: defaultSelectFont, unSelectFont ?: defaultUnSelectFont, extraData)
    }

    private fun createTab(tabName: String, tabLayout: Int,
                          selectFont: FontForegroundColorSpan, unSelectFont: FontForegroundColorSpan,
                          extraData: Any?): Boolean {
        if (tabName.isEmpty()) {
            // do nothing, or you can tip "can't add empty tab"
            return false
        }

        val tabView = LayoutInflater.from(context).inflate(tabLayout, viewGroup, false)
        tabView.tag = tabList.size
        val textView = tabView.findViewById<TextView>(R.id.textViewTabName)
        textView.setText(tabName, TextView.BufferType.SPANNABLE)

        tabView.setOnClickListener(tabClickListener)
        viewGroup.addView(tabView)
        tabList.add(TabData(tabView, selectFont, unSelectFont, extraData))

        return true
    }

    private val tabClickListener = View.OnClickListener { view ->
        if (view.tag is Int) {
            val selectIndex = view.tag as Int
            if (currentTabIndex != selectIndex) {
                changeSelectedTab(selectIndex)
                viewPager?.setCurrentItem(selectIndex, true)
            } else {
                listener?.onDoubleClick(selectIndex)
            }
        }
    }

    fun changeSelectedTab(selectIndex: Int) {
        currentTabIndex = selectIndex
        for ((index, _) in tabList.withIndex()) {
            updateTabView(index)
        }
    }

    fun updateTabView(index: Int) {
        val tab = tabList[index]

        val textView = tab.view.findViewById<TextView>(R.id.textViewTabName)
        val spannable = SpannableString(textView.text)
        // clear span
        val spannableSpans = spannable.getSpans(0, textView.text.length, CharacterStyle::class.java)
        spannableSpans.forEach { span ->
            spannable.removeSpan(span)
        }
        val font =
                if (index == currentTabIndex) tab.selectTabFont
                else tab.unSelectTabFont
        spannable.setSpan(font, 0, textView.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.setText(spannable, TextView.BufferType.SPANNABLE)

        if (tab.extraData != null) {
            val textViewNumber = tab.view.findViewById<TextView>(R.id.textViewNum)
            if (textViewNumber != null) {
                if (tab.extraData is Int) {
                    textViewNumber.visibility = View.VISIBLE
                    textViewNumber.setText(tab.extraData.toString(), TextView.BufferType.SPANNABLE)
                } else {
                    textViewNumber.visibility = View.GONE
                }
            }
            val dotView = tab.view.findViewById<View>(R.id.dotView)
            if (dotView != null) {
                if (tab.extraData is Boolean) {
                    dotView.visibility =
                            if (tab.extraData as Boolean) View.VISIBLE
                            else View.INVISIBLE
                } else {
                    dotView.visibility = View.GONE
                }
            }
        }
    }

    fun getTabData(index: Int): TabData{
        return tabList[index]
    }

    inner class TabData(val view: View,
                        val selectTabFont: FontForegroundColorSpan,
                        val unSelectTabFont: FontForegroundColorSpan,
                        var extraData: Any?)
}