package xlab.world.xlab.view.comment

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.data.adapter.CommentData
import xlab.world.xlab.data.adapter.CommentListData
import xlab.world.xlab.data.request.ReqCommentData
import xlab.world.xlab.server.provider.ApiCommentProvider
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class CommentViewModel(private val apiPost: ApiPostProvider,
                       private val apiComment: ApiCommentProvider,
                       private val networkCheck: NetworkCheck,
                       private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Comment"

    val loadCommentDataEvent = SingleLiveEvent<CommentEvent>()
    val postCommentDataEvent = SingleLiveEvent<CommentEvent>()
    val deleteCommentDataEvent = SingleLiveEvent<CommentEvent>()
    val checkMyPostEvent = SingleLiveEvent<CommentEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadCommentData(postId: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadCommentDataEvent.value = CommentEvent(status = true)
        launch {
            apiComment.getComment(scheduler = scheduler, postId = postId, page = page,
                    responseData = {
                        val commentData = CommentData(total = it.total, nextPage = page + 1)
                        it.comments?.forEach { comment ->
                            commentData.items.add(CommentListData(
                                    userId = comment.userId,
                                    userProfileUrl = comment.profileImg,
                                    userNickName = comment.nickName,
                                    comment = comment.content,
                                    uploadYear = comment.uploadYear,
                                    uploadMonth = comment.uploadMonth,
                                    uploadDay = comment.uploadDay,
                                    uploadHour = comment.uploadHour,
                                    uploadMinute = comment.uploadMinute
                            ))
                        }
                        PrintLog.d("getComment success", commentData.toString(), tag)
                        uiData.value = UIModel(isLoading = false, commentData = commentData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("getComment fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun postCommentData(authorization: String, postId: String, comment: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val reqCommentData = ReqCommentData()
            reqCommentData.addContent(comment)
            apiComment.postComment(scheduler = scheduler, authorization = authorization, postId = postId, requestBody = reqCommentData.getReqBody(),
                    responseData = {
                        PrintLog.d("postComment success", "", tag)
                        postCommentDataEvent.postValue(CommentEvent(status = true))
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        postCommentDataEvent.postValue(CommentEvent(status = false))
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("postComment fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun deleteComment(authorization: String, postId: String, position: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiComment.deleteComment(scheduler = scheduler, authorization = authorization, postId = postId, position = position,
                    responseData = {
                        PrintLog.d("deleteComment success", "", tag)
                        deleteCommentDataEvent.postValue(CommentEvent(status = true))
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("deleteComment fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun checkMyPost(authorization: String, postId: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        // login 확인
        if (SupportData.isGuest(authorization)) { // guest
            uiData.postValue(UIModel(isGuest = true))
            return
        }

        uiData.value = UIModel(isLoading = true, isGuest = false)
        launch {
            apiPost.checkMyPost(scheduler = scheduler, authorization = authorization, postId = postId,
                    responseData = {
                        PrintLog.d("checkMyPost success", it.toString(), tag)
                        uiData.value = UIModel(isLoading = false, profileImg = it.profileImg)
                        checkMyPostEvent.postValue(CommentEvent(status = it.isMyPost))
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("checkMyPost fail", errorData.message, tag)
                        }
                    })
        }
    }
}

data class CommentEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val isGuest: Boolean? = null,
                   val commentData: CommentData? = null,
                   val profileImg: String? = null)