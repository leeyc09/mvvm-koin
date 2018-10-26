package xlab.world.xlab.view.notification

import android.arch.lifecycle.MutableLiveData
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.data.adapter.ShopNotificationData
import xlab.world.xlab.data.adapter.SocialNotificationData
import xlab.world.xlab.data.adapter.SocialNotificationListData
import xlab.world.xlab.server.provider.ApiNotificationProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.ResultCodeData
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class NotificationViewModel(private val apiNotification: ApiNotificationProvider,
                            private val networkCheck: NetworkCheck,
                            private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Notification"

    private var resultCode = ResultCodeData.LOAD_OLD_DATA

    val loadNotificationEventData = SingleLiveEvent<Boolean?>()
    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        this.resultCode = SupportData.setResultCode(oldResultCode = this.resultCode, newResultCode = resultCode)
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }

    fun loadExistNewNotification(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        launch {
            apiNotification.requestNewNotification(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestNewNotification success", it.toString(), viewModelTag)
                        uiData.value = UIModel(newNotificationDotVisibility = if (it.existNew) View.VISIBLE else View.GONE)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(newNotificationDotVisibility = View.GONE)
                        errorData?.let {
                            PrintLog.e("requestNewNotification fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun loadSocialNotificationData(authorization: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadNotificationEventData.value = true
        launch {
            apiNotification.requestSocialNotification(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        val socialNotificationData = SocialNotificationData(total = it.total, nextPage = page + 1)
                        it.notification?.forEach { notification ->
                            socialNotificationData.items.add(SocialNotificationListData(
                                    type = notification.type,
                                    userId = notification.fromUserId,
                                    userProfileUrl = notification.fromUserProfile,
                                    userNick = notification.fromUserNick,
                                    postId = notification.postId,
                                    postType = notification.postType,
                                    postImageUrl = notification.postImage,
                                    youTubeVideoID = notification.youTubeVideoID,
                                    commentContent = notification.comment,
                                    year = notification.year,
                                    month = notification.month,
                                    day = notification.day,
                                    hour = notification.hour,
                                    minute = notification.minute
                            ))
                        }

                        uiData.value = UIModel(isLoading = false,
                                socialNotificationData = socialNotificationData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestSocialNotification fail", errorData.message)
                        }
                    })
        }
    }

    fun loadShopNotificationData(authorization: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadNotificationEventData.value = true
        launch {
            Observable.create<ShopNotificationData> {
                val shopNotificationData = ShopNotificationData(total = 0, nextPage = page + 1)

                it.onNext(shopNotificationData)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                uiData.value = UIModel(isLoading = false, shopNotificationData = it)
            }
        }
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val socialNotificationData: SocialNotificationData? = null,
                   val shopNotificationData: ShopNotificationData? = null,
                   val newNotificationDotVisibility: Int? = null)
