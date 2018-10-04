package xlab.world.xlab.view.search.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_combined_search.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.UserDefaultAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.listener.UserDefaultListener
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.button.ScrollUpButtonHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.follow.FollowViewModel
import xlab.world.xlab.view.search.SearchViewModel

class CombinedSearchUserFragment: Fragment() {
    private val searchViewModel: SearchViewModel by viewModel()
    private val followViewModel: FollowViewModel by viewModel()
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

    private var searchUserAdapter: UserDefaultAdapter? = null

    private var defaultListener: DefaultListener? = null
    private var userDefaultListener: UserDefaultListener? = null

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
        userDefaultListener = UserDefaultListener(context = context as Activity,
                followUserEvent = { position ->
                    followViewModel.userFollow(authorization = spHelper.authorization, position = position,
                            userData = searchUserAdapter?.getItem(position = position), recommendUserData = null)
                })

        // scroll up button 초기화
        scrollUpButtonHelper = ScrollUpButtonHelper(
                smoothScroll = true,
                scrollUpBtn = scrollUpBtn)
        scrollUpButtonHelper.handle(recyclerView)

        // search user adapter & recycler view 초기화
        searchUserAdapter = searchUserAdapter ?: UserDefaultAdapter(context = context!!,
                followListener = userDefaultListener!!.followListener,
                profileListener = defaultListener!!.profileListener)
        recyclerView.adapter = searchUserAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        if (recyclerView.itemDecorationCount < 1)
            recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, bottom = 10f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        if (needInitData)
            searchUserData(searchText = this.searchText, loadingBar = true)
    }

    private fun onBindEvent() {
        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = searchUserAdapter!!.dataLoading, total = searchUserAdapter!!.dataTotal) { _ ->
                searchViewModel.searchUsers(authorization = spHelper.authorization, searchText = searchText, page = searchUserAdapter!!.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // TODO: Search View Model 이벤트
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
                uiData.searchUserData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        setBundleVisibilityData(noSearchDataVisibility =
                        if (it.items.isEmpty()) View.VISIBLE
                        else View.GONE)
                        searchUserAdapter?.updateData(userDefaultData = it)
                    }
                    else
                        searchUserAdapter?.addData(userDefaultData = it)
                }
//                uiData.scrollUpBtnVisibility?.let {
//                    scrollUpBtn.visibility = it
//                }
            }
        })

        // search user 이벤트 observe
        searchViewModel.searchUserEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.status?.let { isLoading ->
                    searchUserAdapter?.dataLoading = isLoading
                    needInitData = false
                }
            }
        })

        // TODO: Follow View Model 이벤트
        // UI 이벤트 observe
        followViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.userUpdatePosition?.let {
//                    if (this.resultCode == Activity.RESULT_CANCELED)
//                        this.resultCode = Activity.RESULT_OK
                    searchUserAdapter?.notifyItemChanged(it)
                }
            }
        })
    }

    fun searchUserData(searchText: String, loadingBar: Boolean?) {
        this.searchText = searchText
        context?.let {
            searchViewModel.searchUsers(authorization = spHelper.authorization, searchText = this.searchText, page = 1, loadingBar = loadingBar)
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
        fun newFragment(): CombinedSearchUserFragment {
            val fragment = CombinedSearchUserFragment()

            val args = Bundle()
            args.putBoolean("needInitData", false)
            args.putInt("noSearchDataVisibility", View.GONE)
            fragment.arguments = args

            return fragment
        }
    }
}