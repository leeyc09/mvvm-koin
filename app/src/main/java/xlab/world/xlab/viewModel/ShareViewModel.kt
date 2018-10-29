package xlab.world.xlab.viewModel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.net.Uri
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.TemplateParams
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import io.reactivex.Observable
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class ShareViewModel(private val networkCheck: NetworkCheck,
                     private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Share"

    val linkData = SingleLiveEvent<LinkDataModel?>()
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

    fun linkShareActivity(data: Uri?) {
        launch {
            Observable.create<Any> {
                data?.let { data ->
                    val type: String? = data.getQueryParameter("type")
                    val code: String? = data.getQueryParameter("code")

                    if (type != null && code != null) {
                        PrintLog.d("linkData type", type, viewModelTag)
                        PrintLog.d("linkData code", code, viewModelTag)
                        when (type) {
                            AppConstants.LINK_PROFILE -> {
                                linkData.postValue(LinkDataModel(userId = code))
                            }
                            AppConstants.LINK_POST -> {
                                linkData.postValue(LinkDataModel(postId = code))
                            }
                            AppConstants.LINK_GOODS -> {
                                linkData.postValue(LinkDataModel(goodsCode = code))
                            }
                        }
                    } else {
                        type?.let { value -> PrintLog.d("linkData type", value, viewModelTag) } ?: PrintLog.d("type", "null", viewModelTag)
                        code?.let { value -> PrintLog.d("linkData code", value, viewModelTag) } ?: PrintLog.d("code", "null", viewModelTag)
                    }
                    it.onComplete()
                } ?: PrintLog.d("linkData", "null", viewModelTag)
            }.with(scheduler = scheduler).subscribe {
                linkData.value = LinkDataModel()
            }
        }
    }
}

data class LinkDataModel(val userId: String? = null, val postId: String? = null, val goodsCode: String? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)
