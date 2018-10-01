package xlab.world.xlab.view.posts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_saved_posts.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.PostThumbnailAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class SavedPostsActivity : AppCompatActivity(), View.OnClickListener {
    private val postsViewModel: PostsViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var resultCode = Activity.RESULT_CANCELED

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var defaultListener: DefaultListener

    private lateinit var postsAdapter: PostThumbnailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_posts)

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

        when (resultCode) {
            Activity.RESULT_OK -> {
                if (this.resultCode == Activity.RESULT_CANCELED)
                    this.resultCode = Activity.RESULT_OK
                when (requestCode) {
                    RequestCodeData.POST_DETAIL -> { // 포스트 상세
                        postsViewModel.loadLikedPostsData(authorization = spHelper.authorization, page = 1)
                    }
                }
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                setResult(ResultCodeData.LOGOUT_SUCCESS)
                finish()
            }
        }
    }

    private fun onSetup() {
        actionBarTitle.setText(getText(R.string.saved_post), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        defaultListener = DefaultListener(context = this)

        postsAdapter = PostThumbnailAdapter(context = this, changeViewTypeListener = null,
                postListener = defaultListener.postListener)
        recyclerView.adapter = postsAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, offset = 0.5f))

        postsViewModel.loadSavedPostsData(authorization = spHelper.authorization, page = 1)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as GridLayoutManager, isLoading = postsAdapter.dataLoading, total = postsAdapter.dataTotal) { _ ->
                postsViewModel.loadSavedPostsData(authorization = spHelper.authorization, page = postsAdapter.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        postsViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.postsData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        // post 없으면 no post 띄우기
                        textViewEmptyPost.visibility =
                                if (it.items.isEmpty()) View.VISIBLE
                                else View.GONE
                        postsAdapter.updateData(postThumbnailData = it)
                    }
                    else
                        postsAdapter.addData(postThumbnailData = it)
                }
            }
        })

        // load like posts 이벤트 observe
        postsViewModel.loadPostsEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { loadPostsEventData ->
            loadPostsEventData?.let { _->
                loadPostsEventData.status?.let { isLoading ->
                    postsAdapter.dataLoading = isLoading
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    setResult(resultCode)
                    finish()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SavedPostsActivity::class.java)
        }
    }
}
