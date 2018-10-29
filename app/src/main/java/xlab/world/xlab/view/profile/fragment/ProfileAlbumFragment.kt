package xlab.world.xlab.view.profile.fragment

import android.app.Activity
import android.os.Bundle
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
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.ShareViewModel
import xlab.world.xlab.view.postDetail.PostDetailViewModel
import xlab.world.xlab.view.posts.PostsViewModel
import xlab.world.xlab.view.profile.ProfileActivity
import xlab.world.xlab.view.profile.ProfileViewModel

class ProfileAlbumFragment: Fragment(), View.OnClickListener {
    private val postsViewModel: PostsViewModel by viewModel()
    private val postDetailViewModel: PostDetailViewModel by viewModel()
    private val shareViewModel: ShareViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var postThumbnailAdapter: PostThumbnailAdapter? = null
    private var postDetailAdapter: PostDetailAdapter? = null

    private var gridLayoutManager: GridLayoutManager? = null // thumbnail view
    private var linearLayoutManager: LinearLayoutManager? = null // detail view

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

        // listener 초기화
        defaultListener = defaultListener ?: DefaultListener(context = context as Activity)
        postDetailListener = postDetailListener ?: PostDetailListener(context = context as AppCompatActivity, fragmentManager = (context as AppCompatActivity).supportFragmentManager,
                postMoreEvent = { editPosition, deletePosition ->
                    editPosition?.let {  position ->
                        RunActivity.postUploadContentActivity(context = context as Activity,
                                postId = postDetailAdapter!!.getItem(position).postId,
                                youTubeVideoId = postDetailAdapter!!.getItem(position).youTubeVideoID,
                                imagePathList = ArrayList())
                    }
                    deletePosition?.let { position ->
                        postDetailViewModel.deletePost(authorization = spHelper.authorization, postId = postDetailAdapter!!.getItem(position).postId)
                    }
                },
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
        changeViewTypeListener = changeViewTypeListener ?: View.OnClickListener { view ->
            if (recyclerView.layoutManager is GridLayoutManager) { // post thumbnail adapter
                // 포스트 썸네일보기 -> 자세히 보기
                recyclerView.adapter = postDetailAdapter
                recyclerView.layoutManager = linearLayoutManager
            } else if (recyclerView.layoutManager is LinearLayoutManager) { // post detail adapter
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
                sharePostListener = postDetailListener!!.sharePostListener,
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
            reloadAlbumPostsData(loadingBar = true)
        else
            setLayoutVisibility()
    }

    private fun onBindEvent() {
        noPostsLayout.setOnClickListener(this) // 포스트 업로드

        swipeRefreshLayout.setOnRefreshListener {
            reloadAlbumPostsData(loadingBar = null)
        }

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            if (it is GridLayoutManager) { // post thumbnail adapter
                ViewFunction.isScrolledRecyclerView(layoutManager = it, isLoading = postThumbnailAdapter!!.dataLoading, total = postThumbnailAdapter!!.dataTotal) { _ ->
                    postsViewModel.loadUserPostsThumbData(userId = getBundleUserId(), page = postThumbnailAdapter!!.dataNextPage)
                }
            } else if (it is LinearLayoutManager) { // post detail adapter
                ViewFunction.isScrolledRecyclerView(layoutManager = it, isLoading = postDetailAdapter!!.dataLoading, total = postDetailAdapter!!.dataTotal) { _ ->
                    postDetailViewModel.loadUserPostsDetailData(authorization = spHelper.authorization, userId = getBundleUserId(), page = postDetailAdapter!!.dataNextPage, loginUserId = spHelper.userId)
                }
            }
        }
    }

    private fun observeViewModel() {
        // TODO: Posts View Model
        // UI 이벤트 observe
        postsViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.postsData?.let {
                    postThumbnailAdapter?.linkData(postThumbnailData = it)
                    swipeRefreshLayout.isRefreshing = false
                }
                uiData.postsDataUpdate?.let {
                    postThumbnailAdapter?.notifyDataSetChanged()
                }
                uiData.emptyPostVisibility?.let {
                    setBundleVisibilityData(noPostsLayout = it)
                }
            }
        })

        // post load 이벤트 observe
        postsViewModel.loadPostsData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isLoading ->
                postThumbnailAdapter?.dataLoading = isLoading
                needInitData = false
            }
        })

        // post share kakao 이벤트 observe
        postDetailViewModel.shareKakaoData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { params ->
                shareViewModel.shareKakao(context = context!!, shareParams = params)
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
                    postDetailAdapter?.linkData(postDetailData = it)
                    swipeRefreshLayout.isRefreshing = false
                }
                uiData.postDetailDataUpdate?.let {
                    postDetailAdapter?.notifyDataSetChanged()
                }
                uiData.postDetailUpdateIndex?.let {
                    (context as ProfileActivity).setResultCodeFromFragment(resultCode = Activity.RESULT_OK)
                    postDetailAdapter?.notifyItemChanged(it)
                }
                uiData.noPostVisibility?.let {
                    // post 없으면 no post 띄우기
                    setBundleVisibilityData(noPostsLayout = it)
                }
            }
        })

        // post detail load 이벤트 observe
        postDetailViewModel.loadPostDetailData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isLoading ->
                postDetailAdapter?.dataLoading = isLoading
                needInitData = false
            }
        })

        // post delete 이벤트 observe
        postDetailViewModel.postDeleteData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isSuccess ->
                if (isSuccess) {
                    (context as ProfileActivity).setResultCodeFromFragment(resultCode = Activity.RESULT_OK)
                    reloadAlbumPostsData(loadingBar = null)
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

    fun reloadAlbumPostsData(loadingBar: Boolean?) {
        context?.let {
            postsViewModel.loadUserPostsThumbData(userId = getBundleUserId(), page = 1, loadingBar = loadingBar)
            postDetailViewModel.loadUserPostsDetailData(authorization = spHelper.authorization, userId = getBundleUserId(), page = 1, loginUserId = spHelper.userId, loadingBar = loadingBar)
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