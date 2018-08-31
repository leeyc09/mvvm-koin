package xlab.world.xlab

import android.app.Application
import com.kakao.auth.*
import xlab.world.xlab.di.appModule
import org.koin.android.ext.android.startKoin

class GlobalApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        application = this
        KakaoSDK.init(KakaoSDKAdapter())

        // start koin
        startKoin(this, appModule)
    }

    companion object {
        var application: GlobalApplication? = null
    }

    internal inner class KakaoSDKAdapter : KakaoAdapter() {
        override fun getSessionConfig(): ISessionConfig {
            return object : ISessionConfig {
                override fun getAuthTypes(): Array<AuthType> {
                    return arrayOf(AuthType.KAKAO_LOGIN_ALL)
                }

                override fun isUsingWebviewTimer(): Boolean {
                    return false
                }

                override fun isSecureMode(): Boolean {
                    return false
                }

                override fun getApprovalType(): ApprovalType {
                    return ApprovalType.INDIVIDUAL
                }

                override fun isSaveFormData(): Boolean {
                    return true
                }
            }
        }

        override fun getApplicationConfig(): IApplicationConfig {
            return IApplicationConfig { GlobalApplication.Companion.application }
        }
    }
}