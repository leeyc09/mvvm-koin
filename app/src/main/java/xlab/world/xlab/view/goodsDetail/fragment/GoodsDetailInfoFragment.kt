package xlab.world.xlab.view.goodsDetail.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.swipe_recycler_view.*
import org.koin.android.architecture.ext.viewModel
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GoodsDetailInfoAdapter
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.goodsDetail.GoodsDetailViewModel

class GoodsDetailInfoFragment: Fragment() {
    private val goodsDetailViewModel: GoodsDetailViewModel by viewModel()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var goodsDetailInfoAdapter: GoodsDetailInfoAdapter? = null

    private val moreInfoListener = View.OnClickListener { view ->
        goodsDetailViewModel.descriptionOpenClose(position = view.tag as Int,
                goodsDescriptionData = goodsDetailInfoAdapter!!.getItem(position = view.tag as Int))
    }
    private val necessaryInfoListener = View.OnClickListener { view ->
        val necessaryInfo = view.tag as GoodsDetailInfoAdapter.NecessaryInfo
        RunActivity.goodsNecessaryActivity(context = context as Activity,
                deliveryNo = necessaryInfo.deliveryNo,
                goodsName = necessaryInfo.goodsName,
                origin = necessaryInfo.origin,
                maker = necessaryInfo.maker)
    }
    private val deliveryListener = View.OnClickListener { view ->
        val necessaryInfo = view.tag as GoodsDetailInfoAdapter.NecessaryInfo
        RunActivity.goodsDeliveryActivity(context = context as Activity, deliveryNo = necessaryInfo.deliveryNo)
    }
    private val goodsInquiryListener = View.OnClickListener { view ->
        val necessaryInfo = view.tag as GoodsDetailInfoAdapter.NecessaryInfo
        RunActivity.goodsInquiryActivity(context = context as Activity, deliveryNo = necessaryInfo.deliveryNo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.swipe_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        swipeRefreshLayout.isEnabled = false

        // Toast, Dialog 초기화
        defaultToast = defaultToast ?: DefaultToast(context = context!!)
        progressDialog = progressDialog ?: DefaultProgressDialog(context = context!!)

        // goodsDetailInfoAdapter & recycler 초기화
        goodsDetailInfoAdapter = goodsDetailInfoAdapter?: GoodsDetailInfoAdapter(context = context!!,
                moreInfoListener = moreInfoListener,
                necessaryInfoListener = necessaryInfoListener,
                deliveryListener = deliveryListener,
                inquiryListener = goodsInquiryListener)
        recyclerView.adapter = goodsDetailInfoAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        if (needInitData)
            loadGoodsDescription()
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
                uiData.goodsDescriptionData?.let {
                    goodsDetailInfoAdapter?.updateData(goodsDetailInfoData = it)
                    needInitData = false
                }
                uiData.goodsDescriptionUpdateIndex?.let {
                    goodsDetailInfoAdapter?.notifyItemChanged(it)
                }
            }
        })
    }

    fun loadGoodsDescription() {
        context?.let {
            goodsDetailViewModel.loadGoodsDetailData(context = context!!, goodsCode = (context as Activity).intent.getStringExtra(IntentPassName.GOODS_CODE), needDescription = true)
        } ?:let { needInitData = true }
    }

    companion object {
        fun newFragment(): GoodsDetailInfoFragment {
            val fragment = GoodsDetailInfoFragment()

            val args = Bundle()
            args.putBoolean("needInitData", true)
            fragment.arguments = args

            return fragment
        }
    }
}