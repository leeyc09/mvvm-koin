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
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.font.CustomFont
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.span.FontForegroundColorSpan

class OnBoardingFragment: Fragment() {
    private val onBoardingViewModel: OnBoardingViewModel by viewModel()
    private val fontColorSpan: FontColorSpan by inject()

    private lateinit var textView: TextView

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
        onSetup(view)

        observeViewModel()
    }

    private fun onSetup(rootView: View) {
        // textView 바인드
        textView = rootView.findViewById(R.id.textView)

        onBoardingViewModel.contentTextSet(content = getContent(), index = getIndex(),
                boldFont = fontColorSpan.notoBold000000, regularFont = fontColorSpan.notoRegular000000)
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        onBoardingViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.contentStr?.let {
                    textView.setText(it, TextView.BufferType.SPANNABLE)
                }
            }
        })
    }

    private fun getIndex(): Int = arguments?.getInt("index") ?: 0
    private fun getContent(): String = arguments?.getString("content") ?: ""

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