package xlab.world.xlab.view.preload

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.BuildConfig
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.ShopAccountDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.login.LoginViewModel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class PreloadActivity: AppCompatActivity() {
    private val preloadViewModel: PreloadViewModel by viewModel()
    private val loginViewModel: LoginViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var shopAccountDialog: ShopAccountDialog

    private val shopLoginListener = object: ShopAccountDialog.Listener {
        override fun webViewFinish(result: Boolean) {
            if (result) {
                spHelper.logout()
                RunActivity.loginActivity(context = this@PreloadActivity,
                        isComePreLoadActivity = true,
                        linkData = null)
                finish()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Fabric.with(this, Crashlytics())

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        intent.data?.let { data ->
            val type: String? = data.getQueryParameter("type")
            val code: String? = data.getQueryParameter("code")

            type?.let { value -> PrintLog.d("linkData type", value, "linkData") } ?: PrintLog.d("type", "null", "linkData")
            code?.let { value -> PrintLog.d("linkData code", value, "linkData") } ?: PrintLog.d("code", "null", "linkData")
        } ?: PrintLog.d("linkData", "null", "linkData")

        printAppInfo()

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        shopAccountDialog = ShopAccountDialog(context = this, listener = shopLoginListener)

        preloadViewModel.checkOnBoarding(onBoarding = spHelper.onBoard)
    }

    private fun onBindEvent() {
    }

    private fun observeViewModel() {
        // TODO: PreLoad View Model
        // on boarding page 전환 이벤트 observe
        preloadViewModel.onBoardingData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { needOnBoardingPage ->
                if (needOnBoardingPage) {
                    spHelper.logout()
                    RunActivity.onBoarding(context = this)
                    finish()
                } else {
                    preloadViewModel.loginRecordCheck(accessToken = spHelper.accessToken)
                }
            }
        })
        // 자동 로그인 이벤트 observe
        preloadViewModel.loginRecordModelData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { hasLoginRecord ->
                if (hasLoginRecord) { // 로그인 기록 있는 경우
                    loginViewModel.requestLoginByAccessToken(authorization = spHelper.authorization, fcmToken = spHelper.fcmToken)
                } else { // 로그인 기록 없는 경우
                    shopAccountDialog.requestLogout(userId = spHelper.userId)
                }
            }
        })

        // TODO: Login View Model
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
                }
            }
        })

        // access token 로그인 시도 이벤트 observe
        loginViewModel.loginByAccessTokenData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.loginData?.let { loginData -> // 로그인 성공 -> 메인 화면
                    spHelper.login(accessToken = loginData.accessToken,
                            userType = loginData.loginType,
                            userId = loginData.userID,
                            socialId = loginData.socialID,
                            userLevel = loginData.userLevel,
                            userEmail = loginData.email,
                            push = loginData.isPushAlarmOn)
                    RunActivity.mainActivity(context = this, linkData = intent.data)
                    finish()
                }
                eventData.isExpireToken?.let {
                    if (it) { // access token 만료
                        loginViewModel.generateNewToken(authorization = spHelper.authorization)
                    }
                    else { // 다른 이유
                        shopAccountDialog.requestLogout(userId = spHelper.userId)
                    }
                }
            }
        })

        // 토큰 갱신 이벤트 observe
        loginViewModel.generateTokenEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.newAccessToken?.let { // 새로운 토큰 저장소에 저장 -> preload 다시 실행
                    spHelper.accessToken = it
                    finish()
                    startActivity(intent)
                }
                eventData.isFailGenerateToken?.let { // 토큰 발행 실패
                    shopAccountDialog.requestLogout(userId = spHelper.userId)
                }
            }
        })
    }

    // 앱 정보 출력
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