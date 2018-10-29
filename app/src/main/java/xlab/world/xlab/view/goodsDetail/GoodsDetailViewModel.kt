package xlab.world.xlab.view.goodsDetail

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GoodsDetailInfoAdapter
import xlab.world.xlab.adapter.recyclerView.GoodsDetailRatingAdapter
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.data.request.ReqRecentViewGoodsData
import xlab.world.xlab.data.request.ReqUsedGoodsData
import xlab.world.xlab.data.response.ResGoodsDetailData
import xlab.world.xlab.server.provider.*
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.span.FontForegroundColorSpan
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
    private val viewModelTag = "GoodsDetail"

    private var resultCode = Activity.RESULT_CANCELED

    private var isPostViewGoods: Boolean = false
    private val defaultDeliveryPrice = 2500
    private var goodsCode: String = ""
    private var goodsData: ResGoodsDetailData? = null
    private val imageRegexPattern = Pattern.compile("\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]")

    private var goodsDetailTopicMatchData: GoodsDetailTopicMatchData = GoodsDetailTopicMatchData()
    private var goodsDetailRatingData: GoodsDetailRatingData = GoodsDetailRatingData()
    private var goodsDetailInfoData: GoodsDetailInfoData = GoodsDetailInfoData()
    private var goodsDetailStatsData: GoodsDetailStatsData = GoodsDetailStatsData()
    private var goodsDetailUsedUserData: GoodsDetailUsedUserData = GoodsDetailUsedUserData()

    val recentViewGoodsData = SingleLiveEvent<ReqRecentViewGoodsData?>()
    val ratingOpenCloseData = SingleLiveEvent<Boolean?>()
    val goodsRatingData = SingleLiveEvent<Int?>()
    val buyNowEventData = SingleLiveEvent<BuyNowModel>()
    val loadUsedUserData = SingleLiveEvent<Boolean?>()
    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        this.resultCode = SupportData.setResultCode(oldResultCode = this.resultCode, newResultCode = resultCode)
    }

    fun postRecentViewGoods(authorization: String, recentViewGoods: ReqRecentViewGoodsData) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiShop.requestPostRecentViewGoods(scheduler = scheduler, authorization = authorization, recentViewGoodsData = recentViewGoods,
                    responseData = {
                        isPostViewGoods = true
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestGoodsDetail fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun loadGoodsDetailData(context: Context, goodsCode: String, needDescription: Boolean) {
        this.goodsCode = goodsCode

        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiShop.requestGoodsDetail(scheduler = scheduler, goodsCode = goodsCode,
                    responseData = {
                        PrintLog.d("requestGoodsDetail success", it.toString(), viewModelTag)
                        goodsData = it.copy()
                        val buyBtnStr =
                                if (it.goods.isSoldOut) context.getString(R.string.sold_out)
                                else context.getString(R.string.buy)
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
                            PrintLog.d("header SubTitle List", headerSubTitleList.toString(), viewModelTag)
                            headerTitleList.forEachIndexed { index, title ->
                                goodsDetailInfoData.items.add(GoodsDetailInfoListData(
                                        dataType = AppConstants.ADAPTER_HEADER,
                                        headerTitle = title,
                                        headerSubTitle = headerSubTitleList[index]))
                            }

                            // 상품 상세
                            PrintLog.d("goods description", it.goods.description, viewModelTag)
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
                            this.goodsDetailInfoData.updateData(goodsDetailInfoData = goodsDetailInfoData)
                        }

                        uiData.value = UIModel(isLoading = false, buyBtnStr = buyBtnStr, buyBtnEnable = !it.goods.isSoldOut,
                                brandName = it.goods.brandName, brandCode = it.goods.brandCode, goodsMainImages = goodsMainImages,
                                goodsName = it.goods.name, goodsPrice = goodsPrice, goodsPriceUnitVisibility = goodsPriceUnitVisibility,
                                goodsDescriptionData = goodsDetailInfoData?.let{_->this.goodsDetailInfoData})

                        // 최근 본 상품에 추가한 적 없는 경우 -> 추가
                        if (!isPostViewGoods) {
                            recentViewGoodsData.value = ReqRecentViewGoodsData(code = it.goods.code, image = it.goods.mainImage)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestGoodsDetail fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun loadGoodsPetData(authorization: String) {
        if (SupportData.isGuest(authorization = authorization)) { // 게스트
            uiData.postValue(UIModel(topicMatchVisibility = View.GONE,
                    ratingViewVisibility = View.GONE,
                    ratingArrowRotation = 0f))
            return
        }

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
                        val newTopicMatchData = GoodsDetailTopicMatchData()
                        val newGoodsRatingData = GoodsDetailRatingData()

                        it.petsData?.forEach { pet ->
                            newTopicMatchData.items.add(GoodsDetailTopicMatchListData(
                                    petId = pet.id,
                                    petName = pet.name,
                                    imageURL = pet.image,
                                    matchingPercent = pet.match,
                                    matchColor = Color.parseColor("#${pet.topicColor}")
                            ))
                            newGoodsRatingData.items.add(GoodsDetailRatingListData(
                                    petId = pet.id,
                                    petType = pet.type,
                                    petBreed = pet.breed,
                                    petName = pet.name,
                                    topicColor = Color.parseColor("#${pet.topicColor}"),
                                    rating = pet.rating
                            ))
                        }

                        this.goodsDetailTopicMatchData.updateData(goodsDetailTopicMatchData = newTopicMatchData)
                        this.goodsDetailRatingData.updateData(goodsDetailRatingData = newGoodsRatingData)

                        uiData.value = UIModel(isLoading = false,
                                topicMatchData = this.goodsDetailTopicMatchData,
                                topicMatchVisibility = if (this.goodsDetailTopicMatchData.items.isEmpty()) View.GONE else View.VISIBLE,
                                goodsRatingData = this.goodsDetailRatingData,
                                ratingViewVisibility = if (this.goodsDetailRatingData.items.isEmpty()) View.GONE else View.VISIBLE,
                                ratingArrowRotation = if (this.goodsDetailRatingData.items.isEmpty()) 0f else 180f)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestGoodsDetailPets fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun ratingOpenClose(authorization: String, currentArrowRotation: Float) {
        launch {
            Observable.create<ArrayList<Any>> {
                if (SupportData.isGuest(authorization = authorization)) { // 게스트
                    uiData.postValue(UIModel(isGuest = true))
                    it.onComplete()
                    return@create
                }

                if (this.goodsDetailTopicMatchData.items.isEmpty()) { // 토픽 없음
                    PrintLog.d("ratingOpenClose", "no topic", viewModelTag)
                    ratingOpenCloseData.postValue(true)
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

            }.with(scheduler = scheduler).subscribe {
                if (it.isNotEmpty()) {
                    uiData.value = UIModel(
                            ratingViewVisibility = it[0] as Int,
                            ratingArrowRotation = it[1] as Float)
                }
            }
        }
    }

    fun descriptionOpenClose(selectIndex: Int) {
        launch {
            Observable.create<Int> {
                val goodsDescriptionData = this.goodsDetailInfoData.items[selectIndex]
                goodsDescriptionData.isShowAllDescription = !goodsDescriptionData.isShowAllDescription

                it.onNext(selectIndex)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe{
                uiData.value = UIModel(goodsDescriptionUpdateIndex = it)
            }
        }
    }

    fun loadGoodsStatsData(authorization: String, goodsCode: String,
                           boldFont: FontForegroundColorSpan, regularFont: FontForegroundColorSpan) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiShop.requestGoodsStats(scheduler = scheduler, authorization = authorization, goodsCode = goodsCode,
                    responseData = {
                        PrintLog.d("requestGoodsStats success", it.toString(), viewModelTag)
                        val goodsStatsData = GoodsDetailStatsData()
                        it.statsList?.forEach { stats ->
                            // 텍스트에서 하이라이트(진한 검정색) 될 텍스트
                            val highlightStr =
                                    if (stats.title.isEmpty()) {
                                        when (stats.topic.type) {
                                            petInfo.dogCode -> petInfo.dogBreedInfo[stats.topic.breed.toInt()].nameKor
                                            petInfo.catCode -> petInfo.catBreedInfo[stats.topic.breed.toInt()].nameKor
                                            else -> ""
                                        }
                                    } else stats.title

                            val textStr = SpannableString(if (stats.title.isEmpty()) "다른 ${highlightStr}의 만족도" else "$highlightStr 만족도")

                            // 텍스트에서 하이라이트(진한 검정색) 적용
                            if (stats.title.isEmpty()) {
                                textStr.setSpan(regularFont, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                textStr.setSpan(boldFont, 3, 3 + highlightStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                textStr.setSpan(regularFont, 3 + highlightStr.length, textStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            } else {
                                textStr.setSpan(boldFont, 0, highlightStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                textStr.setSpan(regularFont, highlightStr.length, textStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }

                            goodsStatsData.items.add(GoodsDetailStatsListData(
                                    title = textStr,
                                    topicImages = stats.topic.images,
                                    goodPercent = stats.good,
                                    sosoPercent = stats.soso,
                                    badPercent = stats.bad ))
                        }
                        this.goodsDetailStatsData.updateData(goodsDetailStatsData = goodsStatsData)

                        uiData.value = UIModel(isLoading = false,
                                goodsStatsData = this.goodsDetailStatsData,
                                noStatsVisibility = if (this.goodsDetailStatsData.items.isEmpty()) View.VISIBLE else View.GONE)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGoodsStats fail", errorData.message)
                        }
                    })
        }
    }

    fun loadUsedUserData(goodsCode: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadUsedUserData.value = true
        launch {
            apiUser.requestGoodsUsedUser(scheduler = scheduler, goodsCode = goodsCode, page = page,
                    responseData = {
                        PrintLog.d("requestGoodsUsedUser success", it.toString(), viewModelTag)
                        val goodsUsedUserData = GoodsDetailUsedUserData(total = it.total, nextPage = page + 1)
                        it.userData?.forEach { user ->
                            goodsUsedUserData.items.add(GoodsDetailUsedUserListData(
                                    userId = user.id,
                                    userNickname = user.nickName,
                                    userProfile = user.profileImg))
                        }

                        if (page == 1) {
                            this.goodsDetailUsedUserData.updateData(goodsDetailUsedUserData = goodsUsedUserData)
                            uiData.value = UIModel(isLoading = false,
                                    goodsUsedUserData = this.goodsDetailUsedUserData,
                                    goodsUsedUserTotal = if (page == 1) it.total else null)
                        } else {
                            this.goodsDetailUsedUserData.addData(goodsDetailUsedUserData = goodsUsedUserData)
                            uiData.value = UIModel(isLoading = false,
                                    goodsUsedUserDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestGoodsUsedUser fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun goodsRating(selectRatingData: GoodsDetailRatingAdapter.RatingTag, authorization: String) {
        val ratingData = this.goodsDetailRatingData.items[selectRatingData.position]

        if (selectRatingData.rating == ratingData.rating) { // rating 취소
            goodsRatingData.postValue(selectRatingData.position)
            return
        }

        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
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
                            id = ratingData.petId,
                            type = ratingData.petType,
                            breed = ratingData.petBreed,
                            rating = selectRatingData.rating
                    ))
            apiUserActivity.requestPostUsedGoods(scheduler = scheduler, authorization = authorization, reqUsedGoodsData = reqUsedGoodsData,
                    responseData = {
                        PrintLog.d("requestPostUsedGoods success", it.toString(), viewModelTag)
                        ratingData.rating = selectRatingData.rating

                        uiData.value = UIModel(isLoading = false, goodsRatingUpdateIndex = selectRatingData.position)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestPostUsedGoods fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun ratingCancel(selectIndex: Int, authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val goodsRatingData = this.goodsDetailRatingData.items[selectIndex]
            apiUserActivity.requestDeleteUsedGoods(scheduler = scheduler, authorization = authorization,
                    goodsCode = goodsCode, topicId = goodsRatingData.petId,
                    responseData = {
                        goodsRatingData.rating = AppConstants.GOODS_RATING_NONE
                        uiData.value = UIModel(isLoading = false, goodsRatingUpdateIndex = selectIndex)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestDeleteUsedGoods fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun buyButtonAction(authorization: String) {
        launch {
            Observable.create<Int> {
                if (SupportData.isGuest(authorization = authorization)) { // 게스트
                    uiData.postValue(UIModel(isGuest = true))
                    it.onComplete()
                    return@create
                }

                it.onNext(goodsData!!.goods.price)
                it.onComplete()

            }.with(scheduler = scheduler).subscribe {
                uiData.value = UIModel(buyOptionDialogShow = it)
            }
        }
    }

    fun addCart(authorization: String, count: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestAddCart(scheduler = scheduler, authorization = authorization, goodsNo = goodsData!!.goods.no, count = count,
                    responseData = {
                        PrintLog.d("requestAddCart success", it.toString())
                        uiData.value = UIModel(isLoading = false, cartToastShow = true)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestAddCart fail", errorData.message)
                        }
                    })
        }
    }

    fun buyNow(authorization: String, count: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestAddCart(scheduler = scheduler, authorization = authorization, goodsNo = goodsData!!.goods.no, count = count,
                    responseData = {
                        uiData.value = UIModel(isLoading = false)
                        buyNowEventData.value = BuyNowModel(sno = it.sno)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestAddCart fail", errorData.message)
                        }
                    })
        }
    }
}

data class BuyNowModel(val sno: Int? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val cartToastShow: Boolean? = null,
                   val isGuest: Boolean? = null, val buyOptionDialogShow: Int? = null,
                   val topicMatchData: GoodsDetailTopicMatchData? = null, val topicMatchVisibility: Int? = null,
                   val goodsRatingData: GoodsDetailRatingData? = null, val goodsRatingUpdateIndex: Int? = null,
                   val ratingViewVisibility: Int? = null, val ratingArrowRotation: Float? = null,
                   val buyBtnStr: String? = null, val buyBtnEnable: Boolean? = null,
                   val brandName: String? = null, val brandCode: String? = null, val goodsMainImages: ArrayList<String>? = null,
                   val goodsName: String? = null, val goodsPrice: String? = null, val goodsPriceUnitVisibility: Int? = null,
                   val goodsDescriptionData: GoodsDetailInfoData? = null,
                   val goodsDescriptionUpdateIndex: Int? = null,
                   val goodsStatsData: GoodsDetailStatsData? = null, val noStatsVisibility: Int? = null,
                   val goodsUsedUserData: GoodsDetailUsedUserData? = null, val goodsUsedUserDataUpdate: Boolean? = null, val goodsUsedUserTotal: Int? = null,
                   val taggedPostsData: PostThumbnailData? = null, val taggedPostsTotal: Int? = null,
                   val postsMoreVisibility: Int? = null)
