package xlab.world.xlab.utils.view.dialog

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_two_select_bottom.*
import xlab.world.xlab.R

class TwoSelectBottomDialog: BottomSheetDialogFragment(), View.OnClickListener {

    interface Listener {
        fun onFirstBtnClick(tag: Any)
        fun onSecondBtnClick(tag: Any)
    }

    private var tag: Any = -1

    private lateinit var listener: Listener

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_two_select_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isCancelable = true

        onSetup()

        onBindEvent()
    }

    private fun onSetup() {
        if (getBundleFirstText() == null || getBundleFirstColor() == null) {
            firstBtn.visibility = View.GONE
        } else {
            firstBtn.visibility = View.VISIBLE
            firstBtn.setText(getBundleFirstText(), TextView.BufferType.SPANNABLE)
            firstBtn.setTextColor(getBundleFirstColor()!!)
        }

        if (getBundleSecondText() == null || getBundleSecondColor() == null) {
            secondBtn.visibility = View.GONE
        } else {
            secondBtn.visibility = View.VISIBLE
            secondBtn.setText(getBundleSecondText(), TextView.BufferType.SPANNABLE)
            secondBtn.setTextColor(getBundleSecondColor()!!)
        }
    }

    private fun onBindEvent() {
        firstBtn.setOnClickListener(this)
        secondBtn.setOnClickListener(this)
        cancelBtn.setOnClickListener(this) // 취소
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.firstBtn -> {
                    listener.onFirstBtnClick(tag)
                    dismiss()
                }
                R.id.secondBtn -> {
                    listener.onSecondBtnClick(tag)
                    dismiss()
                }
                R.id.cancelBtn -> { // 취소
                    dismiss()
                    tag = -1
                }
            }
        }
    }

    fun handle(listener: Listener) {
        this.listener = listener
    }

    fun setTag(tag: Any) {
        this.tag = tag
    }

    private fun getBundleFirstText(): String? = arguments?.getString("firstText")
    private fun getBundleFirstColor(): Int? = arguments?.getInt("firstColor")
    private fun getBundleSecondText(): String? = arguments?.getString("secondText")
    private fun getBundleSecondColor(): Int? = arguments?.getInt("secondColor")

    companion object {
        fun newDialog(firstText: String?, firstColor: Int?,
                      secondText: String?, secondColor: Int?): TwoSelectBottomDialog {
            val dialog = TwoSelectBottomDialog()

            val args = Bundle()
            firstText?.let { args.putString("firstText", it) }
            firstColor?.let { args.putInt("firstColor", it) }
            secondText?.let { args.putString("secondText", it) }
            secondColor?.let { args.putInt("secondColor", it) }
            dialog.arguments = args

            return dialog
        }
    }
}