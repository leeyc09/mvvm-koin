package xlab.world.xlab.view.comment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.action_bar_title_with_number.*
import kotlinx.android.synthetic.main.activity_comment.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.CommentAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.listener.RecyclerViewTouchListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.TwoSelectBottomDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class CommentActivity : AppCompatActivity(), View.OnClickListener {
    private val commentViewModel: CommentViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var glideOption = RequestOptions()
            .circleCrop()
            .placeholder(R.drawable.profile_img_44)
            .error(R.drawable.profile_img_44)

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var commentDeleteDialog: TwoSelectBottomDialog

    private lateinit var commentAdapter: CommentAdapter

    private lateinit var defaultListener: DefaultListener

    private val recyclerViewTouchListener = object: RecyclerViewTouchListener.Listener {
        override fun onClick(view: View, position: Int) {
        }

        override fun onLongClick(view: View, position: Int) {
            commentViewModel.commentLongClick(userId = spHelper.userId, selectIndex = position)
        }
    }
    private val commentDeleteListener = object: TwoSelectBottomDialog.Listener {
        override fun onFirstBtnClick(tag: Any?) {
            commentViewModel.deleteComment(authorization = spHelper.authorization, postId = intent.getStringExtra(IntentPassName.POST_ID), position = tag as Int)
        }

        override fun onSecondBtnClick(tag: Any?) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

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

        commentViewModel.setResultCode(resultCode = resultCode)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.PROFILE -> { // 프로필
                        commentViewModel.loadCommentData(postId = intent.getStringExtra(IntentPassName.POST_ID), page = 1)
                    }
                }
            }
            ResultCodeData.LOGIN_SUCCESS -> { // login -> reload all data
                commentViewModel.loadCommentData(postId = intent.getStringExtra(IntentPassName.POST_ID), page = 1)
                commentViewModel.checkMyPost(authorization = spHelper.authorization, postId = intent.getStringExtra(IntentPassName.POST_ID))
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                actionBackBtn.performClick()
            }
        }
    }

    private fun onSetup() {
        // 타이틀 설정
        actionBarTitle.setText(getText(R.string.comment), TextView.BufferType.SPANNABLE)

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        commentDeleteDialog = DialogCreator.commentDeleteDialog(context = this, listener = commentDeleteListener)

        defaultListener = DefaultListener(context = this)

        // comment recycler view & adapter 초기화
        commentAdapter = CommentAdapter(context = this,
                profileListener = defaultListener.profileListener)
        recyclerView.adapter = commentAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, bottom = 28f))

        commentViewModel.loadCommentData(postId = intent. getStringExtra(IntentPassName.POST_ID), page = 1)
        commentViewModel.checkMyPost(authorization = spHelper.authorization, postId = intent.getStringExtra(IntentPassName.POST_ID))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        textViewCommentFinish.setOnClickListener(this) // 댓글 작성

        ViewFunction.onTextChange(editText = editTextComment) { comment ->
            textViewCommentFinish.isEnabled = comment.isNotEmpty()
        }

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = commentAdapter.dataLoading, total = commentAdapter.dataTotal) { _ ->
                commentViewModel.loadCommentData(postId = intent.getStringExtra(IntentPassName.POST_ID), page = commentAdapter.dataNextPage)
            }
        }

        recyclerView.addOnItemTouchListener(RecyclerViewTouchListener(context = this, recyclerView = recyclerView, clickListener = recyclerViewTouchListener))
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        commentViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.commentPopupVisibility?.let {
                    commentPopupLayout.visibility = it
                }
                uiData.commentData?.let {
                    commentAdapter.linkData(commentData = it)
                }
                uiData.commentDataUpdate?.let {
                    commentAdapter.notifyDataSetChanged()
                }
                uiData.commentCnt?.let {
                    actionBarNumber.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.deleteCommentIndex?.let {
                    commentDeleteDialog.showDialog(manager = supportFragmentManager, dialogTag = "commentDeleteDialog",
                            tagData = it)
                }
                uiData.profileImg?.let {
                    Glide.with(this)
                            .load(it)
                            .apply(glideOption)
                            .into(imageViewProfile)
                }
            }
        })

        // load comment 이벤트 observe
        commentViewModel.loadCommentData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isLoading ->
                commentAdapter.dataLoading = isLoading
            }
        })

        // post comment 이벤트 observe
        commentViewModel.postCommentData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isSuccess ->
                if (isSuccess) {
                    commentViewModel.setResultCode(resultCode = Activity.RESULT_OK)
                    commentViewModel.loadCommentData(postId = intent.getStringExtra(IntentPassName.POST_ID), page = 1)
                    editTextComment?.setText("")
                }
            }
        })

        // delete comment 이벤트 observe
        commentViewModel.deleteCommentData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isSuccess ->
                if (isSuccess) {
                    commentViewModel.setResultCode(resultCode = Activity.RESULT_OK)
                    commentViewModel.loadCommentData(postId = intent.getStringExtra(IntentPassName.POST_ID), page = 1)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    commentViewModel.backBtnAction()
                }
                R.id.textViewCommentFinish -> { // 댓글 작성
                    commentViewModel.postCommentData(authorization = spHelper.authorization, postId = intent.getStringExtra(IntentPassName.POST_ID), comment = getComment())
                }
            }
        }
    }

    private fun getComment(): String = editTextComment?.text.toString().trim()

    companion object {
        fun newIntent(context: Context, postId: String): Intent {
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra(IntentPassName.POST_ID, postId)

            return intent
        }
    }
}
