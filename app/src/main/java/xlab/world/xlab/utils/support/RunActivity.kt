package xlab.world.xlab.utils.support

import android.app.Activity
import android.net.Uri
import xlab.world.xlab.R
import xlab.world.xlab.data.response.ResUserLoginData
import xlab.world.xlab.utils.view.dialog.DefaultDialog
import xlab.world.xlab.view.login.LoginActivity
import xlab.world.xlab.view.main.MainActivity
import xlab.world.xlab.view.onBoarding.OnBoardingActivity
import xlab.world.xlab.view.register.LocalRegisterActivity
import xlab.world.xlab.view.register.SocialRegisterActivity
import xlab.world.xlab.view.resetPassword.ResetPasswordActivity
import xlab.world.xlab.view.topicSetting.TopicSettingActivity
import xlab.world.xlab.view.webView.DefaultWebViewActivity

class RunActivity {
    fun onBoarding(context: Activity) {
        val intent = OnBoardingActivity.newIntent(context = context)
        context.startActivity(intent)
    }

    fun loginActivity(context: Activity, isComePreLoadActivity: Boolean, linkData: Uri?) {
        val intent = LoginActivity.newIntent(context = context, isComePreLoadActivity = isComePreLoadActivity, linkData = linkData)
        context.startActivityForResult(intent, RequestCodeData.LOGIN_USER)
    }

    fun localRegisterActivity(context: Activity) {
        val intent = LocalRegisterActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.REGISTER_USER)
    }

    fun socialRegisterActivity(context: Activity, userData: ResUserLoginData) {
        val intent = SocialRegisterActivity.newIntent(context = context, userData = userData)
        context.startActivityForResult(intent, RequestCodeData.REGISTER_USER)
    }

    fun defaultWebViewActivity(context: Activity, pageTitle: String?, webUrl: String, zoomControl: Boolean) {
        val intent = DefaultWebViewActivity.newIntent(context = context, pageTitle = pageTitle, webUrl = webUrl, zoomControl = zoomControl)
        context.startActivity(intent)
    }

    fun resetPasswordActivity(context: Activity, email: String) {
        val intent = ResetPasswordActivity.newIntent(context = context, email = email)
        context.startActivity(intent)
    }

    fun mainActivity(context: Activity, linkData: Uri?) {
        val intent = MainActivity.newIntent(context = context, linkData = linkData)
        context.startActivity(intent)
    }

    fun topicSettingActivity(context: Activity, isGuest: Boolean) {
        val loginDialog = createLoginDialog(context = context)
        if (isGuest) {
            loginDialog.show()
            return
        }

        val intent = TopicSettingActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.TOPIC_SETTING)
    }

    private fun createLoginDialog(context: Activity): DefaultDialog {
        return DefaultDialog(context = context,
                textBold = context.getString(R.string.login_dialog),
                textRegular = context.getString(R.string.login_dialog2),
                listener = object: DefaultDialog.Listener {
                    override fun onOkayTouch(tag: Any?) {
                        val intent = LoginActivity.newIntent(context = context, isComePreLoadActivity = false, linkData = null)
                        context.startActivityForResult(intent, RequestCodeData.LOGIN_USER)
                    }
                })
    }
}