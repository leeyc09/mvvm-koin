package xlab.world.xlab.view.profile

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.view.View
import com.kakao.message.template.FeedTemplate
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.data.request.ReqUserReportData
import xlab.world.xlab.server.provider.*
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class ProfileViewModel(private val apiUser: ApiUserProvider,
                       private val apiPet: ApiPetProvider,
                       private val apiFollow: ApiFollowProvider,
                       private val apiPost: ApiPostProvider,
                       private val apiUserActivity: ApiUserActivityProvider,
                       private val networkCheck: NetworkCheck,
                       private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Profile"

    private var resultCode = Activity.RESULT_CANCELED

    private var userData: UserData = UserData()
    private var topicData: ProfileTopicData = ProfileTopicData()
    private var usedGoodsData: GoodsThumbnailData = GoodsThumbnailData()

    val loadUserData = SingleLiveEvent<Boolean?>()
    val loadUserPetData = SingleLiveEvent<Boolean?>()
    val loadTopicUsedGoodsData = SingleLiveEvent<Boolean?>()
    val shareKakaoData = SingleLiveEvent<FeedTemplate?>()
    val btnActionData = SingleLiveEvent<BtnActionModel?>()
    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        this.resultCode =  SupportData.setResultCode(oldResultCode = this.resultCode, newResultCode = resultCode)

        when (resultCode) {
            ResultCodeData.LOGIN_SUCCESS,
            ResultCodeData.LOGOUT_SUCCESS -> {
                this.resultCode = resultCode
            }
            Activity.RESULT_OK,
            ResultCodeData.TOPIC_DELETE -> {
                if (this.resultCode == Activity.RESULT_CANCELED)
                    this.resultCode = Activity.RESULT_OK
            }
        }
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }

    fun uploadPostBtnAction(userLevel: Int) {
        // 유저 레벨이 admin 이면 post upload type 선택 다이얼로그 띄우기
        // 일반이면 바로 post upload 화면으로
        btnActionData.postValue(BtnActionModel(post = if (userLevel == AppConstants.ADMIN_USER_LEVEL) null else true,
                postTypeDialog = if (userLevel == AppConstants.ADMIN_USER_LEVEL) true else null))
    }

    // 프로필 타입 설정
    // 나의 프로필 & 상대방 프로필에 따라 action bar 달라짐
    fun setProfileType(userId: String, loginUserId: String, loadingBar: Boolean? = true) {
        uiData.value = UIModel(isLoading = loadingBar)
        launch {
            Observable.create<ArrayList<Int>> {
                // [0] - 나의 프로필 action bar 보이기 & 숨기기
                // [1] - 상대방 프로필 action bar 보이기 & 숨기기
                // [2] - follow 버튼 보이기 & 숨기기
                // [3] - 프로필 수정 버튼 보이기 & 숨기기
                val visibilityArrayList: ArrayList<Int> =
                        if (userId == loginUserId) arrayListOf(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE)
                        else arrayListOf(View.GONE, View.VISIBLE, View.VISIBLE, View.GONE)

                it.onNext(visibilityArrayList)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("setProfileType", it.toString(), viewModelTag)
                uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                        myProfileLayoutVisibility = it[0],
                        otherProfileLayoutVisibility = it[1],
                        followBtnVisibility = it[2],
                        editBtnVisibility = it[3])
            }
        }
    }

    // 해당 유저 토픽 정보 불러오기
    fun loadUserTopicData(userId: String, page: Int, topicDataCount: Int, loginUserId: String, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadUserPetData.value = true
        launch {
            apiPet.getUserPetList(scheduler = scheduler, userId = userId, page = page,
                    responseData = {
                        PrintLog.d("getUserPetList success", it.toString(), viewModelTag)
                        val newTopicData = ProfileTopicData(total = it.total, nextPage = page + 1)
                        it.petsData?.forEach { petData ->
                            newTopicData.items.add(ProfileTopicListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    topicId = petData.id,
                                    imageURL = petData.image))
                        }

                        val loadedTopicCount =
                                if (topicDataCount < 0) 0
                                else topicDataCount
                        val loadedTopicSize = loadedTopicCount + newTopicData.items.size

                        // 모든 topic 다 불러오고, 자신의 프로필 화면 -> 토픽 추가 버튼
                        if (loadedTopicSize >= newTopicData.total && userId == loginUserId)
                            newTopicData.items.add(ProfileTopicListData(dataType = AppConstants.ADAPTER_FOOTER))

                        if (page == 1) {
                            this.topicData.updateData(profileTopicData = newTopicData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, topicData = this.topicData)
                        } else {
                            this.topicData.addData(profileTopicData = newTopicData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, topicDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.e("getUserPetList fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    // 해당 유저 정보(프로필 이미지, 닉네임, 소개, 팔로잉 & 팔로워 수, 팔로잉 상태) 불러오기
    fun loadUserData(context: Context, authorization: String, userId: String, loginUserId: String, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        launch {
            apiUser.requestProfileMain(scheduler = scheduler, authorization = authorization, userId = userId,
                    responseData = {
                        PrintLog.d("requestProfileMain success", it.toString(), viewModelTag)
                        this.userData = UserData(id = userId, nickName = it.nickName,
                                introduction = it.introduction, profileImage = it.profileImg)
                        // 자신의 프로필일 경우 팔로워 버튼 활성화
                        // 다른 사람 프로필일 경우, 팔로워(팔로잉) 수가 0 이상일 경우 팔로워(팔로잉) 버튼 활성화
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                profileImage = it.profileImg,
                                nickName = it.nickName,
                                introduction = it.introduction,
                                followerCnt = SupportData.countFormat(count = it.followerCnt),
                                followerEnable = if (userId == loginUserId) true else it.followerCnt > 0,
                                followingCnt = SupportData.countFormat(count = it.followingCnt),
                                followingEnable = if (userId == loginUserId) true else it.followingCnt > 0,
                                followState = it.isFollowing)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, toastMessage = context.getString(R.string.toast_no_exist_user))
                        loadUserData.value = false
                        errorData?.let {
                            PrintLog.d("requestProfileMain fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun loadTopicUsedGoodsData(context: Context, userId: String, goodsType: Int, page: Int, loginUseId: String, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadTopicUsedGoodsData.value = true
        launch {
            apiUserActivity.requestTopicUsedGoods(scheduler = scheduler, userId = userId, goodsType = goodsType, page = page,
                    responseData = {
                        PrintLog.d("requestTopicUsedGoods success", it.toString(), viewModelTag)
                        val topicUsedGoodsData = GoodsThumbnailData(total = it.total, nextPage = page + 1)
                        it.goods?.forEach { goods ->
                            topicUsedGoodsData.items.add(GoodsThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    goodsImage = goods.image,
                                    goodsCd = goods.code
                            ))
                        }

                        if (page == 1) {
                            // 첫 페이지 & used goods 존재 -> 헤더 설정
                            if (topicUsedGoodsData.items.isNotEmpty()) {
                                topicUsedGoodsData.items.add(0, GoodsThumbnailListData(
                                        dataType = AppConstants.ADAPTER_HEADER,
                                        headerTitle = context.getString(R.string.goods)))
                            }
                            this.usedGoodsData.updateData(goodsThumbnailData = topicUsedGoodsData)
                            val emptyGoods = this.usedGoodsData.items.isEmpty()

                            val myGoodsVisibility = if (userId == loginUseId && emptyGoods) View.VISIBLE else View.GONE
                            val otherGoodsVisibility = if (userId != loginUseId && emptyGoods) View.VISIBLE else View.GONE
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    topicUsedGoodsData = this.usedGoodsData,
                                    usedGoodsVisibility = VisibilityData(myGoods = myGoodsVisibility, otherGoods = otherGoodsVisibility))
                        } else {
                            this.usedGoodsData.addData(goodsThumbnailData = topicUsedGoodsData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    topicUsedGoodsDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.e("requestTopicUsedGoods fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun userFollow(authorization: String, userId: String) {
        if (SupportData.isGuest(authorization = authorization)) {
            uiData.postValue(UIModel(isGuest = true))
            return
        }

        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiFollow.requestFollow(scheduler = scheduler, authorization = authorization, userId = userId,
                    responseData = {
                        PrintLog.d("follow success", it.status.toString(), viewModelTag)
                        uiData.value = UIModel(isLoading = false, followState = it.status)
                        setResultCode(resultCode = Activity.RESULT_OK)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("follow fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun shareKakao() {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        launch {
            Observable.create<FeedTemplate> {
                val params = ShareContent.kakaoProfileShareBuild(
                        title = userData.nickName,
                        description = userData.introduction,
                        profileImage = userData.profileImage,
                        userId = userData.id)

                it.onNext(params)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                shareKakaoData.value = it
            }
        }
    }

    fun userReport(context: Context, authorization: String, respondentId: String) {
        if (SupportData.isGuest(authorization = authorization)) {
            uiData.postValue(UIModel(isGuest = true))
            return
        }
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val reportData = ReqUserReportData(respondentID = respondentId)

            apiUser.requestUserReport(scheduler = scheduler, authorization = authorization, reqUserReportData = reportData,
                    responseData = {
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_success_report))
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestUserReport fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }
}

data class VisibilityData(val myGoods: Int, val otherGoods: Int)
data class BtnActionModel(val post: Boolean? = null, val postTypeDialog: Boolean? = null)
data class UserData(val id: String = "", val nickName: String = "",
                    val introduction: String = "", val profileImage: String = "")
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val isGuest: Boolean? = null, val myProfileLayoutVisibility: Int? = null, val otherProfileLayoutVisibility: Int? = null,
                   val followBtnVisibility: Int? = null, val editBtnVisibility: Int? = null,
                   val profileImage: String? = null, val nickName: String? = null, val introduction: String? = null,
                   val followerCnt: String? = null, val followerEnable: Boolean? = null,
                   val followingCnt: String? = null, val followingEnable: Boolean? = null,
                   val followState: Boolean? = null,
                   val topicData: ProfileTopicData? = null, val topicDataUpdate: Boolean? = null,
                   val topicUsedGoodsData: GoodsThumbnailData? = null, val topicUsedGoodsDataUpdate: Boolean? = null,
                   val usedGoodsVisibility: VisibilityData? = null,
                   val postsThumbData: PostThumbnailData? = null)
