package xlab.world.xlab.view.cart

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_cart.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.CartAdapter
import xlab.world.xlab.data.adapter.CartListData
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.webView.BuyGoodsWebViewActivity

class CartActivity : AppCompatActivity(), View.OnClickListener {
    private val cartViewModel: CartViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var cartAdapter: CartAdapter

    private lateinit var defaultListener: DefaultListener
    private val selectListener = View.OnClickListener { view ->
        cartViewModel.selectCartData(cartIndex = view.tag as Int)
    }
    private val deleteListener = View.OnClickListener { view ->
        cartViewModel.deleteCartData(authorization = spHelper.authorization, cartIndex = view.tag as Int)
    }
    private val goodsMinusListener = View.OnClickListener { view ->
        cartViewModel.cartGoodsCntMinus(authorization = spHelper.authorization, cartIndex = view.tag as Int)
    }
    private val goodsPlusListener = View.OnClickListener { view ->
        cartViewModel.cartGoodsCntPlus(authorization = spHelper.authorization, cartIndex = view.tag as Int)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)

        when (resultCode) {
            Activity.RESULT_OK -> {
                cartViewModel.setResultCodeOK()
                when (requestCode) {
                    RequestCodeData.GOODS_DETAIL, // 제품 상세
                    RequestCodeData.GOODS_BUYING -> { // 구매하기
                        cartViewModel.loadCartData(authorization = spHelper.authorization, page = 1)
                    }
                }
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                setResult(ResultCodeData.LOGOUT_SUCCESS)
                finish()
            }
        }
    }

    private fun onSetup() {
        actionBarTitle.setText(getText(R.string.my_cart_title), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        appBarLayout.stateListAnimator = null

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        defaultListener = DefaultListener(context = this)

        // cart adapter & recycler 초기화
        cartAdapter = CartAdapter(context = this,
                goodsListener = defaultListener.goodsListener,
                selectListener = selectListener,
                deleteListener = deleteListener,
                goodsMinusListener = goodsMinusListener,
                goodsPlusListener = goodsPlusListener)
        goodsRecyclerView.adapter = cartAdapter
        goodsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        goodsRecyclerView.addItemDecoration(CustomItemDecoration(context = this, top = 20f, bottom = 5f))
        (goodsRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        goodsRecyclerView.isNestedScrollingEnabled = false

        cartViewModel.loadCartData(authorization = spHelper.authorization, page = 1)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        allSelectBtn.setOnClickListener(this) // 전체 선택
        buyingBtn.setOnClickListener(this) // 구매하기
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        cartViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                }
                uiData.cartData?.let {
                    cartAdapter.linkData(cartData = it)
                }
                uiData.cartLayoutVisibility?.let {
                    cartLayout.visibility = it
                }
                uiData.noCartLayoutVisibility?.let {
                    noCartLayout.visibility = it
                }
                uiData.cartDataUpdate?.let {
                    cartAdapter.notifyDataSetChanged()
                    cartViewModel.setTotalPrice()
                }
                uiData.cartDataUpdateIndex?.let {
                    cartAdapter.notifyItemChanged(it)
                }
                uiData.selectAll?.let {
                    allSelectBtn.isSelected = it
                }
                uiData.selectCnt?.let {
                    textViewSelectCnt.setText(it, TextView.BufferType.SPANNABLE)
                    textViewOrderGoodsNum.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.totalCnt?.let {
                    textViewTotalCnt.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.totalGoodsPrice?.let {
                    textViewTotalOrderPrice.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.totalDeliveryPrice?.let {
                    textViewTotalDeliverPrice.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.paymentPrice?.let {
                    textViewTotalPayPrice.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.resultCode?.let {
                    setResult(it)
                    finish()
                }
            }
        })

        // load cart 이벤트 observe
        cartViewModel.loadCartEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.status?.let { isLoading ->
                    cartAdapter.dataLoading = isLoading
                }
            }
        })

        // buy goods 이벤트 observe
        cartViewModel.buySelectedGoodsEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.goodsSnoList?.let {
                    RunActivity.buyGoodsWebViewActivity(context = this, snoList = it, from = AppConstants.FROM_CART)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    cartViewModel.backBtnAction()
                }
                R.id.allSelectBtn -> { // 전체 선택
                    cartViewModel.selectAllCartData(isSelectAll = allSelectBtn.isSelected)
                }
                R.id.buyingBtn -> { // 구매하기
                    cartViewModel.buySelectedGoods()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, CartActivity::class.java)
        }
    }
}
