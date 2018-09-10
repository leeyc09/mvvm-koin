package xlab.world.xlab.utils.font

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import xlab.world.xlab.R
import xlab.world.xlab.utils.span.FontForegroundColorSpan

class FontColorSpan(context: Context) {
    // Noto Bold
    val notoBold000000 = FontForegroundColorSpan(
            color = ResourcesCompat.getColor(context.resources, R.color.color000000, null),
            typeFace =CustomFont.getTypeface(CustomFont.notoSansCJKkrBold, context)!!)
    val notoBoldBFBFBF = FontForegroundColorSpan(
            color = ResourcesCompat.getColor(context.resources, R.color.colorBFBFBF, null),
            typeFace = CustomFont.getTypeface(CustomFont.notoSansCJKkrBold, context)!!)

    // Noto Regular
    val notoRegular000000 = FontForegroundColorSpan(
            color = ResourcesCompat.getColor(context.resources, R.color.color000000, null),
            typeFace = CustomFont.getTypeface(CustomFont.notoSansCJKkrRegular, context)!!)

    // Noto Medium
    val notoMediumBFBFBF = FontForegroundColorSpan(
            color = ResourcesCompat.getColor(context.resources, R.color.colorBFBFBF, null),
            typeFace = CustomFont.getTypeface(CustomFont.notoSansCJKkrMedium, context)!!)
}