package xlab.world.xlab.utils.view.button

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.utils.view.dialog.DialogCreator

class MatchButtonHelper (rootView: View,
                         private val context: Activity,
                         private var isMatchShow: Boolean,
                         private val listener: Listener): View.OnClickListener {

    private val spHelper: SPHelper by context.inject()
    private val loginDialog = DialogCreator.loginDialog(context = context)

    private var withAnimation: Boolean = true

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
                    if (SupportData.isGuest(authorization = spHelper.authorization)) {
                        loginDialog.showDialog(tag = null)
                        return
                    }
                    RunActivity.topicSettingActivity(context = context)
                }
                R.id.matchBtn -> { // 매칭률 버튼
                    isMatchShow = !isMatchShow
                    matchButtonChange(isMatchShow)
                    listener.matchVisibility(
                            if (isMatchShow) View.VISIBLE // show match percent
                            else View.GONE // hide match percent
                    )
                }
            }
        }
    }

    private fun matchButtonChange(match: Boolean) {
        if (withAnimation) {
            // 버튼 애니메이션
            val settingAni =
                    if (match) AnimationUtils.loadAnimation(context, R.anim.match_setting_btn_show)
                    else AnimationUtils.loadAnimation(context, R.anim.match_setting_btn_hide)
            settingAni.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                    if (match)
                        matchSettingBtn.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(p0: Animation?) {
                    if (!match)
                        matchSettingBtn.visibility = View.GONE
                    withAnimation = false
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }
            })
            settingAni.fillAfter = true
            settingAni.isFillEnabled = true
            matchSettingBtn.startAnimation(settingAni)
        } else {
            matchSettingBtn.visibility = if (match) View.VISIBLE else View.GONE
        }
        val btnAni =
                if (match) AnimationUtils.loadAnimation(context, R.anim.match_btn_show)
                else AnimationUtils.loadAnimation(context, R.anim.match_btn_hide)
        btnAni.fillAfter = true
        btnAni.isFillEnabled = true
        matchBtn.startAnimation(btnAni)

        // 매칭율 버튼 상태에 따라서 로고 변경
        imageViewLogo.isSelected = match
    }
}