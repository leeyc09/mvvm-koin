package xlab.world.xlab.view.goodsDetail.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_goods_detail_stats.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GoodsDetailStatsAdapter
import xlab.world.xlab.adapter.recyclerView.SocialNotificationAdapter
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.goodsDetail.GoodsDetailViewModel

class GoodsDetailStatsFragment: Fragment() {
    private val goodsDetailViewModel: GoodsDetailViewModel by viewModel()
    private val spHelper: SPHelper by inject()
    private val fontColorSpan: FontColorSpan by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var goodsDetailStatsAdapter: GoodsDetailStatsAdapter? = null

    private var defaultListener: DefaultListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_goods_detail_stats, container, false)
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

        // stats adapter & recycler 초기화
        goodsDetailStatsAdapter = goodsDetailStatsAdapter?: GoodsDetailStatsAdapter(context = context!!)
        recyclerView.adapter = goodsDetailStatsAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        if (recyclerView.itemDecorationCount < 1)
            recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, bottom = 48f))

        if (needInitData)
            loadGoodsStatsData()
        else
            setLayoutVisibility()
    }

    private fun onBindEvent() {
    }

    private fun observeViewModel() {
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
                uiData.goodsStatsData?.let {
                    goodsDetailStatsAdapter?.linkData(goodsDetailStatsData = it)
                    needInitData = true
                }
                uiData.noStatsVisibility?.let {
                    setBundleVisibilityData(noStatsLayout = it)
                }
            }
        })
    }

    private fun setLayoutVisibility() {
        noStatsLayout?.visibility = getBundleNoStatsVisibility()
    }

    private fun getBundleNoStatsVisibility(): Int = arguments?.getInt("noStatsLayout") ?: View.INVISIBLE

    private fun setBundleVisibilityData(noStatsLayout: Int) {
        arguments?.putInt("noStatsLayout", noStatsLayout)

        setLayoutVisibility()
    }

    fun loadGoodsStatsData() {
        context?.let {
            goodsDetailViewModel.loadGoodsStatsData(authorization = spHelper.authorization, goodsCode = (context as Activity).intent.getStringExtra(IntentPassName.GOODS_CODE),
                    boldFont = fontColorSpan.notoBold000000, regularFont = fontColorSpan.notoRegular000000)
        } ?:let { needInitData = true }
    }

    companion object {
        fun newFragment(): GoodsDetailStatsFragment {
            val fragment =  GoodsDetailStatsFragment()

            val args = Bundle()
            args.putBoolean("needInitData", true)
            args.putInt("noStatsLayout", View.INVISIBLE)
            fragment.arguments = args

            return fragment
        }
    }
}