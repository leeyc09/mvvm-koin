package xlab.world.xlab.utils.view.dialog

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_share_bottom.*
import xlab.world.xlab.R

class ShareBottomDialog: BottomSheetDialogFragment(), View.OnClickListener {

    interface Listener {
        fun onCopyLink(tag: Any?)
        fun onShareKakao(tag: Any?)
    }

    private var tag: Any? = null

    private var listener: Listener? = null

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_share_bottom, container, false)
    }

    override fun dismiss() {
        tag = null
        super.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isCancelable = true

        onBindEvent()
    }

    private fun onBindEvent() {
        copyLinkBtn.setOnClickListener(this)
        kakaoShareBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.copyLinkBtn -> {
                    listener?.onCopyLink(tag)
                    dismiss()
                }
                R.id.kakaoShareBtn -> {
                    listener?.onShareKakao(tag)
                    dismiss()
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

    companion object {
        fun newDialog(): ShareBottomDialog {
            return ShareBottomDialog()
        }
    }
}