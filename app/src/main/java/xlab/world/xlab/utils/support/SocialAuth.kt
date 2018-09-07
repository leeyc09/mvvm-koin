package xlab.world.xlab.utils.support

import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.LogoutResponseCallback
import com.kakao.util.exception.KakaoException

class SocialAuth {
    private val tag = "SocialAuth"
    var facebookCallbackManager: CallbackManager? = null
    private var kakaoCallbackManager: KakaoSessionCallback? = null

    fun getFacebookToken(result: (String) -> Unit, error: (String) -> Unit) {
        facebookCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                FacebookSessionCallback(result, error))
    }

    fun getKakaoToken(result:(String) -> Unit, error: (String) -> Unit) {
        PrintLog.d("function", "kakao login", tag)
        if (kakaoCallbackManager == null) {
            kakaoCallbackManager = KakaoSessionCallback(result, error)
            Session.getCurrentSession()
            Session.getCurrentSession().addCallback(kakaoCallbackManager)
            Session.getCurrentSession().checkAndImplicitOpen()
        }
    }

    // kakao login callback class
    private inner class KakaoSessionCallback(var result:(String) -> Unit, var error: (String) -> Unit): ISessionCallback {
        override fun onSessionOpened() {
            result(Session.getCurrentSession().tokenInfo.accessToken)
        }
        override fun onSessionOpenFailed(exception: KakaoException) {
            PrintLog.d("kakao onSessionOpenFailed", exception.message!!, tag)
            error(exception.message!!)
        }
    }

    // facebook login callback class
    private inner class FacebookSessionCallback(var result:(String) -> Unit, var error: (String) -> Unit): FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
            result(loginResult.accessToken.token)
        }
        override fun onCancel() {
            PrintLog.d("facebook onCancel", "onCancel", tag)
            error("onCancel")
        }
        override fun onError(exception: FacebookException) {
            PrintLog.d("facebook onError", exception.message!!, tag)
            error(exception.message!!)
        }
    }

    fun facebookLogout() {
        PrintLog.d("facebook logout", "success", tag)
        LoginManager.getInstance().logOut()
    }

    fun kakaoLogout() {
        UserManagement.requestLogout(object : LogoutResponseCallback() {
            override fun onCompleteLogout() {
                PrintLog.d("kakao logout", "success", tag)
            }
        })
    }
}
