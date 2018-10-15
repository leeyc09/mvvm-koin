package xlab.world.xlab.view.notice

import android.arch.lifecycle.MutableLiveData
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
    val tag = "Notice"

    val loadNoticeventData = SingleLiveEvent<NoticeEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadNoticeData(authorization: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadNoticeventData.postValue(NoticeEvent(status = true))
        launch {
            apiNotice.requestNoticeData(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        PrintLog.d("requestNoticeData success", it.toString(), tag)
                        val noticeData = NoticeData(total = it.total, nextPage = page + 1)
                        it.notices?.forEach { notice ->
                            noticeData.items.add(NoticeListData(
                                    isRead = notice.isRead,
                                    noticeId = notice.id,
                                    title = notice.title,
                                    content = notice.content,
                                    date = SupportData.contentDateForm(year = notice.year, month = notice.month, day = notice.day)))
                        }
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, noticeData = noticeData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("requestNoticeData fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun readNotice(authorization: String, noticeListData: NoticeListData, position: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiNotice.updateReadNotice(scheduler = scheduler, authorization = authorization, noticeId = noticeListData.noticeId,
                    responseData = {
                        noticeListData.isRead = true
                        noticeListData.isSelect = !noticeListData.isSelect
                        uiData.value = UIModel(isLoading = false, noticeUpdatePosition = position)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("updateReadNotice fail", errorData.message, tag)
                        }
                    })
        }
    }

}

data class NoticeEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val noticeData: NoticeData? = null, val noticeUpdatePosition: Int? = null)
