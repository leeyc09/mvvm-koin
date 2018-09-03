package xlab.world.xlab.view.preload

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.BuildConfig
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.login.LoginActivity
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class PreloadActivity: AppCompatActivity() {
    private val preLoadViewModel: PreloadViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var linkData: Uri? = null

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        linkData = intent.data
        linkData?.let { data ->
            val type: String? = data.getQueryParameter("type")
            val code: String? = data.getQueryParameter("code")

            type?.let { value -> PrintLog.d("linkData type", value, "linkData") } ?: PrintLog.d("type", "null", "linkData")
            code?.let { value -> PrintLog.d("linkData code", value, "linkData") } ?: PrintLog.d("code", "null", "linkData")
        } ?: PrintLog.d("linkData", "null", "linkData")

        printAppInfo()

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        PrintLog.d("accessToken", spHelper.accessToken)
        // 저장된 로그인 기록이 없는 경우 -> 로그인 화면으로
        if(spHelper.accessToken == "") {
            spHelper.logout()
            val intent = LoginActivity.newIntent(context = this, isComePreLoadActivity = true, linkData = null)
            startActivity(intent)
            finish()
        } else {
            preLoadViewModel.checkValidToken(authorization = spHelper.authorization, fcmToken = spHelper.fcmToken)
        }
    }

    private fun onBindEvent() {

    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        preLoadViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it)
                        progressDialog.show()
                    else
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                }
            }
        })

        // access token 체크 이벤트 observe
        preLoadViewModel.checkValidTokenEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { checkValidTokenEvent ->
            checkValidTokenEvent?.let { _ ->
                checkValidTokenEvent.loginData?.let { loginData ->
                    spHelper.login(accessToken = loginData.accessToken,
                            userType = loginData.loginType,
                            userId = loginData.userID,
                            socialId = loginData.socialID,
                            userLevel = loginData.userLevel,
                            userEmail = loginData.email,
                            push = loginData.isPushAlarmOn)
                }
                checkValidTokenEvent.isExpireToken?.let {
                    if (it)
                        PrintLog.d("checkValidTokenEvent", "isExpireToken")
                    else {
                        PrintLog.d("checkValidTokenEvent", "other error")
                    }
                }
            }
        })
    }

    private fun printAppInfo() {
        val tag = "AppInfo"
        PrintLog.d("packageName", packageName, tag)
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            packageInfo.signatures.forEach { signature ->
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                PrintLog.d("Key hash", keyHash, tag)
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }
        PrintLog.d("App Version Code", BuildConfig.VERSION_CODE.toString(), tag)
        PrintLog.d("App Version Name", BuildConfig.VERSION_NAME, tag)
    }
}