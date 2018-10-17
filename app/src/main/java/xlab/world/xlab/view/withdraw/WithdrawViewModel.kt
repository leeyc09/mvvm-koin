package xlab.world.xlab.view.withdraw

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class WithDrawViewModel(private val apiUser: ApiUserProvider,
                        private val networkCheck: NetworkCheck,
                        private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val tag = "WithDraw"
    private var withdrawReason: String? = null

    val withdrawEventData = SingleLiveEvent<WithdrawEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun enableWithdraw(withdrawReason: String?, isAgree: Boolean) {
        launch {
            Observable.create<Boolean> {
                withdrawReason?.let { _-> this.withdrawReason = withdrawReason }

                it.onNext((this.withdrawReason != null) && isAgree)
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("enableWithdraw", it.toString())
                uiData.value = UIModel(enableWithdraw = it)
            }
        }
    }

    fun withdraw(context: Context, otherReason: String?, authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = networkCheck.networkErrorMsg)
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiUser.requestWithdraw(scheduler = scheduler, authorization = authorization, content = otherReason ?: withdrawReason!!,
                    responseData = {
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_withdraw_success))
                        withdrawEventData.value = WithdrawEvent(status = true)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.d("requestWithdraw fail", errorData.message)
                        }
                        uiData.value = UIModel(isLoading = false)
                    })
        }
    }
}

data class WithdrawEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val enableWithdraw: Boolean? = null)
