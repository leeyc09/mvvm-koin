package xlab.world.xlab.view.onBoarding.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.text.Spannable
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import xlab.world.xlab.R
import xlab.world.xlab.utils.font.CustomFont
import xlab.world.xlab.utils.span.FontForegroundColorSpan

class OnBoardingFragment: Fragment() {

    private lateinit var boldBlackFont: FontForegroundColorSpan
    private lateinit var regularBlackFont: FontForegroundColorSpan

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = when (getIndex()) {
            0 -> R.layout.fragment_onboarding_one
            1 -> R.layout.fragment_onboarding_two
            2 -> R.layout.fragment_onboarding_three
            else -> -1
        }

        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        boldBlackFont = FontForegroundColorSpan(
                ResourcesCompat.getColor(resources, R.color.color000000, null),
                CustomFont.getTypeface(CustomFont.notoSansCJKkrBold, context!!)!!)
        regularBlackFont = FontForegroundColorSpan(
                ResourcesCompat.getColor(resources, R.color.color000000, null),
                CustomFont.getTypeface(CustomFont.notoSansCJKkrRegular, context!!)!!)

        val textStr = SpannableString(getContent())
        when (getIndex()){
            0 -> {
                textStr.setSpan(boldBlackFont, 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                textStr.setSpan(regularBlackFont, 8, textStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            1 -> {
                textStr.setSpan(regularBlackFont, 0, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                textStr.setSpan(boldBlackFont, 12, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                textStr.setSpan(regularBlackFont, 30, textStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            2 -> {
                textStr.setSpan(boldBlackFont, 0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                textStr.setSpan(regularBlackFont, 10, textStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        view.findViewById<TextView>(R.id.textView).setText(textStr, TextView.BufferType.SPANNABLE)
    }

    private fun getIndex(): Int? = arguments?.getInt("index")
    private fun getContent(): String? = arguments?.getString("content")

    companion object {
        fun newFragment(content: String, index: Int): OnBoardingFragment {
            val fragment = OnBoardingFragment()

            val args = Bundle()
            args.putString("content", content)
            args.putInt("index", index)
            fragment.arguments = args

            return fragment
        }
    }
}