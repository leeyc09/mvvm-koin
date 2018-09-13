package xlab.world.xlab.view.main.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_feed_following.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.PostDetailAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.main.MainViewModel

class FeedFollowingFragment: Fragment(), View.OnClickListener {
    private val mainViewModel: MainViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var noProgressDialog = false
    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private var followingFeedAdapter: PostDetailAdapter? = null

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var defaultListener: DefaultListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feed_following, container, false)
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

        // following feed recycler view & adapter 초기화
        followingFeedAdapter = followingFeedAdapter ?: PostDetailAdapter(context = context!!,
                changeViewTypeListener = null,
                profileListener = defaultListener!!.profileListener,
                followListener = null,
                moreListener = null,
                likePostListener = View.OnClickListener {  },
                commentsListener = defaultListener!!.commentsListener,
                savePostListener = View.OnClickListener {  },
                sharePostListener = View.OnClickListener {  },
                hashTagListener = defaultListener!!.hashTagListener,
                goodsListener = defaultListener!!.goodsListener)
        recyclerView.adapter = followingFeedAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        if (needInitData)
            mainViewModel.loadFollowingFeedData(authorization = spHelper.authorization, page = 1)
        else
            setLayoutVisibility()
    }

    private fun onBindEvent() {
        noFollowLayout.setOnClickListener(this) // 팔로우 하기
        noLoginLayout.setOnClickListener(this) // 로그인 하기

        swipeRefreshLayout.setOnRefreshListener {
            noProgressDialog = true
            mainViewModel.loadFollowingFeedData(authorization = spHelper.authorization, page = 1)
        }

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it, isLoading = followingFeedAdapter!!.dataLoading, total = followingFeedAdapter!!.dataTotal) { _ ->
                mainViewModel.loadFollowingFeedData(authorization = spHelper.authorization, page = followingFeedAdapter!!.dataNextPage)
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
                uiData.followingFeedData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        // post 없으면 no post 띄우기
                        setBundleVisibilityData(listVisibility =
                        if (it.items.isEmpty()) View.GONE
                        else View.VISIBLE,
                                noFollowVisibility = View.GONE,
                                noLoginVisibility = View.GONE,
                                noPostVisibility =
                                if (it.items.isEmpty()) View.VISIBLE
                                else View.GONE)
                        followingFeedAdapter?.updateData(postDetailData = it)
                    }
                    else
                        followingFeedAdapter?.addData(postDetailData = it)
                }
                uiData.guestMode?.let {
                    setBundleVisibilityData(listVisibility = View.GONE,
                            noFollowVisibility = View.GONE,
                            noLoginVisibility = View.VISIBLE,
                            noPostVisibility = View.GONE)
                }
                uiData.noFollowing?.let {
                    setBundleVisibilityData(listVisibility = View.GONE,
                            noFollowVisibility = View.VISIBLE,
                            noLoginVisibility = View.GONE,
                            noPostVisibility = View.GONE)
                }
            }
        })

        // load following feed 이벤트 observe
        mainViewModel.loadFollowingFeedDataEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadFollowingFeedDataEvent ->
            loadFollowingFeedDataEvent?.let { _ ->
                loadFollowingFeedDataEvent.isLoading?.let {
                    followingFeedAdapter?.dataLoading = it
                    swipeRefreshLayout.isRefreshing = false
                    noProgressDialog = false
                    needInitData = false
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.noFollowLayout -> { // 팔로우 하기
//                    val intent = RecommendUserActivity.newIntent(activity)
//                    activity.startActivityForResult(intent, RequestCodeData.PROFILE)
                }
                R.id.noLoginLayout -> { // 로그인 하기
                    RunActivity.loginActivity(context = context as Activity, isComePreLoadActivity = false, linkData = null)
                }
            }
        }
    }

    fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }

    fun reloadFeedData() {
        context?.let {
            mainViewModel.loadFollowingFeedData(authorization = spHelper.authorization, page = 1)
        } ?:let { needInitData = true }
    }

    private fun setLayoutVisibility() {
        recyclerView?.visibility = getBundleListVisibility()
        noFollowLayout?.visibility = getBundleNoFollowVisibility()
        noLoginLayout?.visibility = getBundleNoLoginVisibility()
        noPostLayout?.visibility = getBundleNoPostVisibility()
    }

    private fun getBundleListVisibility(): Int = arguments?.getInt("listVisibility") ?: View.VISIBLE
    private fun getBundleNoFollowVisibility(): Int = arguments?.getInt("noFollowVisibility") ?: View.GONE
    private fun getBundleNoLoginVisibility(): Int = arguments?.getInt("noLoginVisibility") ?: View.GONE
    private fun getBundleNoPostVisibility(): Int = arguments?.getInt("noPostVisibility") ?: View.GONE

    private fun setBundleVisibilityData(listVisibility: Int, noFollowVisibility: Int, noLoginVisibility: Int, noPostVisibility: Int) {
        arguments?.putInt("listVisibility", listVisibility)
        arguments?.putInt("noFollowVisibility", noFollowVisibility)
        arguments?.putInt("noLoginVisibility", noLoginVisibility)
        arguments?.putInt("noPostVisibility", noPostVisibility)

        setLayoutVisibility()
    }

    companion object {
        fun newFragment(): FeedFollowingFragment {
            val fragment = FeedFollowingFragment()

            val args = Bundle()
            args.putBoolean("needInitData", true)
            args.putInt("listVisibility", View.VISIBLE)
            args.putInt("noFollowVisibility", View.GONE)
            args.putInt("noLoginVisibility", View.GONE)
            args.putInt("noPostVisibility", View.GONE)

            fragment.arguments = args

            return fragment
        }
    }
}