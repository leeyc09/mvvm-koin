package xlab.world.xlab.view.profile.fragment

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_profile_album.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.PostDetailAdapter
import xlab.world.xlab.adapter.recyclerView.PostThumbnailAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.listener.PostDetailListener
import xlab.world.xlab.utils.listener.UserDefaultListener
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.postDetail.PostDetailViewModel
import xlab.world.xlab.view.profile.ProfileViewModel

class ProfileAlbumFragment: Fragment(), View.OnClickListener {
    private val profileViewModel: ProfileViewModel by viewModel()
    private val postDetailViewModel: PostDetailViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private var postThumbnailAdapter: PostThumbnailAdapter? = null
    private var postDetailAdapter: PostDetailAdapter? = null

    private var gridLayoutManager: GridLayoutManager? = null // thumbnail view
    private var linearLayoutManager: LinearLayoutManager? = null // detail view

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var defaultListener: DefaultListener? = null
    private var postDetailListener: PostDetailListener? =  null

    private var changeViewTypeListener: View.OnClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile_album, container, false)
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
        postDetailListener = postDetailListener ?: PostDetailListener(context = context as Activity, fragmentManager = (context as AppCompatActivity).supportFragmentManager,
                postMoreEvent = { editPosition, deletePosition ->
                    editPosition?.let {
                        PrintLog.d("postMore", "post edit", profileViewModel.tag)
                    }
                    deletePosition?.let { position ->
                        postDetailViewModel.deletePost(authorization = spHelper.authorization, postId = postDetailAdapter!!.getItem(position).postId)
                    }
                },
                likePostEvent = { position ->
                    postDetailViewModel.likePost(authorization = spHelper.authorization, position = position, postData = postDetailAdapter!!.getItem(position))
                },
                savePostEvent = { position ->
                    postDetailViewModel.savePost(authorization = spHelper.authorization, position = position, postData = postDetailAdapter!!.getItem(position))
                })
        changeViewTypeListener = changeViewTypeListener ?: View.OnClickListener { view ->
            if (recyclerView.layoutManager is GridLayoutManager) { // post thumbnail adapter
                PrintLog.d("post view current type", "grid", profileViewModel.tag)
                // 포스트 썸네일보기 -> 자세히 보기
                recyclerView.adapter = postDetailAdapter
                recyclerView.layoutManager = linearLayoutManager
            } else if (recyclerView.layoutManager is LinearLayoutManager) { // post detail adapter
                PrintLog.d("post view current type", "linear", profileViewModel.tag)
                // 포스트 자세히 보기 -> 썸네일 보기
                recyclerView.adapter = postThumbnailAdapter
                recyclerView.layoutManager = gridLayoutManager
            }
            recyclerView.scrollToPosition(0)
        }

        // posts thumbnail, detail recycler view & adapter 초기화
        postThumbnailAdapter = postThumbnailAdapter ?: PostThumbnailAdapter(context = context!!,
                changeViewTypeListener = changeViewTypeListener,
                postListener = defaultListener!!.postListener)
        postDetailAdapter = postDetailAdapter ?: PostDetailAdapter(context = context!!,
                changeViewTypeListener = changeViewTypeListener,
                profileListener = null,
                followListener = null,
                moreListener = postDetailListener!!.postMoreListener,
                likePostListener = postDetailListener!!.likePostListener,
                commentsListener = defaultListener!!.commentsListener,
                savePostListener = postDetailListener!!.savePostListener,
                sharePostListener = View.OnClickListener {  },
                hashTagListener = defaultListener!!.hashTagListener,
                goodsListener = defaultListener!!.goodsListener)
        recyclerView.adapter = postThumbnailAdapter
        gridLayoutManager?:let {
            gridLayoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
            gridLayoutManager!!.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when(postThumbnailAdapter!!.getItemViewType(position)) {
                        AppConstants.ADAPTER_HEADER -> 3
                        AppConstants.ADAPTER_CONTENT -> 1
                        else -> 1
                    }
                }
            }
        }
        linearLayoutManager = linearLayoutManager ?: LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = gridLayoutManager
        if (recyclerView.itemDecorationCount < 1)
            recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, offset = 0.5f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        if (needInitData)
            reloadPetUsedGoodsData(loadingBar = true)
        else
            setLayoutVisibility()
    }

    private fun onBindEvent() {
        noPostsLayout.setOnClickListener(this) // 포스트 업로드

        swipeRefreshLayout.setOnRefreshListener {
            reloadPetUsedGoodsData(loadingBar = null)
        }

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            if (it is GridLayoutManager) { // post thumbnail adapter
                ViewFunction.isScrolledRecyclerView(layoutManager = it, isLoading = postThumbnailAdapter!!.dataLoading, total = postThumbnailAdapter!!.dataTotal) { _ ->
                    profileViewModel.loadUserPostsThumbData(userId = getBundleUserId(), page = postThumbnailAdapter!!.dataNextPage)
                }
            } else if (it is LinearLayoutManager) { // post detail adapter
                ViewFunction.isScrolledRecyclerView(layoutManager = it, isLoading = postDetailAdapter!!.dataLoading, total = postDetailAdapter!!.dataTotal) { _ ->
                    profileViewModel.loadUserPostsDetailData(authorization = spHelper.authorization, userId = getBundleUserId(), page = postDetailAdapter!!.dataNextPage, loginUserId = spHelper.userId)
                }
            }
        }
    }

    private fun observeViewModel() {
        // TODO: profile view model
        // UI 이벤트 observe
        profileViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.postsThumbData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        // post 없으면 no post 띄우기
                        setBundleVisibilityData(noPostsLayout =
                        if (it.items.isEmpty()) View.VISIBLE
                        else View.GONE)
                        postThumbnailAdapter?.updateData(postThumbnailData = it)
                        swipeRefreshLayout.isRefreshing = false
                    }
                    else
                        postThumbnailAdapter?.addData(postThumbnailData = it)
                }
                uiData.postsDetailData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        // post 없으면 no post 띄우기
                        setBundleVisibilityData(noPostsLayout =
                                if (it.items.isEmpty()) View.VISIBLE
                                else View.GONE)
                        postDetailAdapter?.updateData(postDetailData = it)
                        swipeRefreshLayout.isRefreshing = false
                    }
                    else
                        postDetailAdapter?.addData(postDetailData = it)
                }
            }
        })

        // load user posts thumb 이벤트 observe
        profileViewModel.loadUserPostsThumbDataEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadUserPostsThumbDataEvent ->
            loadUserPostsThumbDataEvent?.let { _ ->
                loadUserPostsThumbDataEvent.status?.let {
                    postThumbnailAdapter?.dataLoading = it
                    needInitData = false
                }
            }
        })

        // load user posts detail 이벤트 observe
        profileViewModel.loadUserPostsDetailDataEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadUserPostsDetailDataEvent ->
            loadUserPostsDetailDataEvent?.let { _ ->
                loadUserPostsDetailDataEvent.status?.let {
                    postDetailAdapter?.dataLoading = it
                    needInitData = false
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
                uiData.postUpdatePosition?.let {
                    postDetailAdapter?.notifyItemChanged(it)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.noPostsLayout -> { // 포스트 업로드
                }
            }
        }
    }

    fun reloadPetUsedGoodsData(loadingBar: Boolean?) {
        context?.let {
            profileViewModel.loadUserPostsThumbData(userId = getBundleUserId(), page = 1, loadingBar = loadingBar)
            profileViewModel.loadUserPostsDetailData(authorization = spHelper.authorization, userId = getBundleUserId(), page = 1, loginUserId = spHelper.userId, loadingBar = loadingBar)
        } ?:let { needInitData = true }
    }

    private fun setLayoutVisibility() {
        noPostsLayout?.visibility = getBundleNoPostsVisibility()
    }

    private fun getBundleUserId(): String = arguments?.getString("userId") ?: ""
    private fun getBundleNoPostsVisibility(): Int = arguments?.getInt("noPostsLayout") ?: View.INVISIBLE

    private fun setBundleVisibilityData(noPostsLayout: Int) {
        arguments?.putInt("noPostsLayout", noPostsLayout)

        setLayoutVisibility()
    }

    companion object {
        fun newFragment(userId: String): ProfileAlbumFragment {
            val fragment =  ProfileAlbumFragment()

            val args = Bundle()
            args.putString("userId", userId)
            args.putBoolean("needInitData", true)
            args.putInt("noPostsLayout", View.INVISIBLE)
            fragment.arguments = args

            return fragment
        }
    }
}