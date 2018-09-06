package xlab.world.xlab.utils.support

import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils

class LetterOrDigitInputFilter: InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        var keepOriginal = true
        val stringBuilder = StringBuilder(end - start)
        (start until end).forEach { i ->
            val c = source?.elementAt(i)
            if (filtering(c!!)) // put your condition here
                stringBuilder.append(c)
            else
                keepOriginal = false
        }

        if (keepOriginal)
            return null
        else {
            if (source is Spanned) {
                val spannableString = SpannableString(stringBuilder)
                TextUtils.copySpansFrom(source, start, stringBuilder.length, null, spannableString, 0)
                return spannableString
            }
            return stringBuilder
        }
    }

    private fun filtering(c: Char): Boolean {
        return c.isLetterOrDigit()
    }
}