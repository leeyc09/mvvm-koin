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
import xlab.world.xlab.adapter.recyclerView.AllFeedAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.main.MainViewModel

class FeedAllFragment: Fragment() {
    private val mainViewModel: MainViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var noProgressDialog = false
    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }
    private var matchVisibility
        get() = arguments?.getInt("matchVisibility") ?: View.VISIBLE
        set(value) {
            arguments?.putInt("matchVisibility", value)
        }

    private var allFeedAdapter: AllFeedAdapter? = null

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

        // all feed recycler view & adapter 초기화
        allFeedAdapter = allFeedAdapter ?: AllFeedAdapter(context = context!!,
                postListener = defaultListener!!.postListener,
                goodsListener = defaultListener!!.goodsListener,
                questionListener = defaultListener!!.questionMatchListener,
                matchVisible = matchVisibility)
        recyclerView.adapter = allFeedAdapter
        val gridLayoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when(allFeedAdapter!!.getItemViewType(position = position)) {
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
            mainViewModel.loadAllFeedData(authorization = spHelper.authorization, page = 1, topicColorList = resources.getStringArray(R.array.topicColorStringList))
        else
            matchVisibleChange(matchVisibility)
    }

    private fun onBindEvent() {
        swipeRefreshLayout.setOnRefreshListener {
            noProgressDialog = true
            mainViewModel.loadAllFeedData(authorization = spHelper.authorization, page = 1, topicColorList = resources.getStringArray(R.array.topicColorStringList))
        }

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it, isLoading = allFeedAdapter!!.dataLoading, total = allFeedAdapter!!.dataTotal) { _ ->
                mainViewModel.loadAllFeedData(authorization = spHelper.authorization, page = allFeedAdapter!!.dataNextPage, topicColorList = resources.getStringArray(R.array.topicColorStringList))
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        mainViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (!noProgressDialog) {
                        if (it && !progressDialog!!.isShowing)
                            progressDialog!!.show()
                        else if (!it && progressDialog!!.isShowing)
                            progressDialog!!.dismiss()
                    }
                }
                uiData.toastMessage?.let {
                    defaultToast?.showToast(message = it)
                }
                uiData.allFeedData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        allFeedAdapter?.updateData(allFeedData = it)
                        swipeRefreshLayout.isRefreshing = false
                    }
                    else
                        allFeedAdapter?.addData(allFeedData = it)

                    if (allFeedAdapter!!.itemCount < 18) {
                        mainViewModel.loadAllFeedData(authorization = spHelper.authorization, page = allFeedAdapter!!.dataNextPage, topicColorList = resources.getStringArray(R.array.topicColorStringList))
                    }

                    if (noProgressDialog)
                        noProgressDialog = false
                }
            }
        })

        // load all feed 이벤트 observe
        mainViewModel.loadAllFeedDataEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadAllFeedDataEvent ->
            loadAllFeedDataEvent?.let { _ ->
                loadAllFeedDataEvent.isLoading?.let {
                    allFeedAdapter?.dataLoading = it
                    needInitData = false
                }
            }
        })
    }

    fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }

    fun matchVisibleChange(visibility: Int) {
        matchVisibility = visibility
        context?.let {
            allFeedAdapter?.changeMatchVisible(visibility)
        }
    }

    fun reloadFeedData() {
        context?.let {
            mainViewModel.loadAllFeedData(authorization = spHelper.authorization, page = 1, topicColorList = resources.getStringArray(R.array.topicColorStringList))
        } ?:let { needInitData = true }
    }

    companion object {
        fun newFragment(): FeedAllFragment {
            val fragment = FeedAllFragment()

            val args = Bundle()
            args.putBoolean("needInitData", true)
            args.putInt("matchVisibility", View.VISIBLE)
            fragment.arguments = args

            return fragment
        }
    }
}