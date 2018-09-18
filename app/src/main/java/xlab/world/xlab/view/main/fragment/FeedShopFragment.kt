package xlab.world.xlab.view.main.fragment

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import kotlinx.android.synthetic.main.swipe_recycler_view.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.ShopFeedAdapter
import xlab.world.xlab.data.request.ReqSearchGoodsData
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.main.MainViewModel

class FeedShopFragment: Fragment() {
    private val mainViewModel: MainViewModel by viewModel()
    private val spHelper: SPHelper by inject()

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

    private var shopFeedAdapter: ShopFeedAdapter? = null

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var defaultListener: DefaultListener? = null
    private var searchEditorActionListener: TextView.OnEditorActionListener? = null
    private var categoryListener: View.OnClickListener? = null

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
        searchEditorActionListener = searchEditorActionListener ?: TextView.OnEditorActionListener { v, actionID, _ ->
            //If the key event is a key-down event on the "putKeyCode" button
            var handled = false
            val text = v.text.toString().trim()
            if (actionID == EditorInfo.IME_ACTION_DONE && text.isNotEmpty()) {
                RunActivity.searchGoodsActivity(context = context as Activity, searchText = text, searchCode = "")
                handled = true
            }
            handled
        }
        categoryListener = categoryListener ?: View.OnClickListener { view ->
            if (view.tag is ReqSearchGoodsData) {
                val searchData = view.tag as ReqSearchGoodsData
                RunActivity.searchGoodsActivity(context = context as Activity, searchText = searchData.text, searchCode = searchData.code)
            }
        }

        // shop feed recycler view & adapter 초기화
        shopFeedAdapter = shopFeedAdapter ?: ShopFeedAdapter(context = context!!,
                searchEditorActionListener = searchEditorActionListener!!,
                categoryListener = categoryListener!!,
                goodsListener = defaultListener!!.goodsListener,
                questionListener = defaultListener!!.questionMatchListener,
                matchVisible = matchVisibility)
        recyclerView.adapter = shopFeedAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        if (needInitData)
            reloadFeedData(loadingBar = true)
        else
            matchVisibleChange(matchVisibility)
    }

    private fun onBindEvent() {
        swipeRefreshLayout.setOnRefreshListener {
            reloadFeedData(loadingBar = null)
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
                uiData.shopFeedData?.let {
                    shopFeedAdapter?.updateData(shopFeedData = it)
                    swipeRefreshLayout.isRefreshing = false
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
            shopFeedAdapter?.changeMatchVisible(visibility)
        }
    }

    fun reloadFeedData(loadingBar: Boolean?) {
        context?.let {
            mainViewModel.loadShopFeedData(authorization = spHelper.authorization, topicColorList = resources.getStringArray(R.array.topicColorStringList), loadingBar = loadingBar)
        } ?:let { needInitData = true }
    }

    companion object {
        fun newFragment(): FeedShopFragment {
            val fragment = FeedShopFragment()

            val args = Bundle()
            args.putBoolean("needInitData", true)
            args.putInt("matchVisibility", View.VISIBLE)
            fragment.arguments = args

            return fragment
        }
    }
}