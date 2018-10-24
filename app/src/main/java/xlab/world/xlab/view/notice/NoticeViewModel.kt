package xlab.world.xlab.view.notice

import android.arch.lifecycle.MutableLiveData
import android.view.View
import xlab.world.xlab.data.adapter.NoticeData
import xlab.world.xlab.data.adapter.NoticeListData
import xlab.world.xlab.server.provider.ApiNoticeProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class NoticeViewModel(private val apiNotice: ApiNoticeProvider,
                      private val networkCheck: NetworkCheck,
                      private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Notice"

    private var noticeData: NoticeData = NoticeData()

    val loadNoticeData = SingleLiveEvent<Boolean?>()
    val uiData = MutableLiveData<UIModel>()

    fun loadExistNewNotification(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        launch {
            apiNotice.requestNewNotice(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestNewNotice success", it.toString(), viewModelTag)
                        uiData.value = UIModel(newNoticeDotVisibility = if (it.existNew) View.VISIBLE else View.GONE)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(newNoticeDotVisibility = View.GONE)
                        errorData?.let {
                            PrintLog.e("requestNewNotice fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun loadNoticeData(authorization: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadNoticeData.value = true
        launch {
            apiNotice.requestNoticeData(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        PrintLog.d("requestNoticeData success", it.toString(), viewModelTag)
                        val newNoticeData = NoticeData(total = it.total, nextPage = page + 1)
                        it.notices?.forEach { notice ->
                            newNoticeData.items.add(NoticeListData(
                                    isRead = notice.isRead,
                                    noticeId = notice.id,
                                    title = notice.title,
                                    content = notice.content,
                                    date = SupportData.contentDateForm(year = notice.year, month = notice.month, day = notice.day)))
                        }

                        if (page == 1) {
                            this.noticeData.updateData(noticeData = newNoticeData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, noticeData = this.noticeData,
                                    emptyNoticeVisibility = if (this.noticeData.items.isEmpty()) View.VISIBLE else View.GONE)
                        } else {
                            this.noticeData.addData(noticeData = newNoticeData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, noticeDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.e("requestNoticeData fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun readNotice(authorization: String, selectIndex: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val noticeListData = this.noticeData.items[selectIndex]
            apiNotice.updateReadNotice(scheduler = scheduler, authorization = authorization, noticeId = noticeListData.noticeId,
                    responseData = {
                        noticeListData.isRead = true
                        noticeListData.isSelect = !noticeListData.isSelect
                        uiData.value = UIModel(isLoading = false, noticeUpdateIndex = selectIndex)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("updateReadNotice fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val emptyNoticeVisibility: Int? = null,
                   val noticeData: NoticeData? = null, val noticeDataUpdate: Boolean? = null, val noticeUpdateIndex: Int? = null,
                   val newNoticeDotVisibility: Int? = null)
