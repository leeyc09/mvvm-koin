package xlab.world.xlab.view.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_title_with_number.*
import kotlinx.android.synthetic.main.activity_goods_tagged_posts.*
import org.koin.android.architecture.ext.viewModel
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.PostThumbnailAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.button.ScrollUpButtonHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class GoodsTaggedPostsActivity : AppCompatActivity(), View.OnClickListener {
    private val searchViewModel: SearchViewModel by viewModel()

    private var resultCode = Activity.RESULT_CANCELED

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var scrollUpButtonHelper: ScrollUpButtonHelper

    private lateinit var postsAdapter: PostThumbnailAdapter

    private lateinit var defaultListener: DefaultListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_tagged_posts)

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
                        searchViewModel.searchGoodsTaggedPosts(goodsCode = intent.getStringExtra(IntentPassName.GOODS_CODE), page = 1)
                    }
                }
            }
            ResultCodeData.LOGIN_SUCCESS -> { // login -> reload all data
                this.resultCode = ResultCodeData.LOGIN_SUCCESS
                searchViewModel.searchGoodsTaggedPosts(goodsCode = intent.getStringExtra(IntentPassName.GOODS_CODE), page = 1)
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                setResult(ResultCodeData.LOGOUT_SUCCESS)
                finish()
            }
        }
    }

    private fun onSetup() {
        actionBarTitle.setText(getText(R.string.item_post_tag), TextView.BufferType.SPANNABLE)

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        defaultListener = DefaultListener(context = this)

        // scroll up button 초기화
        scrollUpButtonHelper = ScrollUpButtonHelper(
                smoothScroll = true,
                scrollUpBtn = scrollUpBtn)
        scrollUpButtonHelper.handle(recyclerView)

        // posts adapter & recycler 초기화
        postsAdapter = PostThumbnailAdapter(context = this,
                postListener = defaultListener.postListener,
                changeViewTypeListener = null)
        recyclerView.adapter = postsAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, offset = 0.5f))

        searchViewModel.searchGoodsTaggedPosts(goodsCode = intent.getStringExtra(IntentPassName.GOODS_CODE), page = 1)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as GridLayoutManager, isLoading = postsAdapter.dataLoading, total = postsAdapter.dataTotal) {_->
                searchViewModel.searchGoodsTaggedPosts(goodsCode = intent.getStringExtra(IntentPassName.GOODS_CODE), page = postsAdapter.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        searchViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.searchPostsData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        postsAdapter.updateData(postThumbnailData = it)
                        actionBarNumber.setText(it.total.toString(), TextView.BufferType.SPANNABLE)
                    }
                    else
                        postsAdapter.addData(postThumbnailData = it)
                }
            }
        })

        // search post 이벤트 observe
        searchViewModel.searchGoodsTaggedPostsEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.status?.let { isLoading ->
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
        fun newIntent(context: Context, goodsCode: String): Intent {
            val intent = Intent(context, GoodsTaggedPostsActivity::class.java)
            intent.putExtra(IntentPassName.GOODS_CODE, goodsCode)

            return intent
        }
    }
}
