package xlab.world.xlab.utils.view.button

import android.content.Context
import android.view.View
import android.widget.*
import xlab.world.xlab.R

class MatchButtonHelper (rootView: View,
                         private var isMatchShow: Boolean,
                         private val listener: Listener): View.OnClickListener {

    interface Listener {
        fun onMatchSetting()
        fun isSelectedMatchBtn(isSelected: Boolean)
    }

    private lateinit var matchSettingBtn: FrameLayout
    private lateinit var matchBtn: FrameLayout
    private lateinit var imageViewLogo: ImageView

    init {
        setupView(rootView)
        setupListener()
    }

    private fun setupView(rootView: View) {
        matchSettingBtn = rootView.findViewById(R.id.matchSettingBtn)
        matchBtn = rootView.findViewById(R.id.matchBtn)
        imageViewLogo = rootView.findViewById(R.id.imageViewLogo)

        matchButtonChange(isMatchShow)
    }

    private fun setupListener() {
        matchSettingBtn.setOnClickListener(this) // 매칭률 설정 버튼
        matchBtn.setOnClickListener(this) // 매칭률 버튼
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.matchSettingBtn -> { // 매칭률 설정 버튼
                    listener.onMatchSetting()
                }
                R.id.matchBtn -> { // 매칭률 버튼
                    isMatchShow = !isMatchShow
                    matchButtonChange(isMatchShow)
                    listener.isSelectedMatchBtn(matchSettingBtn.isSelected)
                }
            }
        }
    }

    private fun matchButtonChange(match: Boolean) {
        matchSettingBtn.isSelected = match
        matchBtn.isSelected = match
        imageViewLogo.isSelected = match

        matchSettingBtn.visibility =
                if (match) View.VISIBLE
                else View.GONE
    }
}