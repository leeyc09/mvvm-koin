package xlab.world.xlab.view.goodsRating

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.GoodsRatingData
import xlab.world.xlab.data.adapter.GoodsRatingListData
import xlab.world.xlab.data.request.ReqUsedGoodsData
import xlab.world.xlab.server.provider.ApiPetProvider
import xlab.world.xlab.server.provider.ApiShopProvider
import xlab.world.xlab.server.provider.ApiUserActivityProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class GoodsRatingViewModel(private val apiShop: ApiShopProvider,
                           private val apiPet: ApiPetProvider,
                           private val apiUserActivity: ApiUserActivityProvider,
                           private val networkCheck: NetworkCheck,
                           private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "GoodsRating"

    private var goodsCode = ""
    private var goodsImage = ""
    private var goodsName = ""
    private var goodsBrand = ""

    private var initGoodsRatingData = GoodsRatingData()
    private var goodsRatingData = GoodsRatingData()

    val uiData = MutableLiveData<UIModel>()

    fun loadGoodsData(goodsCode: String) {
        this.goodsCode = goodsCode
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiShop.requestGoodsSimple(scheduler = scheduler, goodsCode = goodsCode,
                    responseData = {
                        PrintLog.d("requestGoodsSimpleData success", it.toString(), viewModelTag)
                        this.goodsImage = it.image
                        this.goodsName = it.name
                        this.goodsBrand = it.brand
                        uiData.value = UIModel(isLoading = false, goodsImageUrl = it.image,
                                goodsBrand = it.brand, goodsName = it.name)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestGoodsSimpleData fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun loadGoodsRatingData(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.requestGoodsDetailPets(scheduler = scheduler, authorization = authorization, goodsCode = goodsCode,
                    responseData = {
                        PrintLog.d("requestGoodsDetailPets success", it.toString(), viewModelTag)
                        val newGoodsRatingData = GoodsRatingData()
                        it.petsData?.forEach { pet ->
                            newGoodsRatingData.items.add(GoodsRatingListData(
                                    petId = pet.id,
                                    petType = pet.type,
                                    petBreed = pet.breed,
                                    petImage = pet.image,
                                    petName = pet.name,
                                    rating = pet.rating
                            ))
                        }
                        newGoodsRatingData.total = newGoodsRatingData.items.size

                        this.goodsRatingData.updateData(newGoodsRatingData)
                        this.initGoodsRatingData.updateData(newGoodsRatingData)

                        uiData.value = UIModel(isLoading = false, ratingData = this.goodsRatingData,
                                finishBtnEnable = false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestGoodsDetailPets fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun ratingChange(position: Int, rating: Int) {
        launch {
            Observable.create<Boolean> {
                // 다른 rating 선택 -> rating 변경, 같은 rating 선택 -> rating 취소
                goodsRatingData.items[position].rating =
                        if (goodsRatingData.items[position].rating != rating) rating
                        else AppConstants.GOODS_RATING_NONE

                it.onNext(goodsRatingData != initGoodsRatingData)
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(ratingDataUpdateIndex = position, finishBtnEnable = it)
            }
        }
    }

    fun postGoodsRating(context: Context, authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        postGoodsRating(authorization = authorization, index = 0) {
            uiData.postValue(UIModel(isLoading = false,
                    toastMessage = context.getString(R.string.toast_rating_success),
                    resultCode = Activity.RESULT_OK))
        }
    }

    private fun postGoodsRating(authorization: String, index: Int, finishTask: (Boolean) -> Unit) {
        // rating 값 변동이 있을 경우
        if (goodsRatingData.items[index].rating != initGoodsRatingData.items[index].rating) {
            // rating 값 없으면 rating 지우기
            if (goodsRatingData.items[index].rating == AppConstants.GOODS_RATING_NONE) {
                postRatingCancel(authorization = authorization, ratingData = goodsRatingData.items[index]) { end ->
                    if (index + 1 < goodsRatingData.total)
                        postGoodsRating(authorization = authorization, index = index + 1) {
                            finishTask(it)
                        }
                    else
                        finishTask(true)
                }
            } else {
                postRating(authorization = authorization, ratingData = goodsRatingData.items[index]) { end ->
                    if (index + 1 < goodsRatingData.total)
                        postGoodsRating(authorization = authorization, index = index + 1) {
                            finishTask(it)
                        }
                    else
                        finishTask(true)
                }
            }
        } else { // 변동 없을 경우
            if (index + 1 < goodsRatingData.total)
                postGoodsRating(authorization = authorization, index = index + 1) {
                    finishTask(it)
                }
            else
                finishTask(true)
        }
    }

    private fun postRatingCancel(authorization: String, ratingData: GoodsRatingListData, end: (Boolean) -> Unit) {
        launch {
            apiUserActivity.requestDeleteUsedGoods(scheduler = scheduler, authorization = authorization,
                    goodsCode = goodsCode, topicId = ratingData.petId,
                    responseData = {
                        PrintLog.d("requestDeleteUsedGoods success", ratingData.toString(), viewModelTag)
                        end(true)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.e("requestDeleteUsedGoods fail", errorData.message, viewModelTag)
                        }
                        end(true)
                    })
        }
    }

    private fun postRating(authorization: String, ratingData: GoodsRatingListData, end: (Boolean) -> Unit) {
        launch {
            val reqUsedGoodsData = ReqUsedGoodsData(
                    usedType = AppConstants.FROM_RATING,
                    goodsCode = goodsCode,
                    goodsName = goodsName,
                    goodsBrand = goodsBrand,
                    goodsImage = goodsImage,
                    goodsType = AppConstants.GOODS_PET,
                    topic = ReqUsedGoodsData.Topic(
                            id = ratingData.petId,
                            type = ratingData.petType,
                            breed = ratingData.petBreed,
                            rating = ratingData.rating
                    ))
            apiUserActivity.requestPostUsedGoods(scheduler = scheduler, authorization = authorization, reqUsedGoodsData = reqUsedGoodsData,
                    responseData = {
                        PrintLog.d("requestPostUsedGoods success", ratingData.toString(), viewModelTag)
                        end(true)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.e("requestPostUsedGoods fail", errorData.message, viewModelTag)
                        }
                        end(true)
                    })
        }
    }

    fun backBtnAction() {
        launch {
            Observable.create<Boolean> {
                it.onNext(goodsRatingData != initGoodsRatingData)
                it.onComplete()
            }.with(scheduler).subscribe {
                // 바뀐 내용이 있으면 확인 다이얼로그 띄우고, 없으면 바로 종료
                if (it)
                    uiData.value = UIModel(isChangeData = it)
                else
                    uiData.value = UIModel(resultCode = Activity.RESULT_CANCELED)

            }
        }
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val goodsImageUrl: String? = null, val goodsBrand: String? = null, val goodsName: String? = null,
                   val ratingData: GoodsRatingData? = null, val ratingDataUpdateIndex: Int? = null,
                   val finishBtnEnable: Boolean? = null,
                   val isChangeData: Boolean? = null)
