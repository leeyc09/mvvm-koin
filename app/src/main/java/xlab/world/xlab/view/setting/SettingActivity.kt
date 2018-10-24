package xlab.world.xlab.view.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_setting.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.BuildConfig
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultOneDialog
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.ShopAccountDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.notice.NoticeViewModel

class SettingActivity : AppCompatActivity(), View.OnClickListener {
    private val settingViewModel: SettingViewModel by viewModel()
    private val noticeViewModel: NoticeViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var logoutDialog: DefaultOneDialog
    private lateinit var shopLogoutDialog: ShopAccountDialog

    private val logoutListener = object: DefaultOneDialog.Listener {
        override fun onOkayTouch(tag: Any?) {
            settingViewModel.userLogout(authorization = spHelper.authorization)
        }
    }

    private val shopAccountListener = object: ShopAccountDialog.Listener {
        override fun webViewFinish(result: Boolean) {
            if (result) {
                spHelper.logout()
                RunActivity.loginActivity(context = this@SettingActivity, isComePreLoadActivity = true, linkData = null)

                settingViewModel.backBtnAction()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name.toString())
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name.toString())

        settingViewModel.setResultCode(resultCode = resultCode)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.WITHDRAW -> { // 회원탈퇴 -> 로그아웃 진행
                        settingViewModel.userLogout(authorization = spHelper.authorization)
                    }
                }
            }
            ResultCodeData.LOAD_OLD_DATA -> {
                when (requestCode) {
                    RequestCodeData.NOTICE -> { // 공지사항
                        noticeViewModel.loadExistNewNotification(authorization = spHelper.authorization)
                    }
                }
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                actionBackBtn.performClick()
            }
        }
    }

    private fun onSetup() {
        // 타이틀 설정, 액션 버튼 비활성
        actionBarTitle.setText(getString(R.string.setting), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE
        // 앱 버전
        textViewVersion.text = String.format("V %s", BuildConfig.VERSION_NAME)

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        logoutDialog = DefaultOneDialog(context = this,
                text = getString(R.string.dial_logout),
                listener = logoutListener)
        shopLogoutDialog = ShopAccountDialog(context = this, listener = shopAccountListener)

        settingViewModel.loadUserSettingData(userType = spHelper.userType, authorization = spHelper.authorization)
        noticeViewModel.loadExistNewNotification(authorization = spHelper.authorization)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        profileEditBtn.setOnClickListener(this) // 프로필 편집
        topicSettingBtn.setOnClickListener(this) // 토픽 설정
        likedPostBtn.setOnClickListener(this) // 좋아한 포스트
        changePasswordBtn.setOnClickListener(this) // 비밀번호 변경
        updateShopAccountBtn.setOnClickListener(this) // shop 정보 편집
        pushAlarmBtn.setOnClickListener(this) // 푸시 수신 on&off
        noticeBtn.setOnClickListener(this) // 공지사항
        updateBtn.setOnClickListener(this) // 앱 버전 업데이트
        policyBtn.setOnClickListener(this) // 약관 및 정책
        suggestionBtn.setOnClickListener(this) // 문의 및 의견 보내기
        logoutBtn.setOnClickListener(this) // 로그아웃
        withdrawBtn.setOnClickListener(this) // 회원 탈퇴
    }

    private fun observeViewModel() {
        // TODO: Setting View Model
        // UI 이벤트 observe
        settingViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.pushAlarm?.let {
                    pushAlarmBtn.isSelected = it
                }
            }
        })

        // logout 이벤트 observe
        settingViewModel.logoutData.observe(owner = this, observer = android.arch.lifecycle.Observer { logoutEventData ->
            logoutEventData?.let { isSuccess ->
                if (isSuccess) {
                    shopLogoutDialog.requestLogout(userId = spHelper.userId)
                }
            }
        })

        // TODO: Notice View Model
        // UI 이벤트 observe
        noticeViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let {_->
                uiData.newNoticeDotVisibility?.let {
                    noticeDotView.visibility = it
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    settingViewModel.backBtnAction()
                }
                R.id.profileEditBtn -> { // 프로필 편집
                    RunActivity.profileEditActivity(context = this)
                }
                R.id.topicSettingBtn -> { // 토픽 설정
                    RunActivity.topicSettingActivity(context = this)
                }
                R.id.likedPostBtn -> { // 좋아한 포스트
                    RunActivity.likedPostActivity(context = this)
                }
                R.id.changePasswordBtn -> { // 비밀번호 변경
                    RunActivity.updatePasswordActivity(context = this)
                }
                R.id.updateShopAccountBtn -> { // shop 정보 편집
                    RunActivity.shopProfileEditActivity(context = this)
                }
                R.id.pushAlarmBtn -> { // 푸시 수신 on&off
                    settingViewModel.updatePushAlarm(authorization = spHelper.authorization)
                }
                R.id.noticeBtn -> { // 공지사항
                    RunActivity.noticeActivity(context = this)
                }
                R.id.updateBtn -> { // 앱 버전 업데이트
                    val updateURL = Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                    startActivity(Intent(Intent.ACTION_VIEW, updateURL))
                }
                R.id.policyBtn -> { // 약관 및 정책
                    RunActivity.policyActivity(context = this)
                }
                R.id.suggestionBtn -> { // 문의 및 의견 보내기
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:")
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(resources.getString(R.string.cs_mail)))
                    intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.mail_subject))

                    val messageBody = "\n\n\n${resources.getString(R.string.mail_content1)}" +
                            "${resources.getString(R.string.mail_content2)} ${spHelper.userEmail}\n" + // user email
                            "${resources.getString(R.string.mail_content3)} Android ${Build.VERSION.RELEASE} API ${Build.VERSION.SDK_INT}\n" + // os version
                            "${resources.getString(R.string.mail_content4)} ${BuildConfig.VERSION_NAME}\n" + // app version
                            "${resources.getString(R.string.mail_content5)} ${Build.MANUFACTURER} ${Build.MODEL}" // device name

                    intent.putExtra(Intent.EXTRA_TEXT, messageBody)

                    startActivity(intent)
                }
                R.id.logoutBtn -> { // 로그아웃
                    logoutDialog.show()
                }
                R.id.withdrawBtn -> { // 회원 탈퇴
                    RunActivity.withdrawActivity(context = this)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SettingActivity::class.java)
        }
    }
}
