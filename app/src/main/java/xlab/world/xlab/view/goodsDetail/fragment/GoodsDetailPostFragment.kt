package xlab.world.xlab.view.goodsDetail.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_goods_detail_post.*
import org.koin.android.architecture.ext.viewModel
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GoodsDetailUsedUserAdapter
import xlab.world.xlab.adapter.recyclerView.PostThumbnailAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.goodsDetail.GoodsDetailViewModel
import xlab.world.xlab.view.posts.PostsViewModel

class GoodsDetailPostFragment: Fragment() {
    private val goodsDetailViewModel: GoodsDetailViewModel by viewModel()
    private val postsViewModel: PostsViewModel by viewModel()

    private var needInitUserData
        get() = arguments?.getBoolean("needInitUserData") ?: true
        set(value) {
            arguments?.putBoolean("needInitUserData", value)
        }
    private var needInitPostsData
        get() = arguments?.getBoolean("needInitPostsData") ?: true
        set(value) {
            arguments?.putBoolean("needInitPostsData", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var goodsDetailUsedUserAdapter: GoodsDetailUsedUserAdapter? = null
    private var taggedPostAdapter: PostThumbnailAdapter? = null

    private var defaultListener: DefaultListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_goods_detail_post, container, false)
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

        // goodsDetailUsedUserAdapter & recycler 초기화
        goodsDetailUsedUserAdapter = goodsDetailUsedUserAdapter?: GoodsDetailUsedUserAdapter(context = context!!,
                profileListener = defaultListener!!.profileListener)
        userRecyclerView.adapter = goodsDetailUsedUserAdapter
        userRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        if (userRecyclerView.itemDecorationCount < 1)
            userRecyclerView.addItemDecoration(CustomItemDecoration(context = context!!, right = 20f))

        // taggedPostAdapter & recycler 초기화
        taggedPostAdapter = taggedPostAdapter ?: PostThumbnailAdapter(context = context!!,
                postListener = defaultListener!!.postListener,
                changeViewTypeListener = null)
        postRecyclerView.adapter = taggedPostAdapter
        postRecyclerView.layoutManager = GridLayoutManager(activity, 3, GridLayoutManager.VERTICAL, false)
        postRecyclerView.addItemDecoration(CustomItemDecoration(context = context!!, offset = 0.5f))

        if (needInitUserData) loadUsedUser(page = 1)
        else setUserLayout()

        if (needInitPostsData) loadTaggedPosts(page = 1)
        else setPostLayout()
    }

    private fun onBindEvent() {
        ViewFunction.onRecyclerViewScrolledDown(recyclerView = userRecyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = goodsDetailUsedUserAdapter!!.dataLoading, total = goodsDetailUsedUserAdapter!!.dataTotal) {_->
                loadUsedUser(page = goodsDetailUsedUserAdapter!!.dataNextPage)
            }
        }

//        ViewFunction.onRecyclerViewScrolledDown(recyclerView = postRecyclerView) {
//            ViewFunction.isScrolledRecyclerView(layoutManager = it as GridLayoutManager, isLoading = taggedPostAdapter!!.dataLoading, total = taggedPostAdapter!!.dataTotal) {_->
//                loadTaggedPosts(page = taggedPostAdapter!!.dataNextPage)
//            }
//        }

        ViewFunction.isNestedScrollViewScrolledDown(nestedScrollView = scrollView) { isScrolled ->
            if (isScrolled && !taggedPostAdapter!!.dataLoading) {
                loadTaggedPosts(page = taggedPostAdapter!!.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // TODO: GoodsDetailViewModel
        // UI 이벤트 observe
        goodsDetailViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.goodsUsedUserData?.let {
                    goodsDetailUsedUserAdapter?.linkData(goodsDetailUsedUserData = it)
                    needInitUserData = false
                }
                uiData.goodsUsedUserDataUpdate?.let {
                    goodsDetailUsedUserAdapter?.notifyDataSetChanged()
                }
                uiData.goodsUsedUserTotal?.let {
                    setBundleGoodsUsedUserTotal(total = it)
                }
            }
        })

        // load used user 이벤트 observe
        goodsDetailViewModel.loadUsedUserData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isLoading ->
                goodsDetailUsedUserAdapter?.dataLoading = isLoading
            }
        })

        // TODO: PostsViewModel
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
                    defaultToast?.showToast(message = it)
                }
                uiData.postsData?.let {
                    taggedPostAdapter?.linkData(postThumbnailData = it)
                    needInitPostsData = false
                }
                uiData.postsDataUpdate?.let {
                    taggedPostAdapter?.notifyDataSetChanged()
                }
                uiData.postsTotal?.let {
                    setBundleTagPostsTotal(total = it)
                }
            }
        })

        // load tagged posts 이벤트 observe
        postsViewModel.loadPostsData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isLoading ->
                taggedPostAdapter?.dataLoading = isLoading
            }
        })
    }

    private fun getBundleGoodsUsedUserTotal(): Int = arguments?.getInt("goodsUsedUserTotal") ?: 0
    private fun getBundleTagPostsTotal(): Int = arguments?.getInt("tagPostsTotal") ?: 0

    private fun setBundleGoodsUsedUserTotal(total: Int) {
        arguments?.putInt("goodsUsedUserTotal", total)
        textViewUserCnt?.setText(total.toString(), TextView.BufferType.SPANNABLE)
    }
    private fun setBundleTagPostsTotal(total: Int) {
        arguments?.putInt("tagPostsTotal", total)
        textViewPostCnt?.setText(total.toString(), TextView.BufferType.SPANNABLE)
    }

    private fun setUserLayout() {
        textViewUserCnt?.setText(getBundleGoodsUsedUserTotal().toString(), TextView.BufferType.SPANNABLE)
    }
    private fun setPostLayout() {
        textViewPostCnt?.setText(getBundleTagPostsTotal().toString(), TextView.BufferType.SPANNABLE)
    }

    fun loadUsedUser(page: Int) {
        context?.let {
            goodsDetailViewModel.loadUsedUserData(goodsCode = (context as Activity).intent.getStringExtra(IntentPassName.GOODS_CODE), page = page)
        } ?:let { needInitUserData = true }
    }

    fun loadTaggedPosts(page: Int) {
        context?.let {
            postsViewModel.loadGoodsTaggedPosts(goodsCode = (context as Activity).intent.getStringExtra(IntentPassName.GOODS_CODE), page = page)
        } ?:let { needInitPostsData = true }
    }

    companion object {
        fun newFragment(): GoodsDetailPostFragment {
            val fragment = GoodsDetailPostFragment()

            val args = Bundle()
            args.putBoolean("needInitUserData", true)
            args.putBoolean("needInitPostsData", true)
            args.putInt("goodsUsedUserTotal", 0)
            args.putInt("tagPostsTotal", 0)
            fragment.arguments = args

            return fragment
        }
    }
}