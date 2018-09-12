package xlab.world.xlab.view.postDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
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
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.TwoSelectBottomDialog
import xlab.world.xlab.utils.view.toast.DefaultToast

class PostDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val postDetailViewModel: PostDetailViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var resultCode = Activity.RESULT_CANCELED
    private var postId = ""

    private lateinit var postDetailAdapter: PostDetailAdapter

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

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

    private fun onSetup() {
        actionBarTitle.visibility = View.GONE
        actionBtn.visibility = View.GONE

        postId = intent.getStringExtra(IntentPassName.POST_ID)
        // 바로 comment 화면으로 넘겨야 하는 경우
        if (intent.getBooleanExtra(IntentPassName.GO_COMMENT, false)) {
            RunActivity.postCommentActivity(context = this, postId = postId)
        }

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        defaultListener = DefaultListener(context = this)
        postDetailListener = PostDetailListener(context = this, isLogin = spHelper.accessToken.isNotEmpty(), fragmentManager = supportFragmentManager,
                postMoreEvent = { edit, delete ->
                    edit?.let {
                        PrintLog.d("postMore", "post edit", "PostDetail")
                    }
                    delete?.let {
                        postDetailViewModel.deletePost(authorization = spHelper.authorization, postId = postId)
                    }
                },
                likePostEvent = { position ->
                    PrintLog.d("postMore", "post like position: $position", "PostDetail")
                    val item = postDetailAdapter.getItem(position)
                    PrintLog.d("postMore", "post like status: ${item.isLike}", "PostDetail")
                },
                savePostEvent = { position ->
                    PrintLog.d("postMore", "post save position: $position", "PostDetail")
                    val item = postDetailAdapter.getItem(position)
                    PrintLog.d("postMore", "post save status: ${item.isSave}", "PostDetail")
                },
                followUserEvent = { position ->
                    PrintLog.d("postMore", "post user follow position: $position", "PostDetail")
                    val item = postDetailAdapter.getItem(position)
                    PrintLog.d("postMore", "post follow status: ${item.isFollowing}", "PostDetail")
                })

        postDetailAdapter = PostDetailAdapter(context = this,
                changeViewTypeListener = null,
                profileListener = defaultListener.profileListener,
                followListener = postDetailListener.followListener,
                moreListener = postDetailListener.postMoreListener,
                likePostListener = postDetailListener.likePostListener,
                commentsListener = defaultListener.commentsListener,
                savePostListener = postDetailListener.savePostListener,
                sharePostListener = View.OnClickListener {  },
                hashTagListener = defaultListener.hashTagListener,
                goodsListener = defaultListener.goodsListener)
        recyclerView.adapter = postDetailAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        postDetailViewModel.loadPostDetail(authorization = spHelper.authorization, postId = postId, userId = spHelper.userId)
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
                uiData.postDetailData?.let {
                    postDetailAdapter.updateData(it)
                }
            }
        })

        // load post detail 이벤트 observe
        postDetailViewModel.loadPostDetailEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadPostDetailEvent ->
            loadPostDetailEvent?.let { _ ->
                loadPostDetailEvent.isFail?.let {
                    if (it)
                        actionBackBtn.performClick()
                }
            }
        })

        // post delete 이벤트 observe
        postDetailViewModel.postDeleteEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { postDeleteEvent ->
            postDeleteEvent?.let { _ ->
                postDeleteEvent.isSuccess?.let {
                    if (it) {
                        if (resultCode == Activity.RESULT_CANCELED)
                            resultCode = Activity.RESULT_OK
                        actionBackBtn.performClick()
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
//                    when (resultCode) {
//                        ResultCodeData.LOGIN_SUCCESS,
//                        Activity.RESULT_OK-> {
//                            setResult(resultCode)
//                        }
//                        else -> {
//                            if (postDetailListener.isChange)
//                                setResult(Activity.RESULT_OK)
//                            else
//                                setResult(Activity.RESULT_CANCELED)
//                        }
//                    }
                    setResult(resultCode)
                    finish()
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
