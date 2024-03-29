package xlab.world.xlab.view.profile.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_profile_pet.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GoodsThumbnailAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.profile.ProfileViewModel

class ProfilePetFragment: Fragment(), View.OnClickListener {
    private val profileViewModel: ProfileViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var goodsThumbnailAdapter: GoodsThumbnailAdapter? = null

    private var defaultListener: DefaultListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile_pet, container, false)
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

        // topic goods recycler view & adapter 초기화
        goodsThumbnailAdapter = goodsThumbnailAdapter ?: GoodsThumbnailAdapter(context = context!!,
                moreItemListener = null,
                goodsListener = defaultListener!!.goodsListener)
        recyclerView.adapter = goodsThumbnailAdapter
        val gridLayoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when(goodsThumbnailAdapter!!.getItemViewType(position = position)) {
                    AppConstants.ADAPTER_HEADER -> 3
                    AppConstants.ADAPTER_CONTENT -> 1
                    else -> 1
                }
            }
        }
        recyclerView.layoutManager = gridLayoutManager
        if (recyclerView.itemDecorationCount < 1)
            recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, offset = 0.5f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false


        if (needInitData) {
            reloadPetUsedGoodsData(loadingBar = true)
        } else
            setLayoutVisibility()
    }

    private fun onBindEvent() {
        swipeRefreshLayout.setOnRefreshListener {
            reloadPetUsedGoodsData(loadingBar = null)
        }

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as GridLayoutManager, isLoading = goodsThumbnailAdapter!!.dataLoading, total = goodsThumbnailAdapter!!.dataTotal) { _ ->
                profileViewModel.loadTopicUsedGoodsData(context = context!!, userId = getBundleUserId(), goodsType = AppConstants.USED_GOODS_PET, page = goodsThumbnailAdapter!!.dataNextPage, loginUseId = spHelper.userId)
            }
        }
    }

    private fun observeViewModel() {
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
                    defaultToast!!.showToast(message = it)
                }
                uiData.topicUsedGoodsData?.let {
                    goodsThumbnailAdapter?.linkData(goodsThumbnailData = it)
                    swipeRefreshLayout.isRefreshing = false
                }
                uiData.topicUsedGoodsDataUpdate?.let {
                    goodsThumbnailAdapter?.notifyDataSetChanged()
                }
                uiData.usedGoodsVisibility?.let {
                    setBundleVisibilityData(noMyGoodsLayout = it.myGoods, noOtherGoodsLayout = it.otherGoods)
                }
            }
        })

        // load topic used goods 이벤트 observe
        profileViewModel.loadTopicUsedGoodsData.observe(owner = this, observer = android.arch.lifecycle.Observer { loadTopicUsedGoodsEvent ->
            loadTopicUsedGoodsEvent?.let { isLoading ->
                goodsThumbnailAdapter?.dataLoading = isLoading
                needInitData = false
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {

            }
        }
    }

    fun reloadPetUsedGoodsData(loadingBar: Boolean?) {
        context?.let {
            profileViewModel.loadTopicUsedGoodsData(context = context!!, userId = getBundleUserId(), goodsType = AppConstants.USED_GOODS_PET, page = 1, loginUseId = spHelper.userId, loadingBar = loadingBar)
        } ?:let { needInitData = true }
    }

    private fun setLayoutVisibility() {
        noMyGoodsLayout?.visibility = getBundleNoMyGoodsVisibility()
        noOtherGoodsLayout?.visibility = getBundleNoOtherGoodsVisibility()
    }

    private fun getBundleUserId(): String = arguments?.getString("userId") ?: ""
    private fun getBundleNoMyGoodsVisibility(): Int = arguments?.getInt("noMyGoodsLayout") ?: View.INVISIBLE
    private fun getBundleNoOtherGoodsVisibility(): Int = arguments?.getInt("noOtherGoodsLayout") ?: View.INVISIBLE

    private fun setBundleVisibilityData(noMyGoodsLayout: Int, noOtherGoodsLayout: Int) {
        arguments?.putInt("noMyGoodsLayout", noMyGoodsLayout)
        arguments?.putInt("noOtherGoodsLayout", noOtherGoodsLayout)

        setLayoutVisibility()
    }

    companion object {
        fun newFragment(userId: String): ProfilePetFragment {
            val fragment =  ProfilePetFragment()

            val args = Bundle()
            args.putString("userId", userId)
            args.putBoolean("needInitData", true)
            args.putInt("noMyGoodsLayout", View.INVISIBLE)
            args.putInt("noOtherGoodsLayout", View.INVISIBLE)
            fragment.arguments = args

            return fragment
        }
    }
}