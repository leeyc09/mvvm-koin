package xlab.world.xlab.view.crrDetail

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.view.View
import xlab.world.xlab.R
import xlab.world.xlab.server.provider.ApiGodoProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel

class CRRDetailViewModel(private val apiGodo: ApiGodoProvider,
                         private val networkCheck: NetworkCheck,
                         private val scheduler: SchedulerProvider): AbstractViewModel() {

    val uiData = MutableLiveData<UIModel>()

    fun loadCRRDetail(context: Context, authorization: String, handelSno: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestCRRDetail(scheduler = scheduler, authorization = authorization, handleSno = handelSno,
                    responseData = {
                        PrintLog.d("requestCRRDetail success", it.toString())

                        var titleStr: String? = null
                        var reasonTitle: String? = null
                        var bankInfoVisibility: Int? = null

                        when (it.userHandleMode) {
                            "r" -> { // 환불
                                titleStr = context.getString(R.string.order_step_refund_detail)
                                reasonTitle = context.getString(R.string.goods_refund_reason)
                                bankInfoVisibility = View.VISIBLE
                            }
                            "b" -> { // 반품
                                titleStr = context.getString(R.string.order_step_return_detail)
                                reasonTitle = context.getString(R.string.goods_return_reason)
                                bankInfoVisibility = View.VISIBLE
                            }
                            "e" -> { // 교환
                                titleStr = context.getString(R.string.order_step_change_detail)
                                reasonTitle = context.getString(R.string.goods_change_reason)
                                bankInfoVisibility = View.INVISIBLE
                            }
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestCRRDetail fail", errorData.message)
                        }
                    })
        }
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)
