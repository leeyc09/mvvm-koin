package xlab.world.xlab.view.search.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_combined_search.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.PostThumbnailAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.button.ScrollUpButtonHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.search.SearchViewModel

class CombinedSearchPostFragment: Fragment() {
    private val searchViewModel: SearchViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: false
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }
    private var searchText
        get() = arguments?.getString("searchText") ?: ""
        set(value) {
            arguments?.putString("searchText", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private lateinit var scrollUpButtonHelper: ScrollUpButtonHelper

    private var searchPostAdapter: PostThumbnailAdapter? = null

    private var defaultListener: DefaultListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_combined_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        matchBtnLayout.visibility = View.GONE
        // Toast, Dialog 초기화
        defaultToast = defaultToast ?: DefaultToast(context = context!!)
        progressDialog = progressDialog ?: DefaultProgressDialog(context = context!!)

        defaultListener = defaultListener ?: DefaultListener(context = context as Activity)

        // scroll up button 초기화
        scrollUpButtonHelper = ScrollUpButtonHelper(
                smoothScroll = true,
                scrollUpBtn = scrollUpBtn)
        scrollUpButtonHelper.handle(recyclerView)

        // search post adapter & recycler view 초기화
        searchPostAdapter = searchPostAdapter ?: PostThumbnailAdapter(context = context!!,
                changeViewTypeListener = null,
                postListener = defaultListener!!.postListener)
        recyclerView.adapter = searchPostAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        if (recyclerView.itemDecorationCount < 1)
            recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, offset = 0.5f))

        if (needInitData)
            searchPostsData(searchText = this.searchText, loadingBar = true)
    }

    private fun onBindEvent() {
        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as GridLayoutManager, isLoading = searchPostAdapter!!.dataLoading, total = searchPostAdapter!!.dataTotal) { _ ->
                searchViewModel.searchPosts(searchText = searchText, page = searchPostAdapter!!.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        searchViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.searchPostsData?.let {
                    searchPostAdapter?.linkData(postThumbnailData = it)
                }
                uiData.searchPostsDataUpdate?.let {
                    searchPostAdapter?.notifyDataSetChanged()
                }
                uiData.noSearchDataVisibility?.let {
                    setBundleVisibilityData(noSearchDataVisibility = it)
                }
            }
        })

        // search post 이벤트 observe
        searchViewModel.searchPostsEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.status?.let { isLoading ->
                    searchPostAdapter?.dataLoading = isLoading
                    needInitData = false
                }
            }
        })
    }

    fun searchPostsData(searchText: String, loadingBar: Boolean?) {
        this.searchText = searchText
        context?.let {
            searchViewModel.searchPosts(searchText = this.searchText, page = 1, loadingBar = loadingBar)
        } ?:let { needInitData = true }
    }

    private fun setLayoutVisibility() {
        textViewNoSearchData?.visibility = getBundleNoSearchDataVisibility()
    }

    private fun getBundleNoSearchDataVisibility(): Int = arguments?.getInt("noSearchDataVisibility") ?: View.GONE

    private fun setBundleVisibilityData(noSearchDataVisibility: Int) {
        arguments?.putInt("noSearchDataVisibility", noSearchDataVisibility)

        setLayoutVisibility()
    }

    companion object {
        fun newFragment(): CombinedSearchPostFragment {
            val fragment = CombinedSearchPostFragment()

            val args = Bundle()
            args.putBoolean("needInitData", false)
            args.putInt("noSearchDataVisibility", View.GONE)
            fragment.arguments = args

            return fragment
        }
    }
}