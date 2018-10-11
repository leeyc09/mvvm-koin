package xlab.world.xlab.view.goodsDetail

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Color
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.adapter.recyclerView.GoodsDetailInfoAdapter
import xlab.world.xlab.adapter.recyclerView.GoodsDetailRatingAdapter
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.data.request.ReqUsedGoodsData
import xlab.world.xlab.data.response.ResGoodsDetailData
import xlab.world.xlab.server.provider.*
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.util.regex.Pattern

class GoodsDetailViewModel(private val apiGodo: ApiGodoProvider,
                           private val apiShop: ApiShopProvider,
                           private val apiPet: ApiPetProvider,
                           private val apiUser: ApiUserProvider,
                           private val apiUserActivity: ApiUserActivityProvider,
                           private val petInfo: PetInfo,
                           private val networkCheck: NetworkCheck,
                           private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "GoodsDetail"
    private val defaultDeliveryPrice = 2500
    private var goodsCode: String = ""
    private var goodsData: ResGoodsDetailData? = null
    private var hasTopicData: Boolean = false
    private val imageRegexPattern = Pattern.compile("\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]")

    val ratingOpenCloseEventData = SingleLiveEvent<RatingOpenCloseEvent>()
    val loadGoodsPetEventData = SingleLiveEvent<GoodsDetailEvent>()
    val goodsRatingEventData = SingleLiveEvent<GoodsRatingEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadGoodsDetailData(goodsCode: String, needDescription: Boolean) {
        this.goodsCode = goodsCode

        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiShop.requestGoodsDetail(scheduler = scheduler, goodsCode = goodsCode,
                    responseData = {
                        PrintLog.d("requestGoodsDetail success", it.toString(), tag)
                        goodsData = it.copy()
                        val buyBtnStr =
                                if (it.goods.isSoldOut) TextConstants.SOLD_OUT
                                else TextConstants.BUY
                        val goodsMainImages = arrayListOf(it.goods.mainImage)
                        val goodsPrice =
                                if (it.goods.price > 0) SupportData.applyPriceFormat(price = it.goods.price)
                                else it.goods.priceStr
                        val goodsPriceUnitVisibility = if (it.goods.price > 0) View.VISIBLE else View.GONE

                        var goodsDetailInfoData: GoodsDetailInfoData? = null
                        if (needDescription) {
                            goodsDetailInfoData = GoodsDetailInfoData()
                            // set extra info ex) 상품코드, 브랜드, 원산지...
                            val headerTitleList = arrayListOf("상품코드", "브랜드", "원산지", "제조사", "배송비")
                            val headerSubTitleList = arrayListOf(
                                    it.goods.no,
                                    it.goods.brandName,
                                    it.goods.originName,
                                    it.goods.makerName,
                                    SupportData.applyPriceFormat(price = defaultDeliveryPrice) + "원")
                            PrintLog.d("header Title List", headerTitleList.toString(), tag)
                            PrintLog.d("header SubTitle List", headerSubTitleList.toString(), tag)
                            headerTitleList.forEachIndexed { index, title ->
                                goodsDetailInfoData.items.add(GoodsDetailInfoListData(
                                        dataType = AppConstants.ADAPTER_HEADER,
                                        headerTitle = title,
                                        headerSubTitle = headerSubTitleList[index]))
                            }

                            // 상품 상세
                            PrintLog.d("goods description", it.goods.description, tag)
                            // 이미지 url parse
                            val detailMatcher = imageRegexPattern.matcher(it.goods.description)
                            val detailUrl = ArrayList<String>()
                            while (detailMatcher.find()) {
                                detailUrl.add(detailMatcher.group(0))
                            }
                            goodsDetailInfoData.items.add(GoodsDetailInfoListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    detailUrl = detailUrl,
                                    detailText = goodsData!!.goods.description))

                            // set footer
                            val footerTitleList = arrayOf("필수 표기 정보", "배송 • 교환 • 환불 안내", "제품 문의")
                            footerTitleList.forEachIndexed { index, title ->
                                goodsDetailInfoData.items.add(GoodsDetailInfoListData(
                                        dataType = AppConstants.ADAPTER_FOOTER,
                                        footerTitle = title,
                                        footerIndex = index,
                                        necessaryInfo = GoodsDetailInfoAdapter.NecessaryInfo(
                                                deliveryNo = it.goods.deliverySno.toString(),
                                                goodsName = it.goods.name,
                                                origin = it.goods.originName,
                                                maker = it.goods.makerName
                                        )
                                ))
                            }
                        }

                        uiData.value = UIModel(isLoading = false, buyBtnStr = buyBtnStr, buyBtnEnable = !it.goods.isSoldOut,
                                brandName = it.goods.brandName, brandCode = it.goods.brandCode, goodsMainImages = goodsMainImages,
                                goodsName = it.goods.name, goodsPrice = goodsPrice, goodsPriceUnitVisibility = goodsPriceUnitVisibility,
                                goodsDescriptionData = goodsDetailInfoData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGoodsDetail fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadGoodsPetData(authorization: String) {
        if (SupportData.isGuest(authorization = authorization)) { // 게스트
            loadGoodsPetEventData.postValue(GoodsDetailEvent(status = true))
            return
        }

        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.requestGoodsDetailPets(scheduler = scheduler, authorization = authorization, goodsCode = goodsCode,
                    responseData = {
                        PrintLog.d("requestGoodsDetailPets success", it.toString(), tag)
                        val topicMatchData = GoodsDetailTopicMatchData()
                        val goodsRatingData = GoodsDetailRatingData()

                        it.petsData?.forEach { pet ->
                            topicMatchData.items.add(GoodsDetailTopicMatchListData(
                                    petId = pet.id,
                                    petName = pet.name,
                                    imageURL = pet.image,
                                    matchingPercent = pet.match,
                                    matchColor = Color.parseColor("#${pet.topicColor}")
                            ))
                            goodsRatingData.items.add(GoodsDetailRatingListData(
                                    petId = pet.id,
                                    petType = pet.type,
                                    petBreed = pet.breed,
                                    petName = pet.name,
                                    topicColor = Color.parseColor("#${pet.topicColor}"),
                                    rating = pet.rating
                            ))
                        }
                        hasTopicData = topicMatchData.items.isNotEmpty()
                        uiData.value = UIModel(isLoading = false, topicMatchData = topicMatchData, goodsRatingData = goodsRatingData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGoodsDetailPets fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun ratingOpenClose(authorization: String, currentArrowRotation: Float) {
        launch {
            Observable.create<ArrayList<Any>> {
                if (SupportData.isGuest(authorization = authorization)) { // 게스트
                    ratingOpenCloseEventData.postValue(RatingOpenCloseEvent(isGuest = true))
                    it.onComplete()
                    return@create
                }

                if (!hasTopicData) { // 토픽 없음
                    PrintLog.d("ratingOpenClose", "no topic", tag)
                    ratingOpenCloseEventData.postValue(RatingOpenCloseEvent(noTopic = true))
                    it.onComplete()
                    return@create
                }

                val visibility =
                        if (currentArrowRotation == 0f) View.VISIBLE
                        else View.GONE

                val rotation =
                        if (currentArrowRotation == 0f) 180f
                        else 0f

                it.onNext(arrayListOf(visibility, rotation))
                it.onComplete()

            }.with(scheduler).subscribe {
                if (it.isNotEmpty()) {
                    uiData.value = UIModel(
                            ratingViewVisibility = it[0] as Int,
                            ratingArrowRotation = it[1] as Float)
                }
            }
        }
    }

    fun descriptionOpenClose(position: Int, goodsDescriptionData: GoodsDetailInfoListData) {
        launch {
            Observable.create<Int> {
                goodsDescriptionData.isShowAllDescription = !goodsDescriptionData.isShowAllDescription

                it.onNext(position)
                it.onComplete()
            }.with(scheduler).subscribe{
                uiData.value = UIModel(goodsDescriptionUpdateIndex = it)
            }
        }
    }

    fun loadGoodsStatsData(authorization: String, goodsCode: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiShop.requestGoodsStats(scheduler = scheduler, authorization = authorization, goodsCode = goodsCode,
                    responseData = {
                        PrintLog.d("requestGoodsStats success", it.toString(), tag)
                        val goodsStatsData = GoodsDetailStatsData()
                        it.statsList?.forEach { stats ->
                            val title =
                                    if (stats.title == "") {
                                        when (stats.topic.type) {
                                            petInfo.dogCode -> petInfo.dogBreedInfo[stats.topic.breed.toInt()].nameKor
                                            petInfo.catCode -> petInfo.catBreedInfo[stats.topic.breed.toInt()].nameKor
                                            else -> ""
                                        }
                                    } else stats.title
                            goodsStatsData.items.add(GoodsDetailStatsListData(
                                    title = title,
                                    topicImages = stats.topic.images,
                                    goodPercent = stats.good,
                                    sosoPercent = stats.soso,
                                    badPercent = stats.bad ))
                        }

                        uiData.value = UIModel(isLoading = false, goodsStatsData = goodsStatsData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGoodsStats fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadUsedUserData(goodsCode: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiUser.requestGoodsUsedUser(scheduler = scheduler, goodsCode = goodsCode, page = page,
                    responseData = {
                        PrintLog.d("requestGoodsUsedUser success", it.toString(), tag)
                        val goodsUsedUserData = GoodsDetailUsedUserData(total = it.total, nextPage = page + 1)
                        it.userData?.forEach { user ->
                            goodsUsedUserData.items.add(GoodsDetailUsedUserListData(
                                    userId = user.id,
                                    userNickname = user.nickName,
                                    userProfile = user.profileImg))
                        }
                        uiData.value = UIModel(isLoading = false, goodsUsedUserData = goodsUsedUserData,
                                goodsUsedUserTotal = if (page == 1) it.total else null)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGoodsUsedUser fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadTaggedPostsData(goodsCode: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiShop.requestGoodsTaggedPosts(scheduler = scheduler, goodsCode = goodsCode, page = 1,
                    responseData = {
                        PrintLog.d("requestGoodsTaggedPosts success", it.toString(), tag)
                        val taggedPostsData = PostThumbnailData()

                        it.postsData?.forEachIndexed postsData@ { index, post ->
                            if (index > 5) return@postsData
                            taggedPostsData.items.add(PostThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    postId = post.id,
                                    postType = post.postType,
                                    imageURL = post.postFile.firstOrNull(),
                                    youTubeVideoID = post.youTubeVideoID))
                        }
                        uiData.value = UIModel(isLoading = false, taggedPostsData = taggedPostsData, goodsUsedUserTotal = it.total,
                                postsMoreVisibility = if (it.total > 6) View.VISIBLE else View.GONE)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGoodsTaggedPosts fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun goodsRating(selectRatingData: GoodsDetailRatingAdapter.RatingTag,
                    goodsRatingData: GoodsDetailRatingListData, authorization: String) {
        if (selectRatingData.rating == goodsRatingData.rating) { // rating 취소
            goodsRatingEventData.postValue(GoodsRatingEvent(ratingCancelPosition = selectRatingData.position))
            return
        }

        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val reqUsedGoodsData = ReqUsedGoodsData(
                    usedType = AppConstants.FROM_RATING,
                    goodsCode = goodsCode,
                    goodsName = goodsData!!.goods.name,
                    goodsBrand = goodsData!!.goods.brandName,
                    goodsImage = goodsData!!.goods.mainImage,
                    goodsType = AppConstants.GOODS_PET,
                    topic = ReqUsedGoodsData.Topic(
                            id = goodsRatingData.petId,
                            type = goodsRatingData.petType,
                            breed = goodsRatingData.petBreed,
                            rating = selectRatingData.rating
                    ))
            apiUserActivity.requestPostUsedGoods(scheduler = scheduler, authorization = authorization, reqUsedGoodsData = reqUsedGoodsData,
                    responseData = {
                        PrintLog.d("requestPostUsedGoods success", it.toString(), tag)
                        goodsRatingData.rating = selectRatingData.rating

                        uiData.value = UIModel(isLoading = false, goodsRatingUpdateIndex = selectRatingData.position)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestPostUsedGoods fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun ratingCancel(position: Int, goodsRatingData: GoodsDetailRatingListData, authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiUserActivity.requestDeleteUsedGoods(scheduler = scheduler, authorization = authorization,
                    goodsCode = goodsCode, topicId = goodsRatingData.petId,
                    responseData = {
                        goodsRatingData.rating = AppConstants.GOODS_RATING_NONE
                        uiData.value = UIModel(isLoading = false, goodsRatingUpdateIndex = position)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestDeleteUsedGoods fail", errorData.message, tag)
                        }
                    })
        }
    }
}

data class GoodsRatingEvent(val ratingCancelPosition: Int? = null)
data class RatingOpenCloseEvent(val isGuest: Boolean? = null, val noTopic: Boolean? = null)
data class GoodsDetailEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val topicMatchData: GoodsDetailTopicMatchData? = null,
                   val goodsRatingData: GoodsDetailRatingData? = null, val goodsRatingUpdateIndex: Int? = null,
                   val ratingViewVisibility: Int? = null, val ratingArrowRotation: Float? = null,
                   val buyBtnStr: String? = null, val buyBtnEnable: Boolean? = null,
                   val brandName: String? = null, val brandCode: String? = null, val goodsMainImages: ArrayList<String>? = null,
                   val goodsName: String? = null, val goodsPrice: String? = null, val goodsPriceUnitVisibility: Int? = null,
                   val goodsDescriptionData: GoodsDetailInfoData? = null,
                   val goodsDescriptionUpdateIndex: Int? = null,
                   val goodsStatsData: GoodsDetailStatsData? = null,
                   val goodsUsedUserData: GoodsDetailUsedUserData? = null, val goodsUsedUserTotal: Int? = null,
                   val taggedPostsData: PostThumbnailData? = null, val taggedPostsTotal: Int? = null,
                   val postsMoreVisibility: Int? = null)
