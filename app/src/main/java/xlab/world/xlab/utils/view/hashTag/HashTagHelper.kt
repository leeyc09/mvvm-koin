package xlab.world.xlab.utils.view.hashTag

import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.CharacterStyle
import android.widget.TextView
import xlab.world.xlab.utils.span.FontForegroundColorSpan
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.view.span.ClickableForegroundColorSpan

class HashTagHelper(private val hashTagCharsColor: HashMap<Char, Int>,
                    private val hashTagCharsFont: HashMap<Char, Typeface>,
                    private val onHashTagWritingListener: HashTagHelper.WritingListener? = null,
                    private val onHashTagClickListener: HashTagHelper.ClickListener? = null,
                    private val additionalHashTagChar: ArrayList<Char> = ArrayList()) {

    private val tag = "HashTag"

    interface WritingListener {
        fun onWritingHashTag(hashTagSign: Char, hashTag: String, start: Int, end: Int)
        fun onLastWritingHashTag(hashTagSign: Char)
        fun onEditTextState(state: Int)
    }

    interface ClickListener {
        fun onHashTagClicked(hashTag: String)
    }

    private var state: Int = stateNoTag
//    var userTags: ArrayList<String> = arrayListOf("xlab", "kakao", "facebook")

    private var textView: TextView? = null

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s.toString().isNotEmpty()) {
                // 텍스트
                try {
                    val position = textView!!.selectionStart - 1 // cursor position
                    if (position >= 0) {
                        val lastTypeChar = s.toString()[textView!!.selectionStart - 1] // get last input char
                        // if last input char is tag sign, state change to 'stateOnlyTag'
                        if (hashTagCharsColor[lastTypeChar] != null) { // letter start tag sign ex) '#', '@'
                            onHashTagWritingListener?.onLastWritingHashTag(lastTypeChar)
                            state = stateOnlyTag
                        }
                    }
                    // color set text view
                    clearSpannable(s!!)
                    setColorsToAllHashTags(s)
                } catch (e: IndexOutOfBoundsException) {
                    PrintLog.e("hash tag helper", e.message!!, tag)
                }
            }
            onHashTagWritingListener?.onEditTextState(state)
            // state reset to 'stateNoTag'
            state = stateNoTag
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    fun handle(tv: TextView) {
        if (textView == null) {
            textView = tv
            textView!!.addTextChangedListener(textWatcher)
            textView!!.setText(textView!!.text, TextView.BufferType.SPANNABLE) //set buffer type

            if (onHashTagClickListener != null) {
                textView!!.movementMethod = LinkMovementMethod.getInstance() // text click event
                textView!!.highlightColor = Color.TRANSPARENT // text highlight block
            }

//            setColorsToAllHashTags(textView!!.text)
        } else {
            throw RuntimeException("TextView is not null. You need to create a unique HashTagHelper for every TextView")
        }
    }

    private fun clearSpannable(text: CharSequence) {
        val spannable = textView!!.text as Spannable
        val spans = spannable.getSpans(0, text.length, CharacterStyle::class.java)
        spans.forEach { span ->
            if (span::class.java.name == ClickableForegroundColorSpan::class.java.name ||
                    span::class.java.name == FontForegroundColorSpan::class.java.name )
                spannable.removeSpan(span)
        }
    }

    private fun setColorsToAllHashTags(text: CharSequence) {
        var index = 0
        while (index < text.length) { // -1
            val sign = text[index]
            var nextIndex = index + 1
            if (hashTagCharsColor[sign] != null) { // letter start tag sign ex) '#', '@'
                nextIndex = findNextIndexHashTag(text, index)
                if (nextIndex > index + 1) { // tag with word
                    setColorHashTag(hashTagCharsColor[sign]!!, hashTagCharsFont[sign]!!, index, nextIndex)
                }
            }

            index = nextIndex
        }
    }

    private fun findNextIndexHashTag(text: CharSequence, startIndex: Int): Int {
        (startIndex + 1 until text.length).forEach { index ->
            val sign = text[index]
            val isValidSign = sign.isLetterOrDigit() || additionalHashTagChar.contains(sign) // find special letter or tag char
            if (!isValidSign)
                return index
        }

        return text.length
    }

    private fun setColorHashTag(color: Int,font: Typeface, startIndex: Int, nextIndex: Int) {
        val spannable = textView!!.text as Spannable
        val characterStyle: CharacterStyle =
                if(onHashTagClickListener != null) ClickableForegroundColorSpan(color, font, onHashTagClickListener)
                else FontForegroundColorSpan(color, font)

        val text = textView!!.text.toString()
        val sign = text.substring(startIndex, startIndex + 1)
        val hashTag = text.substring(startIndex + 1, nextIndex)
        // input text end of tag char
        if (textView!!.selectionEnd in (startIndex + 1..nextIndex)) {
                onHashTagWritingListener?.onWritingHashTag(sign.last(), hashTag, startIndex + 1, nextIndex)
                state = stateTagWithChar
        }

        if (sign.contains(AppConstants.USER_TAG_SIGN)) { // user tag color and font chance
//            if (userTags.contains(hashTag)) {
//                spannable.setSpan(characterStyle, startIndex, nextIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//            }
        } else if (sign.contains(AppConstants.HASH_TAG_SIGN)){ // hash tag color and fon change
            spannable.setSpan(characterStyle, startIndex, nextIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun getAllHashTags(): ArrayList<String> {
        val text = textView!!.text.toString()
        val spannable = textView!!.text as Spannable

        // use set to exclude duplicates
        val hashTags = ArrayList<String>()
        val userTags = ArrayList<String>()

        spannable.getSpans(0, text.length, CharacterStyle::class.java).forEach { span ->
            val sign = text.substring(spannable.getSpanStart(span), spannable.getSpanStart(span) + 1)
            val tagText = text.substring(spannable.getSpanStart(span) + 1, spannable.getSpanEnd(span))
            PrintLog.d("sign", sign, tag)
            PrintLog.d("tagText", tagText, tag)
            if (sign.contains(AppConstants.HASH_TAG_SIGN)) {
                hashTags.remove(tagText)
                hashTags.add(tagText)
            }
            else {
                userTags.remove(tagText)
                userTags.add(tagText)
            }
        }

        return hashTags
    }

    companion object {
        const val stateNoTag = 0
        const val stateOnlyTag = 1
        const val stateTagWithChar = 2
    }
}