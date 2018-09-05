package xlab.world.xlab.utils.font

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import xlab.world.xlab.R
import xlab.world.xlab.utils.span.FontForegroundColorSpan

class FontColorSpan(context: Context) {

    val notoBold000000 = FontForegroundColorSpan(
            ResourcesCompat.getColor(context.resources, R.color.color000000, null),
            CustomFont.getTypeface(CustomFont.notoSansCJKkrBold, context)!!)

    val notoRegular000000 = FontForegroundColorSpan(
            ResourcesCompat.getColor(context.resources, R.color.color000000, null),
            CustomFont.getTypeface(CustomFont.notoSansCJKkrRegular, context)!!)
}