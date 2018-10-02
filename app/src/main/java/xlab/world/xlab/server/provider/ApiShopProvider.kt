package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.request.ReqGoodsSearchData
import xlab.world.xlab.data.response.ResGoodsSearchData
import xlab.world.xlab.data.response.ResMessageErrorData
import xlab.world.xlab.data.response.ResShopFeedData
import xlab.world.xlab.server.`interface`.IShopRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiShopProvider {
    // shop feed 가져오기
    fun getShopFeed(scheduler: SchedulerProvider, authorization: String,
                    responseData: (ResShopFeedData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // search goods
    fun requestSearchGoods(scheduler: SchedulerProvider, authorization: String, reqGoodsSearchData: ArrayList<ReqGoodsSearchData>, page: Int, sortType: Int,
                    responseData: (ResGoodsSearchData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

}

class ApiShop(private val iShopRequest: IShopRequest): ApiShopProvider {
    override fun getShopFeed(scheduler: SchedulerProvider, authorization: String,
                             responseData: (ResShopFeedData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iShopRequest.getShopMain(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestSearchGoods(scheduler: SchedulerProvider, authorization: String,
                             reqGoodsSearchData: ArrayList<ReqGoodsSearchData>, page: Int, sortType: Int,
                             responseData: (ResGoodsSearchData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iShopRequest.searchGoods(authorization = authorization, reqGoodsSearchData = reqGoodsSearchData,
                page = page, sortType = sortType)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}