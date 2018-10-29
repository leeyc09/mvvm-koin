package xlab.world.xlab.view.postDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_post_detail.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.PostDetailAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.listener.PostDetailListener
import xlab.world.xlab.utils.listener.UserDefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.ShareViewModel

class PostDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val postDetailViewModel: PostDetailViewModel by viewModel()
    private val shareViewModel: ShareViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var userDefaultListener: UserDefaultListener

    private lateinit var postDetailAdapter: PostDetailAdapter

    private lateinit var defaultListener: DefaultListener
    private lateinit var postDetailListener: PostDetailListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)

        postDetailViewModel.setResultCode(resultCode = resultCode)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.PROFILE, // 프로필
                    RequestCodeData.POST_COMMENT, // 댓글
                    RequestCodeData.POST_UPLOAD, // 포스트 수정
                    RequestCodeData.TAG_POST, // 포스트 태그
                    RequestCodeData.GOODS_DETAIL -> { // 상품 상세
                        postDetailViewModel.loadPostDetail(context = this, authorization = spHelper.authorization,
                                postId = intent.getStringExtra(IntentPassName.POST_ID), userId = spHelper.userId)
                    }
                }
            }
            ResultCodeData.LOGIN_SUCCESS -> { // login -> reload all data
                postDetailViewModel.loadPostDetail(context = this, authorization = spHelper.authorization,
                        postId = intent.getStringExtra(IntentPassName.POST_ID), userId = spHelper.userId)
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                actionBackBtn.performClick()
            }
        }
    }

    private fun onSetup() {
        actionBarTitle.visibility = View.GONE
        actionBtn.visibility = View.GONE

        // 바로 comment 화면으로 넘겨야 하는 경우
        if (intent.getBooleanExtra(IntentPassName.GO_COMMENT, false)) {
            RunActivity.postCommentActivity(context = this, postId = intent.getStringExtra(IntentPassName.POST_ID))
        }

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // listener 초기화
        defaultListener = DefaultListener(context = this)
        postDetailListener = PostDetailListener(context = this, fragmentManager = supportFragmentManager,
                postMoreEvent = { editPosition, deletePosition ->
                    editPosition?.let { _ ->
                        RunActivity.postUploadContentActivity(context = this,
                                postId = intent.getStringExtra(IntentPassName.POST_ID),
                                youTubeVideoId = postDetailAdapter.getItem(0).youTubeVideoID,
                                imagePathList = ArrayList())
                    }
                    deletePosition?.let { _ ->
                        postDetailViewModel.deletePost(authorization = spHelper.authorization, postId = intent.getStringExtra(IntentPassName.POST_ID))
                    }
                },
                likePostEvent = { position ->
                    postDetailViewModel.likePost(authorization = spHelper.authorization, selectIndex = position)
                },
                savePostEvent = { position ->
                    postDetailViewModel.savePost(context = this, authorization = spHelper.authorization, selectIndex = position)
                },
                postShareEvent = { shareType, position ->
                    when (shareType) {
                        AppConstants.ShareType.COPY_LINK -> {

                        }
                        AppConstants.ShareType.KAKAO -> {
                            postDetailViewModel.shareKakao(selectIndex = position)
                        }
                    }
                })
        userDefaultListener = UserDefaultListener(context = this,
                followUserEvent = { position ->
                    postDetailViewModel.userFollow(authorization = spHelper.authorization, selectIndex = position)
                })

        postDetailAdapter = PostDetailAdapter(context = this,
                changeViewTypeListener = null,
                profileListener = defaultListener.profileListener,
                followListener = userDefaultListener.followListener,
                moreListener = postDetailListener.postMoreListener,
                likePostListener = postDetailListener.likePostListener,
                commentsListener = defaultListener.commentsListener,
                savePostListener = postDetailListener.savePostListener,
                sharePostListener = postDetailListener.sharePostListener,
                hashTagListener = defaultListener.hashTagListener,
                goodsListener = defaultListener.goodsListener)
        recyclerView.adapter = postDetailAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        postDetailViewModel.loadPostDetail(context = this, authorization = spHelper.authorization,
                postId = intent.getStringExtra(IntentPassName.POST_ID), userId = spHelper.userId)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        postDetailViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                }
                uiData.resultCode?.let {
                    setResult(it)
                    finish()
                }
                uiData.postDetailData?.let {
                    postDetailAdapter.linkData(postDetailData = it)
                }
                uiData.postDetailUpdateIndex?.let {
                    postDetailViewModel.setResultCode(resultCode =  Activity.RESULT_OK)
                    postDetailAdapter.notifyItemChanged(it)
                }
            }
        })

        // load post detail 이벤트 observe
        postDetailViewModel.failLoadPostDetailData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isFail ->
                if (isFail) actionBackBtn.performClick()
            }
        })

        // post delete 이벤트 observe
        postDetailViewModel.postDeleteData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isSuccess ->
                if (isSuccess) {
                    postDetailViewModel.setResultCode(resultCode =  Activity.RESULT_OK)
                    actionBackBtn.performClick()
                }
            }
        })

        // post share kakao 이벤트 observe
        postDetailViewModel.shareKakaoData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { params ->
                shareViewModel.shareKakao(context = this, shareParams = params)
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    postDetailViewModel.backBtnAction()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, postId: String, goComment: Boolean): Intent {
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra(IntentPassName.POST_ID, postId)
            intent.putExtra(IntentPassName.GO_COMMENT, goComment)

            return intent
        }
    }
}
