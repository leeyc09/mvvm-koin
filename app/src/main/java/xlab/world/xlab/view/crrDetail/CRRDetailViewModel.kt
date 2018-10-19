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

                        var titleStr: String? = null // 처리 모드에 따른 타이틀
                        var reasonTitle: String? = null // 처리 모드에 따른 이유 타이틀
                        var refundInfo: String? = null // CRR 환불 정보
                        var refundInfoVisibility: Int? = null // 처리 모드에 따른 환불 정보 보이기 & 숨기기

                        when (it.userHandleMode) {
                            "r" -> { // 환불
                                titleStr = context.getString(R.string.order_step_refund_detail)
                                reasonTitle = context.getString(R.string.goods_refund_reason)
                                refundInfo = it.refundInfo
                                refundInfoVisibility = View.VISIBLE
                            }
                            "b" -> { // 반품
                                titleStr = context.getString(R.string.order_step_return_detail)
                                reasonTitle = context.getString(R.string.goods_return_reason)
                                refundInfo = it.refundInfo
                                refundInfoVisibility = View.VISIBLE
                            }
                            "e" -> { // 교환
                                titleStr = context.getString(R.string.order_step_change_detail)
                                reasonTitle = context.getString(R.string.goods_change_reason)
                                refundInfoVisibility = View.GONE
                            }
                        }

                        var adminMemoTitle: String? = null // 관리자 메모 타이틀
                        var adminMemo: String? = null // 관리자 메모
                        var adminMemoVisibility: Int? = null // 처리 상태에 따른 사유 보이기 & 숨기기
                        when (it.userHandleFl) {
                            "y" -> { // 승인
                                adminMemoTitle = context.getString(R.string.approval_reason)
                                adminMemo = it.adminReason
                                adminMemoVisibility = View.VISIBLE
                            }
                            "n" -> { // 거절
                                adminMemoTitle = context.getString(R.string.refuse_reason)
                                adminMemo = it.adminReason
                                adminMemoVisibility = View.VISIBLE
                            }
                            "r" -> { // 신청
                                adminMemoVisibility = View.GONE
                            }
                        }

                        var crrMemo: String? = null // CRR 메모
                        var crrMemoVisibility: Int? = null // CRR 메모 보이기 & 숨기기
                        if (it.memo.isEmpty()) {
                            crrMemoVisibility = View.GONE
                        } else {
                            crrMemo = it.memo
                            crrMemoVisibility = View.VISIBLE
                        }

                        uiData.value = UIModel(isLoading = false,
                                titleStr = titleStr, reasonTitle = reasonTitle,
                                refundInfo = refundInfo, refundInfoVisibility = refundInfoVisibility,
                                adminMemoTitle = adminMemoTitle, adminMemo = adminMemo,
                                adminMemoVisibility = adminMemoVisibility, crrReason = it.reason,
                                crrMemo = crrMemo, crrMemoVisibility = crrMemoVisibility)
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

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val titleStr: String? = null, val reasonTitle: String? = null,
                   val refundInfo: String? = null, val refundInfoVisibility: Int? = null,
                   val adminMemoTitle: String? = null, val adminMemo: String? = null,
                   val adminMemoVisibility: Int? = null, val crrReason: String? = null,
                   val crrMemo: String? = null, val crrMemoVisibility: Int? = null)
