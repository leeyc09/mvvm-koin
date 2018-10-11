package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
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
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*

class GoodsDetailTopicMatchAdapter(private val context: Context) : RecyclerView.Adapter<GoodsDetailTopicMatchAdapter.ViewHolder>() {

    private val goodsDetailTopicMatchData: GoodsDetailTopicMatchData = GoodsDetailTopicMatchData()
    var dataLoading: Boolean
        get() = this.goodsDetailTopicMatchData.isLoading
        set(value) { this.goodsDetailTopicMatchData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.goodsDetailTopicMatchData.total
    var dataNextPage: Int = 1
        get() = this.goodsDetailTopicMatchData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun getItem(position: Int): GoodsDetailTopicMatchListData {
        return goodsDetailTopicMatchData.items[position]
    }

    fun updateData(goodsDetailTopicMatchData: GoodsDetailTopicMatchData) {
        this.goodsDetailTopicMatchData.items.clear()
        this.goodsDetailTopicMatchData.items.addAll(goodsDetailTopicMatchData.items)

        this.goodsDetailTopicMatchData.isLoading = false
        this.goodsDetailTopicMatchData.total = goodsDetailTopicMatchData.total
        this.goodsDetailTopicMatchData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(goodsDetailTopicMatchData: GoodsDetailTopicMatchData) {
        val size: Int = itemCount
        this.goodsDetailTopicMatchData.items.addAll(goodsDetailTopicMatchData.items)

        this.goodsDetailTopicMatchData.isLoading = false
        this.goodsDetailTopicMatchData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_detail_topic_match, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = goodsDetailTopicMatchData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return goodsDetailTopicMatchData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        private val imageView: ImageView = view.findViewById(R.id.imageView)
        private val imageViewOverlay: ImageView = view.findViewById(R.id.imageViewOverlay)
        private val textViewMatch: TextView = view.findViewById(R.id.textViewMatch)
        private val textViewName: TextView = view.findViewById(R.id.textViewName)

        override fun display(item: GoodsDetailTopicMatchListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()

            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(offSetDIP, 0, 0, 0)

            // topic profile 이미지
            Glide.with(context)
                    .load(item.imageURL)
                    .apply(glideOption)
                    .into(imageView)

            // topic color
            val topicColorDrawable = ColorDrawable(item.matchColor)
            Glide.with(context)
                    .load(topicColorDrawable)
                    .apply(glideOption)
                    .into(imageViewOverlay)

            // topic 이름
            textViewName.setText(item.petName, TextView.BufferType.SPANNABLE)
            textViewName.isSelected = item.matchingPercent < 50

            // 매칭율이 50% 이상인 경우만 수치 노출
            if (item.matchingPercent < 50) {
                textViewMatch.visibility = View.GONE
            } else {
                textViewMatch.visibility = View.VISIBLE
                textViewMatch.setText(String.format("${item.matchingPercent}%%"), TextView.BufferType.SPANNABLE)
            }
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: GoodsDetailTopicMatchListData, position: Int)
    }
}
