package xlab.world.xlab.utils.view.dialog

import android.app.Activity
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.RequestCodeData
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.view.login.LoginActivity

object DialogCreator {
    fun loginDialog(context: Activity): DefaultDialog {
        return DefaultDialog(context = context,
                textBold = context.getString(R.string.dial_login_dialog),
                textRegular = context.getString(R.string.dial_login_dialog2),
                listener = object: DefaultDialog.Listener {
                    override fun onOkayTouch(tag: Any?) {
                        RunActivity.loginActivity(context = context, isComePreLoadActivity = false, linkData = null)
                    }
                })
    }

    fun suggestAddTopicDialog(context: Activity): DefaultDialog {
        return DefaultDialog(context = context,
                textBold = context.resources.getString(R.string.dial_add_topic),
                textRegular = context.resources.getString(R.string.dial_add_topic2),
                listener = object: DefaultDialog.Listener {
                    override fun onOkayTouch(tag: Any?) {
                        RunActivity.petEditActivity(context = context, petPage = 1, petId = null)
                    }
                })
    }
}