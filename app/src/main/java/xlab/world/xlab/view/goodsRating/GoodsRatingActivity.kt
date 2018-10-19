package xlab.world.xlab.view.goodsRating

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_goods_rating.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GoodsRatingAdapter
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultDialog
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class GoodsRatingActivity : AppCompatActivity(), View.OnClickListener {
    private val goodsRatingViewModel: GoodsRatingViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var glideOption: RequestOptions

    private lateinit var defaultToast: DefaultToast

    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var editCancelDialog: DefaultDialog

    private lateinit var goodsRatingAdapter: GoodsRatingAdapter

    private val ratingListener = View.OnClickListener { view ->
        if (view.tag is GoodsRatingAdapter.RatingTag) {
            val tagData = view.tag as GoodsRatingAdapter.RatingTag
            goodsRatingViewModel.ratingChange(position = tagData.position, rating = tagData.rating)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_rating)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        actionBarTitle.visibility = View.GONE
        actionBtn.visibility = View.GONE

        val imageHolder = ColorDrawable(ResourcesCompat.getColor(resources, R.color.colorBlack30, null))
        glideOption = RequestOptions()
                .centerCrop()
                .placeholder(imageHolder)
                .error(imageHolder)

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        editCancelDialog = DialogCreator.editCancelDialog(context = this)

        // goodsRatingAdapter & recycler 초기화
        goodsRatingAdapter = GoodsRatingAdapter(context = this, ratingListener = ratingListener)
        ratingRecyclerView.adapter = goodsRatingAdapter
        ratingRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        ratingRecyclerView.addItemDecoration(CustomItemDecoration(context = this, bottom = 32f))
        (ratingRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        ratingRecyclerView.isNestedScrollingEnabled = false

        goodsRatingViewModel.loadGoodsData(goodsCode = intent.getStringExtra(IntentPassName.GOODS_CODE))
        goodsRatingViewModel.loadGoodsRatingData(authorization = spHelper.authorization)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        finishBtn.setOnClickListener(this) // 완료 버튼
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        goodsRatingViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.goodsImageUrl?.let {
                    Glide.with(this)
                            .load(it)
                            .apply(glideOption)
                            .into(imageViewGoods)
                }
                uiData.goodsBrand?.let {
                    textViewBrand.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.goodsName?.let {
                    textViewName.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.ratingData?.let {
                    goodsRatingAdapter.linkData(goodsRatingData = it)
                }
                uiData.ratingDataUpdateIndex?.let {
                    goodsRatingAdapter.notifyItemChanged(it)
                }
                uiData.finishBtnEnable?.let {
                    finishBtn.isEnabled = it
                }
                uiData.isChangeData?.let {
                    editCancelDialog.show()
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    goodsRatingViewModel.backBtnAction()
                }
                R.id.finishBtn -> { // 완료
                    goodsRatingViewModel.postGoodsRating(context = this, authorization = spHelper.authorization)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, goodsCode: String): Intent {
            val intent = Intent(context, GoodsRatingActivity::class.java)
            intent.putExtra(IntentPassName.GOODS_CODE, goodsCode)

            return intent
        }
    }
}
