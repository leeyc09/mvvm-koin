package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.GoodsOrderData
import xlab.world.xlab.data.adapter.GoodsOrderListData
import xlab.world.xlab.utils.support.GodoData
import xlab.world.xlab.utils.support.SupportData

class GoodsOrderAdapter(private val context: Context,
                        private val orderDetailListener: View.OnClickListener?,
                        private val deliverTrackingListener: View.OnClickListener,
                        private val orderCancelListener: View.OnClickListener,
                        private val moreListener: View.OnClickListener) : RecyclerView.Adapter<GoodsOrderAdapter.ViewHolder>() {

    private var goodsOrderData: GoodsOrderData = GoodsOrderData()
    var dataLoading: Boolean
        get() = this.goodsOrderData.isLoading
        set(value) { this.goodsOrderData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.goodsOrderData.total
    var dataNextPage: Int = 1
        get() = this.goodsOrderData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun linkData(goodsOrderData: GoodsOrderData) {
        this.goodsOrderData = goodsOrderData
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_order, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = goodsOrderData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return goodsOrderData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        private val orderNumLayout: LinearLayout = view.findViewById(R.id.orderNumLayout)
        private val textViewOrderNo: TextView = view.findViewById(R.id.textViewOrderNo)
        private val textViewOrderDate: TextView = view.findViewById(R.id.textViewOrderDate)
        private val orderCancelBtn: TextView = view.findViewById(R.id.orderCancelBtn)
        private val imageViewGoods: ImageView = view.findViewById(R.id.imageViewGoods)
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        private val optionLayout: LinearLayout = view.findViewById(R.id.optionLayout)
        private val textViewOption: TextView = view.findViewById(R.id.textViewOption)
        private val textViewGoodsCnt: TextView = view.findViewById(R.id.textViewGoodsCnt)
        private val textViewPrice: TextView = view.findViewById(R.id.textViewPrice)
        private val stateLayout: LinearLayout = view.findViewById(R.id.stateLayout)
        private val textViewStatus: TextView = view.findViewById(R.id.textViewStatus)
        private val textViewStatus2: TextView = view.findViewById(R.id.textViewStatus2)
        private val imageViewArrow: ImageView = view.findViewById(R.id.imageViewArrow)
        private val moreBtn: ImageView = view.findViewById(R.id.moreBtn)

        override fun display(item: GoodsOrderListData, position: Int) {
            // 주문 번호 -> 같은 주문번호 맨 위 아이템만 헤더(주문번호, 주문날짜, 주문취소 버튼) 보이기 & 간격 조절
            val offSetDIP: Int
            if (position == 0) {
                setHeaderData(item = item)
                offSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics).toInt()
            } else {
                val preItem = goodsOrderData.items[position - 1]
                if (item.orderNo == preItem.orderNo) { // 이전 아이템과 같은 회사 => 헤더 숨기기
                    orderNumLayout.visibility = View.GONE
                    offSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
                } else {
                    setHeaderData(item = item)
                    offSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, context.resources.displayMetrics).toInt()
                }
            }
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, offSetDIP, 0, 0)

            // 주문상태가 입금 대기일 경우 -> 주문취소 버튼 활성화 & more 버튼 비활성화
            if (item.orderStatus == "o1") {
                orderCancelBtn.visibility = View.VISIBLE
                moreBtn.visibility = View.GONE
            } else {
                orderCancelBtn.visibility = View.GONE
                moreBtn.visibility = View.VISIBLE
            }

            // 상품 이미지
            Glide.with(context)
                    .load(item.image)
                    .apply(glideOption)
                    .into(imageViewGoods)

            // 상품 브랜드 & 이름
            val titleStr = SpannableString("${item.brand} ${item.name}")
            val grayColor = ResourcesCompat.getColor(context.resources, R.color.color6D6D6D, null)
            val blackColor = ResourcesCompat.getColor(context.resources, R.color.color000000, null)
            titleStr.setSpan(ForegroundColorSpan(grayColor), 0, item.brand.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            titleStr.setSpan(ForegroundColorSpan(blackColor), item.name.length, titleStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textViewTitle.setText(titleStr, TextView.BufferType.SPANNABLE)

            // 상품 옵션 -> 옵션 있는 경우만 표기
            if (item.option != null) {
                optionLayout.visibility = View.VISIBLE

                var optionStr = ""
                item.option.forEachIndexed { index, option ->
                    optionStr += option
                    if (index < item.option.size - 1)
                        optionStr += " ∙ "
                }
                textViewOption.setText(optionStr, TextView.BufferType.SPANNABLE)
            } else { // no options
                optionLayout.visibility = View.GONE
            }

            // 상품 갯수 & 가격
            textViewGoodsCnt.setText(String.format("${item.count}개"), TextView.BufferType.SPANNABLE)
            textViewPrice.setText(SupportData.applyPriceFormat(price = item.price * item.count), TextView.BufferType.SPANNABLE)

            // 주문상태 & 추가정보 & 화살표
            GodoData.orderStateMap[item.orderStatus]?.let {
                textViewStatus.setText(it.str, TextView.BufferType.SPANNABLE)
                textViewStatus.setTextColor(it.fontColor)
                // 추가정보 있는 경우만 표기
                if (it.str2.isEmpty()) {
                    textViewStatus2.visibility = View.GONE
                } else {
                    textViewStatus2.visibility = View.VISIBLE
                    textViewStatus2.setText(it.str2, TextView.BufferType.SPANNABLE)
                    textViewStatus2.setTextColor(it.fontColor2)
                }
                // 화살표 필요한 경우만 표기
                imageViewArrow.visibility =
                        if (it.needArrow) View.VISIBLE
                        else View.GONE
            }

            // 터치 이벤트
            moreBtn.tag = item
            moreBtn.setOnClickListener(moreListener)

            if (item.invoiceNo.isNotEmpty()) { // 송장번호 있는 경우만 터치 가능하게
                stateLayout.tag = item.invoiceNo
                stateLayout.setOnClickListener(deliverTrackingListener)
            }

            orderDetailListener?.let {
                mainLayout.tag = item.orderNo
                mainLayout.setOnClickListener(orderDetailListener)
            }

            orderCancelBtn.tag = item.orderNo
            orderCancelBtn.setOnClickListener(orderCancelListener)
        }

        private fun setHeaderData(item: GoodsOrderListData) {
            orderNumLayout.visibility = View.VISIBLE
            textViewOrderNo.setText(item.orderNo, TextView.BufferType.SPANNABLE)
            val orderDate = "${item.orderYear}.${item.orderMonth}.${item.orderDay}"
            textViewOrderDate.setText(orderDate, TextView.BufferType.SPANNABLE)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: GoodsOrderListData, position: Int)
    }
}
