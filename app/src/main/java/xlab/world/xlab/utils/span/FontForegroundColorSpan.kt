package xlab.world.xlab.utils.span

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.ForegroundColorSpan

class FontForegroundColorSpan(private val color: Int, private val typeFace: Typeface): ForegroundColorSpan(color) {
    override fun updateDrawState(ds: TextPaint?) {
        if (ds != null) {
            ds.color = color
            ds.typeface = typeFace
        }
    }

}