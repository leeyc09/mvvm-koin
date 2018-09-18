package xlab.world.xlab.utils.view.dialog

import android.app.Activity
import android.support.v4.content.res.ResourcesCompat
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.PrintLog
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
                        RunActivity.petEditActivity(context = context, petNo = null)
                    }
                })
    }

    fun editCancelDialog(context: Activity): DefaultDialog {
        return DefaultDialog(context = context,
                textBold = context.resources.getString(R.string.dial_cancel),
                textRegular = context.resources.getString(R.string.dial_cancel2),
                listener = object: DefaultDialog.Listener{
                    override fun onOkayTouch(tag: Any?) {
                        context.setResult(Activity.RESULT_CANCELED)
                        context.finish()
                    }
                })
    }

    fun postMoreDialog(context: Activity, listener: TwoSelectBottomDialog.Listener): TwoSelectBottomDialog {
        val dialog = TwoSelectBottomDialog.newDialog(
                firstText = context.getString(R.string.edit),
                firstColor = ResourcesCompat.getColor(context.resources, R.color.color000000, null),
                secondText = context.getString(R.string.delete),
                secondColor = ResourcesCompat.getColor(context.resources, R.color.colorDE5359, null))

        dialog.handle(listener = listener)

        return dialog
    }

    fun commentDeleteDialog(context: Activity, listener: TwoSelectBottomDialog.Listener): TwoSelectBottomDialog {
        val dialog = TwoSelectBottomDialog.newDialog(
                firstText = context.getString(R.string.delete),
                firstColor = ResourcesCompat.getColor(context.resources, R.color.colorDE5359, null),
                secondText = null, secondColor = null)

        dialog.handle(listener = listener)

        return dialog
    }

    fun genderSelectDialog(listener: GenderSelectDialog.Listener): GenderSelectDialog {
        val dialog = GenderSelectDialog.newDialog()
        dialog.handle(listener = listener)

        return dialog
    }

    fun topicColorSelectDialog(selectPosition: Int, listener: TopicColorSelectDialog.Listener): TopicColorSelectDialog {
        val dialog = TopicColorSelectDialog.newDialog(selectPosition = selectPosition)
        dialog.handle(listener = listener)

        return dialog
    }
}