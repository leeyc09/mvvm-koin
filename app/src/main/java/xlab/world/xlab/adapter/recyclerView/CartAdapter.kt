package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData

class CartAdapter(private val context: Context,
                  private val goodsListener: View.OnClickListener?,
                  private val selectListener: View.OnClickListener,
                  private val deleteListener: View.OnClickListener?,
                  private val goodsMinusListener: View.OnClickListener,
                  private val goodsPlusListener: View.OnClickListener) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private var cartData: CartData = CartData()
    var dataLoading: Boolean
        get() = this.cartData.isLoading
        set(value) { this.cartData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.cartData.total
    var dataNextPage: Int = 1
        get() = this.cartData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun linkData(cartData: CartData) {
        this.cartData = cartData
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_cart, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = cartData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return cartData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        private val selectBtn: ImageView = view.findViewById(R.id.selectBtn)
        private val goodsLayout: LinearLayout = view.findViewById(R.id.goodsLayout)
        private val deleteBtn: ImageView = view.findViewById(R.id.deleteBtn)
        private val imageViewGoods: ImageView = view.findViewById(R.id.imageViewGoods)
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        private val textViewOption: TextView = view.findViewById(R.id.textViewOption)
        private val textViewPrice: TextView = view.findViewById(R.id.textViewPrice)
        private val goodsMinusBtn: ImageView = view.findViewById(R.id.goodsMinusBtn)
        private val goodsPlusBtn: ImageView = view.findViewById(R.id.goodsPlusBtn)
        private val textViewGoodsCnt: TextView = view.findViewById(R.id.textViewGoodsCnt)

        private val deliveryCompanyLayout: LinearLayout = view.findViewById(R.id.deliveryCompanyLayout)
        private val textViewDeliveryCompany: TextView = view.findViewById(R.id.textViewDeliveryCompany)
        private val textViewDeliveryCompanyName: TextView = view.findViewById(R.id.textViewDeliveryCompanyName)

        private val priceLayout: LinearLayout = view.findViewById(R.id.priceLayout)
        private val textViewTotalGoodsPrice: TextView = view.findViewById(R.id.textViewTotalGoodsPrice)
        private val textViewTotalDeliveryPrice: TextView = view.findViewById(R.id.textViewTotalDeliveryPrice)
        private val textViewTotalPrice: TextView = view.findViewById(R.id.textViewTotalPrice)

        override fun display(item: CartListData, position: Int) {
            // 상품 선택 여부
            selectBtn.isSelected = item.isSelect
            // 상품 이미지
            Glide.with(context)
                    .load(item.goodsImage)
                    .apply(glideOption)
                    .into(imageViewGoods)
            // 상품 브랜드 & 이름
            val titleStr = SpannableString("${item.goodsBrand} ${item.goodsName}")
            val grayColor = ResourcesCompat.getColor(context.resources, R.color.color6D6D6D, null)
            val blackColor = ResourcesCompat.getColor(context.resources, R.color.color000000, null)
            titleStr.setSpan(ForegroundColorSpan(grayColor), 0, item.goodsBrand.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            titleStr.setSpan(ForegroundColorSpan(blackColor), item.goodsBrand.length, titleStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textViewTitle.setText(titleStr, TextView.BufferType.SPANNABLE)
            // 상품 옵션
            if (item.goodsOption != null) {
                textViewOption.visibility = View.VISIBLE
                var optionStr = ""
                for ((index, option) in item.goodsOption.withIndex()) {
                    optionStr += option
                    if (index < item.goodsOption.size - 1)
                        optionStr += " ∙ "
                }
                textViewOption.setText(optionStr, TextView.BufferType.SPANNABLE)
            } else {
                textViewOption.visibility = View.INVISIBLE
            }
            // 상품 가격 & 갯수
            textViewPrice.setText(SupportData.applyPriceFormat(price = item.goodsPrice * item.goodsCnt), TextView.BufferType.SPANNABLE)
            textViewGoodsCnt.setText(item.goodsCnt.toString(), TextView.BufferType.SPANNABLE)
            // 삭제 버튼 비활성화 -> 상품 판매 회사 & 회사별 가격 표기 X
            if (deleteListener == null) {
                deleteBtn.visibility = View.GONE
                deliveryCompanyLayout.visibility = View.GONE
                priceLayout.visibility = View.GONE
            } else {
                deleteBtn.visibility = View.VISIBLE
                // 상품 판매 회사 -> 같은 회사 맨 위 아이템만 표기
                if (position == 0) {
                    setCompanyLayout(deliveryNo = item.deliverySno)
                } else {
                    val preItem = cartData.items[position - 1]
                    if (item.deliverySno == preItem.deliverySno) { // 이전 아이템과 같은 회사 -> 회사 표기 X
                        deliveryCompanyLayout.visibility = View.GONE
                    } else {
                        setCompanyLayout(deliveryNo = item.deliverySno)
                    }
                }
                // 회사별 상품 판매 통합 가격 -> 같은 회사 맨 아래 아이템만 표기
                if (position + 1 < cartData.items.size) { // 다음 아이템 있는 경우 -> 회사 비교
                    val nextItem = cartData.items[position + 1]
                    if (item.deliverySno == nextItem.deliverySno) { // 다음 아이템과 같은 회사 -> 가격 표기 X
                        priceLayout.visibility = View.GONE
                    } else {
                        setPriceLayout(deliveryNo = item.deliverySno)
                    }
                } else {
                    setPriceLayout(deliveryNo = item.deliverySno)
                }

                deleteBtn.tag = position//item
                deleteBtn.setOnClickListener(deleteListener)
            }

            // 클릭 이벤트
            selectBtn.tag = position//item
            selectBtn.setOnClickListener(selectListener)

            goodsListener?.let {
                goodsLayout.tag = item.goodsCode
                goodsLayout.setOnClickListener(goodsListener)
            }

            goodsMinusBtn.tag = position//item
            goodsMinusBtn.setOnClickListener(goodsMinusListener)

            goodsPlusBtn.tag = position//item
            goodsPlusBtn.setOnClickListener(goodsPlusListener)
        }

        private fun setCompanyLayout(deliveryNo: String) {
            deliveryCompanyLayout.visibility = View.VISIBLE

            val deliveryCompany: String
            if (deliveryNo == AppConstants.CS_XLAB) { // 슬랩 상품
                deliveryCompany = context.getString(R.string.xlab_delivery)
                textViewDeliveryCompanyName.visibility = View.GONE
            } else { // 타 회사 상품
                deliveryCompany = context.getString(R.string.other_delivery)
                textViewDeliveryCompanyName.visibility = View.VISIBLE
                val deliveryCompanyName = when (deliveryNo) {
                    AppConstants.CS_HOLAPET -> context.resources.getString(R.string.holapet_shop) // 올라펫 상품
                    else -> ""
                }
                textViewDeliveryCompanyName.setText(deliveryCompanyName, TextView.BufferType.SPANNABLE)
            }
            textViewDeliveryCompany.setText(deliveryCompany, TextView.BufferType.SPANNABLE)
        }

        private fun setPriceLayout(deliveryNo: String) {
            priceLayout.visibility = View.VISIBLE

            var goodsPrice = 0
            var selectGoodsCount = 0
            cartData.items.forEach {
                if (deliveryNo == it.deliverySno && it.isSelect) {
                    goodsPrice += (it.goodsPrice * it.goodsCnt)
                    selectGoodsCount++
                }
            }
            val deliveryPrice =
                    if (selectGoodsCount == 0) 0
                    else 2500

            textViewTotalGoodsPrice.setText(SupportData.applyPriceFormat(price = goodsPrice), TextView.BufferType.SPANNABLE)
            textViewTotalDeliveryPrice.setText(SupportData.applyPriceFormat(deliveryPrice), TextView.BufferType.SPANNABLE)
            textViewTotalPrice.setText(SupportData.applyPriceFormat(goodsPrice + deliveryPrice), TextView.BufferType.SPANNABLE)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: CartListData, position: Int)
    }
}
