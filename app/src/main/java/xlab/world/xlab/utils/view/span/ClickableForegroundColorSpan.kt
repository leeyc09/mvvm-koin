package xlab.world.xlab.utils.view.span

import android.graphics.Typeface
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import xlab.world.xlab.utils.view.hashTag.HashTagHelper

class ClickableForegroundColorSpan(private val color: Int,
                                   private val typeFace: Typeface,
                                   private val onHashTagClickListener: HashTagHelper.ClickListener) : ClickableSpan() {

    override fun updateDrawState(ds: TextPaint?) {
        if (ds != null) {
            ds.color = color
            ds.typeface = typeFace
        }
    }

    override fun onClick(widget: View?) {
        if (widget != null) {
            val text = (widget as TextView).text
            val spanned = text as Spanned
            val start = spanned.getSpanStart(this)
            val end = spanned.getSpanEnd(this)

            // skip tag signal ex) '#' '@'
            onHashTagClickListener.onHashTagClicked(text.substring(start + 1, end))
        }
    }
}