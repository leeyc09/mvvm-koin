package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.request.ReqUsedGoodsData
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.IPostRequest
import xlab.world.xlab.server.`interface`.IUserActivityRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiUserActivityProvider {
    // post like
    fun postLike(scheduler: SchedulerProvider, authorization: String, postId: String,
                 responseData: (ResLikeSavePostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // liked post 요청
    fun requestLikedPosts(scheduler: SchedulerProvider, authorization: String, page: Int,
                          responseData: (ResThumbnailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // post save
    fun postSave(scheduler: SchedulerProvider, authorization: String, postId: String,
                 responseData: (ResLikeSavePostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // saved post 요청
    fun requestSavedPosts(scheduler: SchedulerProvider, authorization: String, page: Int,
                          responseData: (ResThumbnailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // topic 사용한 goods 요청
    fun requestTopicUsedGoods(scheduler: SchedulerProvider, userId: String, goodsType: Int, page: Int,
                              responseData: (ResGoodsThumbnailData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // 사용한 상품 등록
    fun requestPostUsedGoods(scheduler: SchedulerProvider, authorization: String, reqUsedGoodsData: ReqUsedGoodsData,
                             responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // 사용한 상품 삭제
    fun requestDeleteUsedGoods(scheduler: SchedulerProvider, authorization: String, goodsCode: String, topicId: String,
                               responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiUserActivity(private val iUserActivityRequest: IUserActivityRequest): ApiUserActivityProvider {
    override fun postLike(scheduler: SchedulerProvider, authorization: String, postId: String,
                          responseData: (ResLikeSavePostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserActivityRequest.likePost(authorization = authorization, postId = postId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestLikedPosts(scheduler: SchedulerProvider, authorization: String, page: Int,
                                   responseData: (ResThumbnailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserActivityRequest.getLikedPosts(authorization = authorization, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun postSave(scheduler: SchedulerProvider, authorization: String, postId: String,
                          responseData: (ResLikeSavePostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserActivityRequest.savePost(authorization = authorization, postId = postId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestSavedPosts(scheduler: SchedulerProvider, authorization: String, page: Int,
                                   responseData: (ResThumbnailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserActivityRequest.getSavedPosts(authorization = authorization, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestTopicUsedGoods(scheduler: SchedulerProvider, userId: String, goodsType: Int, page: Int,
                                       responseData: (ResGoodsThumbnailData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserActivityRequest.getUsedGoods(userId = userId, goodsType = goodsType, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestPostUsedGoods(scheduler: SchedulerProvider, authorization: String, reqUsedGoodsData: ReqUsedGoodsData,
                                      responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserActivityRequest.postUsedGoods(authorization = authorization, reqUsedItemData = reqUsedGoodsData)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestDeleteUsedGoods(scheduler: SchedulerProvider, authorization: String, goodsCode: String, topicId: String,
                                        responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserActivityRequest.deleteUsedGoods(authorization = authorization, goodsCode = goodsCode, topicId = topicId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}