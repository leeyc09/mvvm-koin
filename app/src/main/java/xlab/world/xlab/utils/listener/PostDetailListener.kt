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

class PostDetailListener(context: Activity,
                         private val fragmentManager: FragmentManager,
                         private val postMoreEvent: (Int?, Int?) -> Unit,
                         private val likePostEvent: (Int) -> Unit,
                         private val savePostEvent: (Int) -> Unit) {

    private val spHelper: SPHelper by context.inject()

    private val postDeleteDialog = DefaultDialog(
            context = context,
            textBold = context.getString(R.string.dial_delete_post),
            textRegular = context.getString(R.string.dial_delete_post2),
            listener = object: DefaultDialog.Listener {
                override fun onOkayTouch(tag: Any?) {
                    if (tag is Int) {
                        postMoreEvent(null, tag)
                    }
                }
            })

    private val postMoreDialog = DialogCreator.postMoreDialog(
            context = context,
            listener = object: TwoSelectBottomDialog.Listener {
                override fun onFirstBtnClick(tag: Any) {
                    if (tag is Int) {
                        postMoreEvent(tag, null)
                    }
                }

                override fun onSecondBtnClick(tag: Any) {
                    if (tag is Int) {
                        postDeleteDialog.setTag(tag)
                        postDeleteDialog.show()
                    }
                }
            })

    private val loginDialog = DialogCreator.loginDialog(context = context)


    // 포스트 ...
    val postMoreListener = View.OnClickListener { view ->
        if (view.tag is Int) {
            postMoreDialog.setTag(view.tag as Int)
            postMoreDialog.show(fragmentManager, "postMoreDialog")
        }
    }

    // 포스트 like
    val likePostListener = View.OnClickListener { view ->
        if (spHelper.accessToken.isEmpty()) {
            loginDialog.show()
        } else {
            if (view.tag is Int)
                likePostEvent(view.tag as Int)
        }
    }

    // 포스트 save
    val savePostListener = View.OnClickListener { view ->
        if (spHelper.accessToken.isEmpty()) {
            loginDialog.show()
        } else {
            if (view.tag is Int)
                savePostEvent(view.tag as Int)
        }
    }
}