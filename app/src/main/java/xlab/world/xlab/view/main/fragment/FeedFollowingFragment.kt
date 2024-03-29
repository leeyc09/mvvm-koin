package xlab.world.xlab.view.main.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
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
import xlab.world.xlab.utils.listener.PostDetailListener
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.viewModel.ShareViewModel
import xlab.world.xlab.view.main.MainViewModel
import xlab.world.xlab.view.postDetail.PostDetailViewModel

class FeedFollowingFragment: Fragment(), View.OnClickListener {
    private val mainViewModel: MainViewModel by viewModel()
    private val postDetailViewModel: PostDetailViewModel by viewModel()
    private val shareViewModel: ShareViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private var followingFeedAdapter: PostDetailAdapter? = null

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var defaultListener: DefaultListener? = null
    private var postDetailListener: PostDetailListener? = null

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
        postDetailListener = postDetailListener ?: PostDetailListener(context = context as AppCompatActivity, fragmentManager = (context as AppCompatActivity).supportFragmentManager,
                postMoreEvent = { _, _ ->},
                likePostEvent = { position ->
                    postDetailViewModel.likePost(authorization = spHelper.authorization, selectIndex = position)
                },
                savePostEvent = { position ->
                    postDetailViewModel.savePost(context = context!!, authorization = spHelper.authorization, selectIndex = position)
                },
                postShareEvent = { shareType, position ->
                    when (shareType) {
                        AppConstants.ShareType.COPY_LINK -> {

                        }
                        AppConstants.ShareType.KAKAO -> {
                            postDetailViewModel.shareKakao(selectIndex = position)
                        }
                    }
                })

        // following feed recycler view & adapter 초기화
        followingFeedAdapter = followingFeedAdapter ?: PostDetailAdapter(context = context!!,
                changeViewTypeListener = null,
                profileListener = defaultListener!!.profileListener,
                followListener = null,
                moreListener = null,
                likePostListener = postDetailListener!!.likePostListener,
                commentsListener = defaultListener!!.commentsListener,
                savePostListener = postDetailListener!!.savePostListener,
                sharePostListener = postDetailListener!!.sharePostListener,
                hashTagListener = defaultListener!!.hashTagListener,
                goodsListener = defaultListener!!.goodsListener)
        recyclerView.adapter = followingFeedAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        if (needInitData)
            reloadFeedData(loadingBar = true)
        else
            setLayoutVisibility()
    }

    private fun onBindEvent() {
        noFollowLayout.setOnClickListener(this) // 팔로우 하기
        noLoginLayout.setOnClickListener(this) // 로그인 하기

        swipeRefreshLayout.setOnRefreshListener {
            reloadFeedData(loadingBar = null)
        }

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = followingFeedAdapter!!.dataLoading, total = followingFeedAdapter!!.dataTotal) { _ ->
                postDetailViewModel.loadFollowingFeedData(authorization = spHelper.authorization, page = followingFeedAdapter!!.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // TODO: main view model
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
            }
        })

        // TODO: post detail view model
        // UI 이벤트 observe
        postDetailViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog!!.isShowing)
                        progressDialog!!.show()
                    else if (!it && progressDialog!!.isShowing)
                        progressDialog!!.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast!!.showToast(message = it)
                }
                uiData.postDetailData?.let {
                    followingFeedAdapter?.linkData(postDetailData = it)
                    swipeRefreshLayout.isRefreshing = false
                }
                uiData.postDetailDataUpdate?.let {
                    followingFeedAdapter?.notifyDataSetChanged()
                }
                uiData.postDetailUpdateIndex?.let {
                    followingFeedAdapter?.notifyItemChanged(it)
                }
                uiData.noPostVisibility?.let {
                    // post 없으면 no post 띄우기
                    setBundleVisibilityData(noFollowVisibility = View.GONE,
                            noLoginVisibility = View.GONE,
                            noPostVisibility = it)
                }
                uiData.noFollowingVisibility?.let {
                    swipeRefreshLayout.isRefreshing = false
                    setBundleVisibilityData(noFollowVisibility = it,
                            noLoginVisibility = View.GONE,
                            noPostVisibility = View.GONE)
                }
                uiData.noLoginVisibility?.let {
                    setBundleVisibilityData(noFollowVisibility = View.GONE,
                            noLoginVisibility = it,
                            noPostVisibility = View.GONE)
                }
            }
        })

        // load following feed 이벤트 observe
        postDetailViewModel.loadPostDetailData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isLoading ->
                followingFeedAdapter?.dataLoading = isLoading
                needInitData = false
            }
        })

        // post share kakao 이벤트 observe
        postDetailViewModel.shareKakaoData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { params ->
                shareViewModel.shareKakao(context = context!!, shareParams = params)
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.noFollowLayout -> { // 팔로우 하기
                    RunActivity.recommendUserActivity(context = context as Activity)
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

    fun reloadFeedData(loadingBar: Boolean?) {
        context?.let {
            postDetailViewModel.loadFollowingFeedData(authorization = spHelper.authorization, page = 1, loadingBar = loadingBar)
        } ?:let { needInitData = true }
    }

    private fun setLayoutVisibility() {
        noFollowLayout?.visibility = getBundleNoFollowVisibility()
        noLoginLayout?.visibility = getBundleNoLoginVisibility()
        noPostLayout?.visibility = getBundleNoPostVisibility()
    }

    private fun getBundleNoFollowVisibility(): Int = arguments?.getInt("noFollowVisibility") ?: View.GONE
    private fun getBundleNoLoginVisibility(): Int = arguments?.getInt("noLoginVisibility") ?: View.GONE
    private fun getBundleNoPostVisibility(): Int = arguments?.getInt("noPostVisibility") ?: View.GONE

    private fun setBundleVisibilityData(noFollowVisibility: Int, noLoginVisibility: Int, noPostVisibility: Int) {
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
            args.putInt("noFollowVisibility", View.GONE)
            args.putInt("noLoginVisibility", View.GONE)
            args.putInt("noPostVisibility", View.GONE)

            fragment.arguments = args

            return fragment
        }
    }
}