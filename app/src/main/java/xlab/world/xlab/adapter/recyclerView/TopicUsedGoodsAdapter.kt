package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.AppConstants

class TopicUsedGoodsAdapter(private val context: Context,
                            private val goodsListener: View.OnClickListener) : RecyclerView.Adapter<TopicUsedGoodsAdapter.ViewHolder>() {

    private val topicUsedGoodsData: TopicUsedGoodsData = TopicUsedGoodsData()
    var dataLoading: Boolean
        get() = this.topicUsedGoodsData.isLoading
        set(value) { this.topicUsedGoodsData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.topicUsedGoodsData.total
    var dataNextPage: Int = 1
        get() = this.topicUsedGoodsData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)


    fun getItem(position: Int): TopicUsedGoodsListData {
        return topicUsedGoodsData.items[position]
    }

    fun updateData(topicUsedGoodsData: TopicUsedGoodsData) {
        this.topicUsedGoodsData.items.clear()
        this.topicUsedGoodsData.items.addAll(topicUsedGoodsData.items)

        this.topicUsedGoodsData.isLoading = false
        this.topicUsedGoodsData.total = topicUsedGoodsData.total
        this.topicUsedGoodsData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(topicUsedGoodsData: TopicUsedGoodsData) {
        val size: Int = itemCount
        this.topicUsedGoodsData.items.addAll(topicUsedGoodsData.items)

        this.topicUsedGoodsData.isLoading = false
        this.topicUsedGoodsData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_topic_used_goods, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = topicUsedGoodsData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return topicUsedGoodsData.items.size
    }

    // view holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageView: ImageView = view.findViewById(R.id.imageView)
        private val imageViewRating: ImageView = view.findViewById(R.id.imageViewRating)

        override fun display(item: TopicUsedGoodsListData, position: Int) {
            Glide.with(context)
                    .load(item.imageURL)
                    .apply(glideOption)
                    .into(imageView)

            val ratingDrawable = when (item.rating) {
                AppConstants.GOODS_RATING_GOOD -> ResourcesCompat.getDrawable(context.resources, R.drawable.rating_good, null)
                AppConstants.GOODS_RATING_SOSO -> ResourcesCompat.getDrawable(context.resources, R.drawable.rating_soso, null)
                AppConstants.GOODS_RATING_BAD -> ResourcesCompat.getDrawable(context.resources, R.drawable.rating_bad, null)
                else -> null
            }
            imageViewRating.setImageDrawable(ratingDrawable)

            mainLayout.tag = item.goodsCode
            mainLayout.setOnClickListener(goodsListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: TopicUsedGoodsListData, position: Int)
    }
}
