package xlab.world.xlab.utils.view.textView

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import xlab.world.xlab.utils.font.CustomFont

class TextViewRegularNoto: TextView {
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
        val customTypeface = CustomFont.getTypeface(CustomFont.notoSansCJKkrRegular, context)
        includeFontPadding = false
        typeface = customTypeface
    }
}