package xlab.world.xlab.utils.support

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

class ViewFunction {
    // recyclerView 더 스크롤 가능 여부 판단
    fun isScrolledRecyclerView(layoutManager: LinearLayoutManager, isLoading: Boolean, total: Int, isScrolled: (Boolean) -> Unit) {
        val visibleItemCount = layoutManager.childCount
        val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
        val listItemCount = layoutManager.itemCount

        if (visibleItemCount + pastVisibleItems >= listItemCount && !isLoading) {
            if (total < 0) // total 알수 없을 때, 스크롤 가능으로 판단
                isScrolled(true)
            else {
                if (total > listItemCount) // total 보다 data count 작을 경우, 스크롤 가능
                    isScrolled(true)
                else
                    isScrolled(false)
            }
        } else {
            isScrolled(false)
        }
    }

    // 키보드 숨기기
    fun hideKeyboard(context: Context, v: View) {
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
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
}