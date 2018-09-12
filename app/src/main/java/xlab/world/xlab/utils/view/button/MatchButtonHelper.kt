package xlab.world.xlab.utils.view.button

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.*
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DialogCreator

class MatchButtonHelper (rootView: View,
                         private val context: Activity,
                         private var isMatchShow: Boolean,
                         private val listener: Listener): View.OnClickListener {

    private val spHelper: SPHelper by context.inject()
    private val loginDialog = DialogCreator.loginDialog(context = context)

    interface Listener {
        fun matchVisibility(visibility: Int)
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
                    if (spHelper.accessToken.isEmpty()) {
                        loginDialog.show()
                        return
                    }
                    RunActivity.topicSettingActivity(context = context)
                }
                R.id.matchBtn -> { // 매칭률 버튼
                    isMatchShow = !isMatchShow
                    matchButtonChange(isMatchShow)
                    listener.matchVisibility(
                            if (matchSettingBtn.isSelected) View.VISIBLE // show match percent
                            else View.GONE // hide match percent
                    )
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