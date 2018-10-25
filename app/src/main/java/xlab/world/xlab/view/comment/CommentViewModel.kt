package xlab.world.xlab.view.comment

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.data.adapter.CommentData
import xlab.world.xlab.data.adapter.CommentListData
import xlab.world.xlab.data.request.ReqCommentData
import xlab.world.xlab.server.provider.ApiCommentProvider
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class CommentViewModel(private val apiPost: ApiPostProvider,
                       private val apiComment: ApiCommentProvider,
                       private val networkCheck: NetworkCheck,
                       private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Comment"

    private var resultCode = Activity.RESULT_CANCELED
    private var isMyPost = false
    private var commentCnt = 0

    private var commentData: CommentData = CommentData()

    val loadCommentData = SingleLiveEvent<Boolean?>()
    val postCommentData = SingleLiveEvent<Boolean?>()
    val deleteCommentData = SingleLiveEvent<Boolean?>()
    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        this.resultCode = SupportData.setResultCode(oldResultCode = this.resultCode, newResultCode = resultCode)
    }

    fun loadCommentData(postId: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        launch {
            uiData.value = UIModel(isLoading = true)
            loadCommentData.value = true
            apiComment.getComment(scheduler = scheduler, postId = postId, page = page,
                    responseData = {
                        PrintLog.d("getComment success", it.toString(), viewModelTag)

                        val newCommentData = CommentData(total = it.total, nextPage = page + 1)
                        it.comments?.forEach { comment ->
                            newCommentData.items.add(CommentListData(
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

                        if (page == 1) {
                            this.commentCnt = newCommentData.total
                            this.commentData.updateData(commentData = newCommentData)
                            uiData.value = UIModel(isLoading = false, commentData = newCommentData,
                                    commentCnt = SupportData.countFormat(count = commentCnt))
                        } else {
                            this.commentData.addData(commentData = newCommentData)
                            uiData.value = UIModel(isLoading = false, commentDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("getComment fail", errorData.message, viewModelTag)
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
                        PrintLog.d("postComment success", "", viewModelTag)
                        postCommentData.value = true
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        postCommentData.value = false
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("postComment fail", errorData.message, viewModelTag)
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
                        PrintLog.d("deleteComment success", "", viewModelTag)
                        deleteCommentData.value = true
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("deleteComment fail", errorData.message, viewModelTag)
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

        uiData.value = UIModel(isLoading = true)
        launch {
            // 해당 포스트가 자신의 포스트인지 확인 후, 프로필 사진 넘겨줌
            apiPost.checkMyPost(scheduler = scheduler, authorization = authorization, postId = postId,
                    responseData = {
                        PrintLog.d("checkMyPost success", it.toString(), viewModelTag)
                        this.isMyPost = it.isMyPost
                        uiData.value = UIModel(isLoading = false, profileImg = it.profileImg,
                                commentPopupVisibility = if (SupportData.isGuest(authorization)) View.GONE else View.VISIBLE)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false,
                                commentPopupVisibility = View.GONE)
                        errorData?.let {
                            PrintLog.e("checkMyPost fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun commentLongClick(userId: String, selectIndex: Int) {
        launch {
            Observable.create<Int> {
                // 자신의 포스트 댓글, 자신의 댓글인 경우 삭제 가능
                if (this.isMyPost || this.commentData.items[selectIndex].userId == userId) {
                    it.onNext(selectIndex)
                    it.onComplete()
                }
            }.with(scheduler = scheduler).subscribe {
                uiData.value = UIModel(deleteCommentIndex = it)
            }
        }
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val commentPopupVisibility: Int? = null,
                   val commentData: CommentData? = null, val commentDataUpdate: Boolean? = null,
                   val commentCnt: String? = null, val deleteCommentIndex: Int? = null,
                   val profileImg: String? = null)