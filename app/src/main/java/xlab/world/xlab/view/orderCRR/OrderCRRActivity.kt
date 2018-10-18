package xlab.world.xlab.view.orderCRR

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
import kotlinx.android.synthetic.main.activity_order_crr.*
import kotlinx.android.synthetic.main.dialog_edit_text.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.CartAdapter
import xlab.world.xlab.data.adapter.CartListData
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.ListSelectBottomDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

// (CRR -> Change Refund Return)
class OrderCRRActivity : AppCompatActivity(), View.OnClickListener {
    private val orderCRRViewModel: OrderCRRViewModel by viewModel()
    private val spHelper: SPHelper by inject()
    private val letterOrDigitInputFilter: LetterOrDigitInputFilter by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var reasonDialog: ListSelectBottomDialog
    private lateinit var bankDialog: ListSelectBottomDialog

    private lateinit var crrGoodsAdapter: CartAdapter

    private lateinit var defaultListener: DefaultListener
    private val selectListener = View.OnClickListener { view ->
        orderCRRViewModel.selectCrrGoodsData(crrGoodsIndex = view.tag as Int)
    }
    private val goodsMinusListener = View.OnClickListener { view ->
        orderCRRViewModel.crrGoodsCntMinus(crrGoodsIndex = view.tag as Int)
    }
    private val goodsPlusListener = View.OnClickListener { view ->
        orderCRRViewModel.crrGoodsCntPlus(crrGoodsIndex = view.tag as Int)
    }
    private val reasonListener = object : ListSelectBottomDialog.Listener {
        override fun onListSelect(text: String) {
            textViewReasonSelect.setText(text, TextView.BufferType.SPANNABLE)
            textViewReasonSelect.isSelected = true

            checkFinishEnable()
        }
    }
    private val bankListener = object : ListSelectBottomDialog.Listener {
        override fun onListSelect(text: String) {
            textViewBankSelect.setText(text, TextView.BufferType.SPANNABLE)
            textViewBankSelect.isSelected = true

            checkFinishEnable()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_crr)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        actionBtn.visibility = View.GONE

        editTextBankAccount.filters = arrayOf(letterOrDigitInputFilter)

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        reasonDialog = DialogCreator.listSelectBottomDialog(listener = reasonListener)
        bankDialog = DialogCreator.listSelectBottomDialog(listener = bankListener)

        defaultListener = DefaultListener(context = this)

        // crrGoodsAdapter & recycler 초기화
        crrGoodsAdapter = CartAdapter(context = this,
                goodsListener = null,
                selectListener = selectListener,
                deleteListener = null,
                goodsMinusListener = goodsMinusListener,
                goodsPlusListener = goodsPlusListener)
        goodsRecyclerView.adapter = crrGoodsAdapter
        goodsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        goodsRecyclerView.addItemDecoration(CustomItemDecoration(context = this, top = 20f, bottom = 5f))
        (goodsRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        goodsRecyclerView.isNestedScrollingEnabled = false

        orderCRRViewModel.initViewLayout(context = this,
                crrMode = intent.getIntExtra(IntentPassName.CRR_MODE, RequestCodeData.ORDER_REFUND),
                orderNo = intent.getStringExtra(IntentPassName.ORDER_NO),
                sno = intent.getStringExtra(IntentPassName.CART_SNO))
        orderCRRViewModel.loadCrrGoodsData(authorization = spHelper.authorization)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        finishBtn.setOnClickListener(this) // 완료
        allSelectBtn.setOnClickListener(this) // 전체 선택
        reasonBtn.setOnClickListener(this) // 반품사유 선택
        bankBtn.setOnClickListener(this) // 환불은행 선택

        ViewFunction.onTextChange(editText = editTextDetailReason) {
            textViewReasonNum.setText((100 - it.length).toString(), TextView.BufferType.SPANNABLE)
        }
        ViewFunction.onTextChange(editText = editTextBankAccount) {
            checkFinishEnable()
        }
        ViewFunction.onTextChange(editText = editTextAccountHolder) {
            checkFinishEnable()
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        orderCRRViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.resultCode?.let {
                    setResult(it)
                    finish()
                }
                uiData.titleStr?.let {
                    actionBarTitle.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.reasonTitleStr?.let {
                    textViewReasonTitle.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.refundVisibility?.let {
                    refundLayout.visibility = it
                }
                uiData.reasonList?.let {
                    reasonDialog.changeListData(listData = it)
                }
                uiData.bankList?.let {
                    bankDialog.changeListData(listData = it)
                }
                uiData.crrGoodsData?.let {
                    crrGoodsAdapter.linkData(cartData = it)
                }
                uiData.crrGoodsDataUpdate?.let {
                    crrGoodsAdapter.notifyDataSetChanged()
                }
                uiData.crrGoodsDataUpdateIndex?.let {
                    crrGoodsAdapter.notifyItemChanged(it)
                }
                uiData.crrGoodsSelectCnt?.let {
                    textViewSelectNum.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.crrGoodsTotalCnt?.let {
                    textViewTotalNum.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.selectAll?.let {
                    allSelectBtn.isSelected = it
                }
                uiData.needFinishBtnEnableCheck?.let {
                    checkFinishEnable()
                }
                uiData.finishBtnEnable?.let {
                    finishBtn.isEnabled = it
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    orderCRRViewModel.actionBackAction()
                }
                R.id.finishBtn -> { // 완료
                    orderCRRViewModel.orderCRR(authorization = spHelper.authorization,
                            reason = textViewReasonSelect.text.toString().trim(),
                            detailReason = getDetailReason(),
                            bankName = textViewBankSelect.text.toString().trim(),
                            bankAccount = getBankAccount(),
                            accountHolder = getAccountHolder())
                }
                R.id.allSelectBtn -> { // 전체 선택
                    orderCRRViewModel.selectAllCrrGoodsData(isSelectAll = allSelectBtn.isSelected)
                }
                R.id.reasonBtn -> { // 반품사유 선택
                    reasonDialog.show(supportFragmentManager, "reasonDialog")
                }
                R.id.bankBtn -> { // 환불은행 선택
                    bankDialog.show(supportFragmentManager, "bankDialog")
                }
            }
        }
    }

    private fun checkFinishEnable() {
        orderCRRViewModel.checkFinishEnable(crrGoodsSelectCnt = getCrrGoodsSelectCnt(),
                isReasonSelect = textViewReasonSelect.isSelected,
                isBankSelect = textViewBankSelect.isSelected,
                bankAccount = getBankAccount(),
                accountHolder = getAccountHolder())
    }

    private fun getCrrGoodsSelectCnt(): Int = textViewSelectNum.text.toString().trim().toInt()
    private fun getDetailReason(): String = editTextDetailReason.text.toString().trim()
    private fun getBankAccount(): String = editTextBankAccount.text.toString().trim()
    private fun getAccountHolder(): String = editTextAccountHolder.text.toString().trim()

    companion object {
        fun newIntent(context: Context, crrMode: Int, orderNo: String, sno: String): Intent {
            val intent = Intent(context, OrderCRRActivity::class.java)
            intent.putExtra(IntentPassName.CRR_MODE, crrMode)
            intent.putExtra(IntentPassName.ORDER_NO, orderNo)
            intent.putExtra(IntentPassName.CART_SNO, sno)

            return intent
        }
    }
}
