package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.AppConstants

class GoodsRatingAdapter(private val context: Context,
                         private val ratingListener: View.OnClickListener) : RecyclerView.Adapter<GoodsRatingAdapter.ViewHolder>() {

    private var goodsRatingData: GoodsRatingData = GoodsRatingData()
    var dataLoading: Boolean
        get() = this.goodsRatingData.isLoading
        set(value) { this.goodsRatingData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.goodsRatingData.total
    var dataNextPage: Int = 1
        get() = this.goodsRatingData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun linkData(goodsRatingData: GoodsRatingData) {
        this.goodsRatingData = goodsRatingData
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_rating, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = goodsRatingData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return goodsRatingData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        private val imageViewTopic: ImageView = view.findViewById(R.id.imageViewTopic)
        private val textViewName: TextView = view.findViewById(R.id.textViewName)
        private val imageViewRatingBad: ImageView = view.findViewById(R.id.imageViewRatingBad)
        private val imageViewRatingSoSo: ImageView = view.findViewById(R.id.imageViewRatingSoSo)
        private val imageViewRatingGood: ImageView = view.findViewById(R.id.imageViewRatingGood)

        override fun display(item: GoodsRatingListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24.5f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()

            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, offSetDIP, 0, 0)

            // 토픽 프로필 이미지 & 이름
            Glide.with(context)
                    .load(item.petImage)
                    .apply(glideOption)
                    .into(imageViewTopic)
            textViewName.setText(item.petName, TextView.BufferType.SPANNABLE)

            // rating set
            imageViewRatingBad.isSelected = item.rating == AppConstants.GOODS_RATING_BAD
            imageViewRatingSoSo.isSelected = item.rating == AppConstants.GOODS_RATING_SOSO
            imageViewRatingGood.isSelected = item.rating == AppConstants.GOODS_RATING_GOOD

            imageViewRatingBad.tag = RatingTag(AppConstants.GOODS_RATING_BAD, position)
            imageViewRatingBad.setOnClickListener(ratingListener)

            imageViewRatingSoSo.tag = RatingTag(AppConstants.GOODS_RATING_SOSO, position)
            imageViewRatingSoSo.setOnClickListener(ratingListener)

            imageViewRatingGood.tag = RatingTag(AppConstants.GOODS_RATING_GOOD, position)
            imageViewRatingGood.setOnClickListener(ratingListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: GoodsRatingListData, position: Int)
    }
    data class RatingTag(val rating: Int, val position: Int)
}
