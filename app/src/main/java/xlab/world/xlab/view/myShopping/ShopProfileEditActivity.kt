package xlab.world.xlab.view.myShopping

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_shop_profile_edit.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultDialog
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.toast.DefaultToast

class ShopProfileEditActivity : AppCompatActivity(), View.OnClickListener {
    private val myShoppingViewModel: MyShoppingViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var editCancelDialog: DefaultDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_profile_edit)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        // 타이틀 설정, 액션 버튼 비활성화
        actionBarTitle.setText(getText(R.string.edit_shop_info), TextView.BufferType.SPANNABLE)
        actionBtn.isEnabled = false

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        editCancelDialog = DialogCreator.editCancelDialog(context = this)

        myShoppingViewModel.loadShopProfile(authorization = spHelper.authorization)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionBtn.setOnClickListener(this) // 저장하기

        ViewFunction.onTextChange(editText = editTextName) {
            myShoppingViewModel.enableSaveData(name = it)
        }
        ViewFunction.onTextChange(editText = editTextEmail) {
            myShoppingViewModel.enableSaveData(email = it)
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        myShoppingViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                }
                uiData.resultCode?.let {
                    setResult(it)
                    finish()
                }
                uiData.actionBtnEnable?.let {
                    actionBtn.isEnabled = it
                }
                uiData.cancelDialogShow?.let {
                    editCancelDialog.showDialog(tag = null)
                }
                uiData.shopName?.let {
                    editTextName.setText(it)
                }
                uiData.shopEmail?.let {
                    editTextEmail.setText(it)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    myShoppingViewModel.profileEditActionBackBtnAction()
                }
                R.id.actionBtn -> { // 저장하기
                    myShoppingViewModel.updateShopProfile(context = this, authorization = spHelper.authorization)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ShopProfileEditActivity::class.java)
        }
    }
}
