package xlab.world.xlab.utils.support

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_on_boarding.*

object ViewFunction {
    // 키보드 숨기기
    fun hideKeyboard(context: Context, view: View) {
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // 키보드 보이기
    fun showKeyboard(context: Context) {
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    // 키보드 visible 상태 판단
    fun showUpKeyboardLayout(view: View, visibility: (Int) -> Unit) {
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            view.getWindowVisibleDisplayFrame(r)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - r.bottom

            if (keypadHeight > screenHeight * 0.15) {
                visibility(View.VISIBLE)
            } else {
                visibility(View.INVISIBLE)
            }
        }
    }

    // view visibility 변경 판단
    fun visibilityChange(view: View, visibility: (Int) -> Unit) {
        view.viewTreeObserver.addOnGlobalLayoutListener {
            visibility(view.visibility)
        }
    }

    // view focus 변경 판단
    fun onFocusChange(editText: EditText, hasFocus: (Boolean) -> Unit) {
        editText.setOnFocusChangeListener {_, focus ->
            hasFocus(focus)
        }
    }

    // edit text 내용 변경 판단
    fun onTextChange(editText: EditText, text: (String) -> Unit) {
        editText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                text(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    // 키보드 버튼 touch 판단
    fun onKeyboardActionTouch(editText: EditText, putActionID: Int, isTouch: (Boolean) -> Unit) {
        editText.setOnEditorActionListener { _, actionID, _ ->
            //If the key event is a key-down event on the "putKeyCode" button
            if (actionID == putActionID) {
                isTouch(true)
            } else {
                isTouch(false)
            }
            false
        }
    }

    // edit text 이미지 touch 판단
    fun onDrawableTouch(editText: EditText, event: MotionEvent, isTouch: (Boolean) -> Unit) {
        if (editText.hasFocus()) { // 해당 edit text에 focus가 있는 경우
            if (event.action == MotionEvent.ACTION_UP) {
                // 이미지가 오른쪽에 있는 경우
                if (editText.compoundDrawables[2] != null) {
                    if (event.rawX >= editText.right - editText.compoundDrawables[2].bounds.width()) {
                        isTouch(true)
                        return
                    }
                }
            }
        }
        isTouch(false)
    }

    // view pager page 변경 판단
    fun onViewPagerChangePosition(viewPager: ViewPager, position: (Int) -> Unit) {
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(index: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(index: Int) {
                position(index)
            }
        })
    }

    // recycler view 스크롤 판단
    fun onRecyclerViewScrolledDown(recyclerView: RecyclerView, layoutManager: (RecyclerView.LayoutManager) -> Unit) {
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                recyclerView.layoutManager?.let {
                    if (dx > 0 || dy > 0) {
                        layoutManager(it)
                    }
                }
            }
        })
    }

    // he function tell true or false recycler view scrolled bottom
    fun isScrolledRecyclerView(layoutManager: LinearLayoutManager, isLoading: Boolean, total: Int, isScrolled: (Boolean) -> Unit) {
        val visibleItemCount = layoutManager.childCount
        val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
        val listItemCount = layoutManager.itemCount

        if (visibleItemCount + pastVisibleItems >= listItemCount && !isLoading) {
            if (total < 0 || total > listItemCount)// total 알수 없을 때 or total 보다 data count 작을 경우 스크롤 가능으로 판단
                isScrolled(true)
        }
    }

    // nested scroll view 스크롤 판단
    fun isNestedScrollViewScrolledDown(nestedScrollView: NestedScrollView, isScrolled: (Boolean) -> Unit) {
        nestedScrollView.setOnScrollChangeListener { v: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            v?.let { _ ->
                v.getChildAt(v.childCount - 1).let { childView ->
                    if ((scrollY >= childView.measuredHeight - v.measuredHeight) && scrollY > oldScrollY) {
                        isScrolled(true)
                    }
                }
            }
        }
    }

    // dot indicator 세팅
    fun setDotIndicator(tabLayoutDot: TabLayout, marginDIP: Int) {
        for (i in 0 until tabLayoutDot.tabCount) {
            val tab: View = (tabLayoutDot.getChildAt(0) as ViewGroup).getChildAt(i)
            val params = tab.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(marginDIP, 0, marginDIP, 0)
            tab.requestLayout()
        }
    }

    // birth format watcher (yyyy-mm-dd)
    fun birthWatcher(editText: EditText, result: (String) -> Unit) {
        editText.addTextChangedListener(object: TextWatcher {
            var preLength = 0
            var preBirth = ""
            var cursorPosition = 0
            override fun afterTextChanged(s: Editable?) {
                val length = s!!.toString().length
                val originBirth = s.toString().replace("-", "")

                if (!originBirth.isEmpty()) {
                    try {
                        originBirth.toInt()
                    } catch (e: NumberFormatException) {
                        editText.setText(preBirth)
                        editText.setSelection(cursorPosition-1)
                        return
                    }
                }

                var newBirth = ""
                for ((index, char) in originBirth.withIndex()) {
                    newBirth +=
                            if (index == 4 || index == 6) "-$char"
                            else char
                }

                if (s.toString() != newBirth) {
                    cursorPosition = editText.selectionEnd
                    editText.setText(newBirth)
                    if (length == cursorPosition) {
                        editText.setSelection(newBirth.length)
                    }
                    else {
                        if (preLength > length)
                            cursorPosition--
                        editText.setSelection(cursorPosition)
                    }
                }
                result(newBirth)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                preLength = s!!.toString().length
                preBirth = s.toString()
                cursorPosition = editText.selectionEnd
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    // pet weight watcher
//    fun weightWatcher(editText: EditText, result: (Float) -> Unit) {
//        editText.addTextChangedListener(object: TextWatcher {
//            var preWeight = ""
//            var cursorPosition = 0
//            override fun afterTextChanged(s: Editable?) {
//                if (!s!!.toString().isEmpty()) {
//                    try {
//                        val weightFloat = s.toString().toFloat()
//
//                        if (preWeight != s.toString()) {
//                            val weightStr = s.toString().split(".")
//                            val weightInteger: String?
//                            val weightFloating: String?
//                            if (!weightStr.isEmpty()) {
//                                weightInteger = weightStr[0]
//                                if (weightInteger.length > 3) {
//                                    editText.setText(preWeight)
//                                    editText.setSelection(cursorPosition-1)
//                                    return
//                                }
//                                if (weightStr.size > 1) {
//                                    weightFloating = weightStr[1]
//                                    if (weightFloating.length > 1) {
//                                        editText.setText(preWeight)
//                                        editText.setSelection(cursorPosition-1)
//                                        return
//                                    }
//                                }
//                            }
//                            result(weightFloat)
//                        }
//                    } catch (e: NumberFormatException) {
//                        editText.setText(preWeight)
//                        editText.setSelection(cursorPosition-1)
//                    }
//                } else {
//                    result(-1f)
//                }
//            }
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                preWeight = s!!.toString()
//                cursorPosition = editText.selectionEnd
//            }
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//        })
//    }
}