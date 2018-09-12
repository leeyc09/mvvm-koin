package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
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
}