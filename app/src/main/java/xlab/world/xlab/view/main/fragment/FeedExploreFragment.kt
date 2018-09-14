package xlab.world.xlab.view.main.fragment

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.swipe_recycler_view.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.ExploreFeedAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.main.MainViewModel

class FeedExploreFragment: Fragment() {
    private val mainViewModel: MainViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private var exploreFeedAdapter: ExploreFeedAdapter? = null

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var defaultListener: DefaultListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.swipe_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        // Toast, Dialog 초기화
        defaultToast = defaultToast ?: DefaultToast(context = context!!)
        progressDialog = progressDialog ?: DefaultProgressDialog(context = context!!)

        defaultListener = defaultListener ?: DefaultListener(context = context as Activity)

        // explore feed recycler view & adapter 초기화
        exploreFeedAdapter = exploreFeedAdapter ?: ExploreFeedAdapter(context = context!!,
                postListener = defaultListener!!.postListener,
                goodsListener = defaultListener!!.goodsListener)
        recyclerView.adapter = exploreFeedAdapter
        val gridLayoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when(exploreFeedAdapter!!.getItemViewType(position = position)) {
                    AppConstants.FEED_GOODS,
                    AppConstants.FEED_POST-> 1
                    AppConstants.FEED_AD -> 3
                    else -> 1
                }
            }
        }
        recyclerView.layoutManager = gridLayoutManager
        if (recyclerView.itemDecorationCount < 1)
            recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, offset = 0.5f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        if (needInitData)
            reloadFeedData()
    }

    private fun onBindEvent() {
        swipeRefreshLayout.setOnRefreshListener {
            reloadFeedData(loadingBar = null)
        }

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as GridLayoutManager, isLoading = exploreFeedAdapter!!.dataLoading, total = exploreFeedAdapter!!.dataTotal) { _ ->
                mainViewModel.loadExploreFeedData(authorization = spHelper.authorization, page = exploreFeedAdapter!!.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        mainViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog!!.isShowing)
                        progressDialog!!.show()
                    else if (!it && progressDialog!!.isShowing)
                        progressDialog!!.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast?.showToast(message = it)
                }
                uiData.exploreFeedData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        exploreFeedAdapter?.updateData(exploreFeedData = it)
                        swipeRefreshLayout.isRefreshing = false
                    }
                    else
                        exploreFeedAdapter?.addData(exploreFeedData = it)

                    if (exploreFeedAdapter!!.itemCount < 18) {
                        mainViewModel.loadExploreFeedData(authorization = spHelper.authorization, page = exploreFeedAdapter!!.dataNextPage)
                    }
                }
            }
        })

        // load explore feed 이벤트 observe
        mainViewModel.loadExploreFeedDataEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadExploreFeedDataEvent ->
            loadExploreFeedDataEvent?.let { _ ->
                loadExploreFeedDataEvent.isLoading?.let {
                    exploreFeedAdapter?.dataLoading = it
                    needInitData = false
                }
            }
        })
    }

    fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }

    fun reloadFeedData(loadingBar: Boolean? = true) {
        context?.let {
            mainViewModel.loadExploreFeedData(authorization = spHelper.authorization, page = 1, loadingBar = loadingBar)
        } ?:let { needInitData = true }
    }

    companion object {
        fun newFragment(): FeedExploreFragment {
            val fragment = FeedExploreFragment()

            val args = Bundle()
            args.putBoolean("needInitData", true)
            fragment.arguments = args

            return fragment
        }
    }
}