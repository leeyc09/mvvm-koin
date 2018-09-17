package xlab.world.xlab.utils.view.dialog

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_select_gender.*
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.UserInfo

class GenderSelectDialog: BottomSheetDialogFragment(), View.OnClickListener {

    interface Listener {
        fun onGenderSelect(genderNum: Int, genderStr: String?)
    }

    private lateinit var listener: Listener

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_select_gender, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isCancelable = true

        onBindEvent()
    }

    private fun onBindEvent() {
        maleBtn.setOnClickListener(this) // 남자
        femleBtn.setOnClickListener(this) // 여자
        etcBtn.setOnClickListener(this) // 기타
        cancelBtn.setOnClickListener(this) // 취소
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.maleBtn -> { // 남자
                    val genderNum = (v.tag as String).toInt()
                    listener.onGenderSelect(genderNum = genderNum, genderStr = UserInfo.genderMap[genderNum])
                    dismiss()
                }
                R.id.femleBtn -> { // 여자
                    val genderNum = (v.tag as String).toInt()
                    listener.onGenderSelect(genderNum = genderNum, genderStr = UserInfo.genderMap[genderNum])
                    dismiss()
                }
                R.id.etcBtn -> { // 기타
                    val genderNum = (v.tag as String).toInt()
                    listener.onGenderSelect(genderNum = genderNum, genderStr = UserInfo.genderMap[genderNum])
                    dismiss()
                }
                R.id.cancelBtn -> { // 취소
                    dismiss()
                }
            }
        }
    }

    fun handle(listener: Listener) {
        this.listener = listener
    }

    companion object {
        fun newDialog(): GenderSelectDialog {
            return GenderSelectDialog()
        }
    }
}