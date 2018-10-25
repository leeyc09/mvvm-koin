package xlab.world.xlab.utils.listener

import android.app.Activity
import android.support.v4.app.FragmentManager
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.TwoSelectBottomDialog

class UserDefaultListener(context: Activity,
                          private val followUserEvent: (Int) -> Unit) {

    private val spHelper: SPHelper by context.inject()

    private val loginDialog = DialogCreator.loginDialog(context = context)

    // 포스트 user follow
    val followListener = View.OnClickListener { view ->
        if (spHelper.accessToken.isEmpty()) {
            loginDialog.showDialog(tag = null)
        } else {
            if (view.tag is Int)
                followUserEvent(view.tag as Int)
        }
    }
}