package xlab.world.xlab.view

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.TemplateParams
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import io.reactivex.Observable
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.ShareContent

class ShareViewModel(private val networkCheck: NetworkCheck,
                     private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Share"

    val uiData = MutableLiveData<UIModel>()

    fun shareKakao(context: Context, shareParams: TemplateParams) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        launch {
            Observable.create<Any> {
                KakaoLinkService.getInstance().sendDefault(context, shareParams, object : ResponseCallback<KakaoLinkResponse>() {
                    override fun onSuccess(result: KakaoLinkResponse?) {
                        PrintLog.d("kakao share success", "", viewModelTag)
                    }

                    override fun onFailure(errorResult: ErrorResult?) {
                        PrintLog.e("kakao share fail", errorResult.toString(), viewModelTag)
                    }
                })
            }.with(scheduler = scheduler).subscribe {}
        }
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)
