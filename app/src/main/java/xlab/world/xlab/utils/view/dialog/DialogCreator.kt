package xlab.world.xlab.utils.view.dialog

import android.app.Activity
import android.support.v4.content.res.ResourcesCompat
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.RunActivity

object DialogCreator {
    fun loginDialog(context: Activity): DefaultDialog {
        return DefaultDialog(context = context,
                textBold = context.getString(R.string.dial_login),
                textRegular = context.getString(R.string.dial_login2),
                listener = object: DefaultDialog.Listener {
                    override fun onOkayTouch(tag: Any?) {
                        RunActivity.loginActivity(context = context, isComePreLoadActivity = false, linkData = null)
                    }
                })
    }

    fun suggestAddTopicDialog(context: Activity): DefaultDialog {
        return DefaultDialog(context = context,
                textBold = context.getString(R.string.dial_add_topic),
                textRegular = context.getString(R.string.dial_add_topic2),
                listener = object: DefaultDialog.Listener {
                    override fun onOkayTouch(tag: Any?) {
                        RunActivity.petEditActivity(context = context, petNo = null)
                    }
                })
    }

    fun editCancelDialog(context: Activity): DefaultDialog {
        return DefaultDialog(context = context,
                textBold = context.getString(R.string.dial_cancel),
                textRegular = context.getString(R.string.dial_cancel2),
                listener = object: DefaultDialog.Listener{
                    override fun onOkayTouch(tag: Any?) {
                        context.setResult(Activity.RESULT_CANCELED)
                        context.finish()
                    }
                })
    }

    fun ratingCancelDialog(context: Activity, listener: DefaultDialog.Listener): DefaultDialog {
        return DefaultDialog(context = context,
                textBold = context.getString(R.string.dial_rating_cancel),
                textRegular = context.getString(R.string.dial_rating_cancel2),
                listener = listener)
    }

    fun deletePetDialog(context: Activity, listener: DefaultOneDialog.Listener): DefaultOneDialog {
        return DefaultOneDialog(context = context,
                text = context.getString(R.string.dial_delete_topic),
                listener = listener)
    }

    fun orderCancelDialog(context: Activity, listener: DefaultOneDialog.Listener): DefaultOneDialog {
        return DefaultOneDialog(context = context,
                text = context.getString(R.string.dial_order_cancel),
                listener = listener)
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

    fun postUploadTypeSelectDialog(context: Activity): TwoSelectBottomDialog {
        val dialog = TwoSelectBottomDialog.newDialog(
                firstText = context.getString(R.string.picture),
                firstColor = ResourcesCompat.getColor(context.resources, R.color.color000000, null),
                secondText = context.getString(R.string.youtube_link),
                secondColor = ResourcesCompat.getColor(context.resources, R.color.color000000, null))

        dialog.handle(listener = object: TwoSelectBottomDialog.Listener {
            override fun onFirstBtnClick(tag: Any) { // image post upload
                RunActivity.postUploadPictureActivity(context = context, postId = "", youTubeVideoId = "")
            }

            override fun onSecondBtnClick(tag: Any) { // youtube link post upload
                EditTextDialog(context = context,
                        listener = object: EditTextDialog.EditTextDialogListener {
                            override fun onOkayTouch(text: String) {
                                RunActivity.postUploadPictureActivity(context = context, postId = "", youTubeVideoId = text)
                            }
                        }).show()
            }
        })

        return dialog
    }

    fun shareDialog(context: Activity, listener: ShareBottomDialog.Listener): ShareBottomDialog {
        val dialog = ShareBottomDialog.newDialog()
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

    fun buyGoodsOptionDialog(listener: BuyGoodsOptionDialog.Listener): BuyGoodsOptionDialog {
        val dialog = BuyGoodsOptionDialog.newDialog()
        dialog.handle(listener = listener)

        return dialog
    }
}