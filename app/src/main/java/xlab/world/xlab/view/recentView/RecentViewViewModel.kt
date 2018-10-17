package xlab.world.xlab.view.recentView

import android.arch.lifecycle.MutableLiveData
import android.view.View
import xlab.world.xlab.data.adapter.GoodsThumbnailData
import xlab.world.xlab.data.adapter.GoodsThumbnailListData
import xlab.world.xlab.server.provider.ApiShopProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class RecentViewViewModel(private val apiShop: ApiShopProvider,
                          private val networkCheck: NetworkCheck,
                          private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "RecentView"

    val loadRecentViewGoodsEventData = SingleLiveEvent<RecentViewEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadRecentViewGoods(authorization: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadRecentViewGoodsEventData.postValue(RecentViewEvent(status = true))
        launch {
            apiShop.requestRecentViewGoods(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        PrintLog.d("requestRecentViewGoods success", it.toString())
                        val recentViewGoodsData = GoodsThumbnailData(total = it.total, nextPage = page + 1)
                        it.goods?.forEach { goods ->
                            recentViewGoodsData.items.add((GoodsThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    goodsImage = goods.image,
                                    goodsCd = goods.code
                            )))
                        }
                        val emptyVisibility =
                                if (page == 1) {
                                    if (recentViewGoodsData.items.isEmpty()) View.VISIBLE
                                    else View.GONE
                                }
                                else null

                        uiData.value = UIModel(isLoading = false, recentViewGoodsData = recentViewGoodsData,
                                emptyVisibility = emptyVisibility)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestRecentViewGoods fail", errorData.message)
                        }
                    })
        }
    }
}

data class RecentViewEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val recentViewGoodsData: GoodsThumbnailData? = null,
                   val emptyVisibility: Int? = null)
