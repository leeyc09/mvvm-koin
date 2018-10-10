package xlab.world.xlab.adapter.recyclerView

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SupportData

class ShopNotificationAdapter(private val context: Context,
                              private val goodsListener: View.OnClickListener) : RecyclerView.Adapter<ShopNotificationAdapter.ViewHolder>() {
    private val fontColorSpan: FontColorSpan by (context as Activity).inject()

    private val shopNotificationData: ShopNotificationData = ShopNotificationData()
    var dataLoading: Boolean
        get() = this.shopNotificationData.isLoading
        set(value) { this.shopNotificationData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.shopNotificationData.total
    var dataNextPage: Int = 1
        get() = this.shopNotificationData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun getItem(position: Int): ShopNotificationListData {
        return shopNotificationData.items[position]
    }

    fun updateData(shopNotificationData: ShopNotificationData) {
        this.shopNotificationData.items.clear()
        this.shopNotificationData.items.addAll(shopNotificationData.items)

        this.shopNotificationData.isLoading = false
        this.shopNotificationData.total = shopNotificationData.total
        this.shopNotificationData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(shopNotificationData: ShopNotificationData) {
        val size: Int = itemCount
        this.shopNotificationData.items.addAll(shopNotificationData.items)

        this.shopNotificationData.isLoading = false
        this.shopNotificationData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_shop_notification, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = shopNotificationData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return shopNotificationData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        private val imageViewGoods: ImageView = view.findViewById(R.id.imageViewGoods)
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        private val textViewContent: TextView = view.findViewById(R.id.textViewContent)
        private val textViewTime: TextView = view.findViewById(R.id.textViewTime)

        override fun display(item: ShopNotificationListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()

            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, offSetDIP, 0, 0)

            // 상품 이미지
            Glide.with(context)
                    .load(item.goodsImage)
                    .apply(glideOption)
                    .into(imageViewGoods)

            // 내용 텍스트 value
            var titleStr = ""
            var contentStr = ""
            when (item.type) {
                AppConstants.SHOP_NOTIFICATION_LEAD_DECIDE -> { // lead decide
                    titleStr = context.resources.getString(R.string.noti_shop_lead_decide)
                    contentStr = context.resources.getString(R.string.noti_shop_lead_decide2)
                }
                AppConstants.SHOP_NOTIFICATION_LEAD_RATING -> { // lead rating
                    titleStr = context.resources.getString(R.string.noti_shop_lead_rating)
                    contentStr = context.resources.getString(R.string.noti_shop_lead_rating2)
                }
            }

            // notification title & 내용
            textViewTitle.setText(titleStr, TextView.BufferType.SPANNABLE)
            textViewContent.setText(contentStr, TextView.BufferType.SPANNABLE)

            // notification 시간
            textViewTime.setText(SupportData.contentDateForm(year = item.year, month = item.month, day = item.day, hour = item.hour, minute = item.minute), TextView.BufferType.SPANNABLE)

            // set click listener
            mainLayout.tag = item.goodsCode
            mainLayout.setOnClickListener(goodsListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: ShopNotificationListData, position: Int)
    }
}
