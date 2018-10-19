package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.request.ReqGoodsSearchData
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.IShopRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiShopProvider {
    // 상품 간단 정보 가져오기
    fun requestGoodsSimple(scheduler: SchedulerProvider, goodsCode: String,
                           responseData: (ResGoodsSimpleData) -> Unit, errorData: (ResMessageErrorData?) -> Unit):Disposable

    // 상품 상세 가져오기
    fun requestGoodsDetail(scheduler: SchedulerProvider, goodsCode: String,
                           responseData: (ResGoodsDetailData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // shop feed 가져오기
    fun getShopFeed(scheduler: SchedulerProvider, authorization: String,
                    responseData: (ResShopFeedData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // search goods
    fun requestSearchGoods(scheduler: SchedulerProvider, authorization: String, reqGoodsSearchData: ArrayList<ReqGoodsSearchData>, page: Int, sortType: Int,
                           responseData: (ResGoodsSearchData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // 상품 통계 가져오기
    fun requestGoodsStats(scheduler: SchedulerProvider, authorization: String, goodsCode: String,
                          responseData: (ResGoodsStatsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // 상품 태그한 포스트 가져오기
    fun requestGoodsTaggedPosts(scheduler: SchedulerProvider, goodsCode: String, page: Int,
                                responseData: (ResThumbnailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // 최근 본 상품 가져오기
    fun requestRecentViewGoods(scheduler: SchedulerProvider, authorization: String, page: Int,
                               responseData: (ResGoodsThumbnailData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiShop(private val iShopRequest: IShopRequest): ApiShopProvider {
    override fun requestGoodsSimple(scheduler: SchedulerProvider, goodsCode: String,
                                    responseData: (ResGoodsSimpleData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iShopRequest.getGoodsSimple(goodsCode = goodsCode)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestGoodsDetail(scheduler: SchedulerProvider, goodsCode: String,
                                    responseData: (ResGoodsDetailData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iShopRequest.getGoodsDetail(goodsCode = goodsCode)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

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

    override fun requestGoodsStats(scheduler: SchedulerProvider, authorization: String, goodsCode: String,
                                   responseData: (ResGoodsStatsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iShopRequest.getGoodsStats(authorization = authorization, goodsCode = goodsCode)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestGoodsTaggedPosts(scheduler: SchedulerProvider, goodsCode: String, page: Int,
                                         responseData: (ResThumbnailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iShopRequest.getGoodsTaggedPosts(goodsCode = goodsCode, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestRecentViewGoods(scheduler: SchedulerProvider, authorization: String, page: Int,
                                        responseData: (ResGoodsThumbnailData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iShopRequest.getRecentViewGoods(authorization = authorization, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}