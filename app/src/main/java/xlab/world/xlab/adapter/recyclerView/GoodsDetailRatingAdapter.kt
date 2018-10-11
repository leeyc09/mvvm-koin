package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.AppConstants

class GoodsDetailRatingAdapter(private val context: Context,
                               private val ratingListener: View.OnClickListener) : RecyclerView.Adapter<GoodsDetailRatingAdapter.ViewHolder>() {

    private val goodsDetailRatingData: GoodsDetailRatingData = GoodsDetailRatingData()
    var dataLoading: Boolean
        get() = this.goodsDetailRatingData.isLoading
        set(value) { this.goodsDetailRatingData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.goodsDetailRatingData.total
    var dataNextPage: Int = 1
        get() = this.goodsDetailRatingData.nextPage

    fun getItem(position: Int): GoodsDetailRatingListData {
        return goodsDetailRatingData.items[position]
    }

    fun updateData(goodsDetailRatingData: GoodsDetailRatingData) {
        this.goodsDetailRatingData.items.clear()
        this.goodsDetailRatingData.items.addAll(goodsDetailRatingData.items)

        this.goodsDetailRatingData.isLoading = false
        this.goodsDetailRatingData.total = goodsDetailRatingData.total
        this.goodsDetailRatingData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(goodsDetailRatingData: GoodsDetailRatingData) {
        val size: Int = itemCount
        this.goodsDetailRatingData.items.addAll(goodsDetailRatingData.items)

        this.goodsDetailRatingData.isLoading = false
        this.goodsDetailRatingData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_detail_rating, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = goodsDetailRatingData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return goodsDetailRatingData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        private val textViewName: TextView = view.findViewById(R.id.textViewName)
        private val ratingBadBtn: ImageView = view.findViewById(R.id.ratingBadBtn)
        private val ratingSoSoBtn: ImageView = view.findViewById(R.id.ratingSoSoBtn)
        private val ratingGoodBtn: ImageView = view.findViewById(R.id.ratingGoodBtn)

        override fun display(item: GoodsDetailRatingListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()

            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, offSetDIP, 0, 0)

            // topic 이름 & 색상
            textViewName.setText(item.petName, TextView.BufferType.SPANNABLE)
            textViewName.setTextColor(item.topicColor)

            // rating 초기화
            ratingBadBtn.isSelected = item.rating == 0
            ratingSoSoBtn.isSelected = item.rating == 1
            ratingGoodBtn.isSelected = item.rating == 2

            ratingBadBtn.tag = RatingTag(AppConstants.GOODS_RATING_BAD, position)
            ratingBadBtn.setOnClickListener(ratingListener)

            ratingSoSoBtn.tag = RatingTag(AppConstants.GOODS_RATING_SOSO, position)
            ratingSoSoBtn.setOnClickListener(ratingListener)

            ratingGoodBtn.tag = RatingTag(AppConstants.GOODS_RATING_GOOD, position)
            ratingGoodBtn.setOnClickListener(ratingListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: GoodsDetailRatingListData, position: Int)
    }
    data class RatingTag(val rating: Int, val position: Int)
}
