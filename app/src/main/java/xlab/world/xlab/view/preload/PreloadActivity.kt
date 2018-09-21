package xlab.world.xlab.view.preload

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.BuildConfig
import xlab.world.xlab.utils.support.PetInfo
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.login.LoginActivity
import xlab.world.xlab.view.login.LoginViewModel
import xlab.world.xlab.view.main.MainActivity
import xlab.world.xlab.view.onBoarding.OnBoardingActivity
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class PreloadActivity: AppCompatActivity() {
    private val loginViewModel: LoginViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var linkData: Uri? = null

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Fabric.with(this, Crashlytics())

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

        // onBoarding 화면 본적 없는 경우 -> onBoarding 화면으로
        if(!spHelper.onBoard) {
            spHelper.logout()
            RunActivity.onBoarding(context = this)
            finish()
            return
        }

        PrintLog.d("accessToken", spHelper.accessToken)
        // 저장된 로그인 기록이 없는 경우 -> 로그인 화면으로
        if(spHelper.accessToken == "") {
            spHelper.logout()
            RunActivity.loginActivity(context = this, isComePreLoadActivity = true, linkData = linkData)
            finish()
        } else {
            loginViewModel.requestLoginByAccessToken(authorization = spHelper.authorization, fcmToken = spHelper.fcmToken)
        }
    }

    private fun onBindEvent() {

    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        loginViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                    spHelper.logout()
                    RunActivity.loginActivity(context = this, isComePreLoadActivity = true, linkData = linkData)
                    finish()
                }
            }
        })

        // access token 로그인 시도 이벤트 observe
        loginViewModel.requestLoginByAccessTokenEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { checkValidTokenEvent ->
            checkValidTokenEvent?.let { _ ->
                checkValidTokenEvent.loginData?.let { loginData -> // 로그인 성공 -> 메인 화면
                    spHelper.login(accessToken = loginData.accessToken,
                            userType = loginData.loginType,
                            userId = loginData.userID,
                            socialId = loginData.socialID,
                            userLevel = loginData.userLevel,
                            userEmail = loginData.email,
                            push = loginData.isPushAlarmOn)
                    RunActivity.mainActivity(context = this, linkData = linkData)
                    finish()
                }
                checkValidTokenEvent.isExpireToken?.let {
                    if (it) { // access token 만료 -> 토큰 갱신 시도
                        loginViewModel.generateNewToken(authorization = spHelper.authorization)
                    }
                    else { // 다른 이유 -> 로그인 화면
                        spHelper.logout()
                        RunActivity.loginActivity(context = this, isComePreLoadActivity = true, linkData = linkData)
                        finish()
                    }
                }
            }
        })

        // 토큰 갱신 이벤트 observe
        loginViewModel.generateTokenEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { generateTokenEvent ->
            generateTokenEvent?.let { _ ->
                generateTokenEvent.newAccessToken?.let { // 새로운 토큰 저장소에 저장
                    spHelper.accessToken = it
                    finish()
                    startActivity(intent)
                }
                generateTokenEvent.isFailGenerateToken?.let {
                    spHelper.logout()
                    RunActivity.loginActivity(context = this, isComePreLoadActivity = true, linkData = linkData)
                    finish()
                }
            }
        })
    }

    // 앱 정보 출
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