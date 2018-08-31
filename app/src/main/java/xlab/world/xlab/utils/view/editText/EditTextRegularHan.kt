package xlab.world.xlab.utils.view.editText

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import xlab.world.xlab.utils.font.CustomFont

class EditTextRegularHan: AppCompatEditText {
    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val customTypeface = CustomFont.getTypeface(CustomFont.spoqaHanSansRegular, context)
        includeFontPadding = false
        typeface = customTypeface
    }
}