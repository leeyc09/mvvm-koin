package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.constraint.Guideline
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
import xlab.world.xlab.utils.support.PrintLog

class GoodsDetailStatsAdapter(private val context: Context) : RecyclerView.Adapter<GoodsDetailStatsAdapter.ViewHolder>() {

    private val goodsDetailStatsData: GoodsDetailStatsData = GoodsDetailStatsData()
    var dataLoading: Boolean
        get() = this.goodsDetailStatsData.isLoading
        set(value) { this.goodsDetailStatsData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.goodsDetailStatsData.total
    var dataNextPage: Int = 1
        get() = this.goodsDetailStatsData.nextPage

    private val topicBackgroundDrawable = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorFFFFFF, null))
    private val moreOverlayDrawable = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorBlack40, null))
    private val glideOption = RequestOptions().circleCrop()

    fun getItem(position: Int): GoodsDetailStatsListData {
        return goodsDetailStatsData.items[position]
    }

    fun updateData(goodsDetailStatsData: GoodsDetailStatsData) {
        this.goodsDetailStatsData.items.clear()
        this.goodsDetailStatsData.items.addAll(goodsDetailStatsData.items)

        this.goodsDetailStatsData.isLoading = false
        this.goodsDetailStatsData.total = goodsDetailStatsData.total
        this.goodsDetailStatsData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(goodsDetailStatsData: GoodsDetailStatsData) {
        val size: Int = itemCount
        this.goodsDetailStatsData.items.addAll(goodsDetailStatsData.items)

        this.goodsDetailStatsData.isLoading = false
        this.goodsDetailStatsData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_detail_stats, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = goodsDetailStatsData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return goodsDetailStatsData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        private val topicImageLayout: FrameLayout = view.findViewById(R.id.topicImageLayout)
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)

        private val imageViewGood: ImageView = view.findViewById(R.id.imageViewGood)
        private val goodPercentLayout: ConstraintLayout = view.findViewById(R.id.goodPercentLayout)
        private val goodPercentGuideline: Guideline = view.findViewById(R.id.goodPercentGuideline)
        private val goodPercentBar: View = view.findViewById(R.id.goodPercentBar)
        private val textViewGoodPercent: TextView = view.findViewById(R.id.textViewGoodPercent)
        private val textViewGoodPercentUnit: TextView = view.findViewById(R.id.textViewGoodPercentUnit)

        private val imageViewSoso: ImageView = view.findViewById(R.id.imageViewSoso)
        private val sosoPercentLayout: ConstraintLayout = view.findViewById(R.id.sosoPercentLayout)
        private val sosoPercentGuideline: Guideline = view.findViewById(R.id.sosoPercentGuideline)
        private val sosoPercentBar: View = view.findViewById(R.id.sosoPercentBar)
        private val textViewSosoPercent: TextView = view.findViewById(R.id.textViewSosoPercent)
        private val textViewSosoPercentUnit: TextView = view.findViewById(R.id.textViewSosoPercentUnit)

        private val imageViewBad: ImageView = view.findViewById(R.id.imageViewBad)
        private val badPercentLayout: ConstraintLayout = view.findViewById(R.id.badPercentLayout)
        private val badPercentGuideline: Guideline = view.findViewById(R.id.badPercentGuideline)
        private val badPercentBar: View = view.findViewById(R.id.badPercentBar)
        private val textViewBadPercent: TextView = view.findViewById(R.id.textViewBadPercent)
        private val textViewBadPercentUnit: TextView = view.findViewById(R.id.textViewBadPercentUnit)

        private val noStatsLayout: TextView = view.findViewById(R.id.noStatsLayout)

        override fun display(item: GoodsDetailStatsListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 31f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, offSetDIP, 0, 0)

            // 통계 타이틀
            textViewTitle.setText(item.title, TextView.BufferType.SPANNABLE)

            // 통계 %
            textViewGoodPercent.setText(item.goodPercent.toString(), TextView.BufferType.SPANNABLE)
            textViewSosoPercent.setText(item.sosoPercent.toString(), TextView.BufferType.SPANNABLE)
            textViewBadPercent.setText(item.badPercent.toString(), TextView.BufferType.SPANNABLE)

            // set topic image
            if (item.topicImages == null) { // no image
                topicImageLayout.visibility = View.GONE
            } else { // exit image
                topicImageLayout.visibility = View.VISIBLE

                run topicImages@{
                    item.topicImages.forEachIndexed { index, image ->
                        val newView =
                                if (index < 4) createTopicView(topicImageLayout, index, image, null)
                                else createTopicView(topicImageLayout, index, image, if (item.topicImages.size - 5 > 0) item.topicImages.size - 5 else null)
                        topicImageLayout.addView(newView)

                        if (index >= 4) return@topicImages
                    }
                }
            }
            // 100% -> ratio 0.8
            val goodRatio = item.goodPercent * 0.008f
            val sosoRatio = item.sosoPercent * 0.008f
            val badRatio = item.badPercent * 0.008f
            goodPercentLayout.visibility =
                    if (goodRatio == 0f) View.INVISIBLE
                    else View.VISIBLE

            sosoPercentLayout.visibility =
                    if (sosoRatio == 0f) View.INVISIBLE
                    else View.VISIBLE

            badPercentLayout.visibility =
                    if (badRatio == 0f) View.INVISIBLE
                    else View.VISIBLE

            goodPercentGuideline.setGuidelinePercent(goodRatio)
            sosoPercentGuideline.setGuidelinePercent(sosoRatio)
            badPercentGuideline.setGuidelinePercent(badRatio)

            val biggest = Math.max(goodRatio, Math.max(sosoRatio, badRatio))
            val twoDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics).toInt()
            val threeDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context.resources.displayMetrics).toInt()

            // percent bar 색상, 사이즈
            imageViewGood.isSelected = biggest == goodRatio && goodRatio != 0f
            goodPercentBar.isSelected = biggest == goodRatio && goodRatio != 0f
            goodPercentBar.layoutParams.height = if (biggest == goodRatio && goodRatio != 0f) threeDIP else twoDIP
            textViewGoodPercent.isSelected = biggest == goodRatio && goodRatio != 0f
            textViewGoodPercent.textSize = if (biggest == goodRatio && goodRatio != 0f) 18f else 13f
            textViewGoodPercentUnit.isSelected = biggest == goodRatio && goodRatio != 0f

            imageViewSoso.isSelected = biggest == sosoRatio && sosoRatio != 0f
            sosoPercentBar.isSelected = biggest == sosoRatio && sosoRatio != 0f
            sosoPercentBar.layoutParams.height = if (biggest == sosoRatio && sosoRatio != 0f) threeDIP else twoDIP
            textViewSosoPercent.isSelected = biggest == sosoRatio && sosoRatio != 0f
            textViewSosoPercent.textSize = if (biggest == sosoRatio && sosoRatio != 0f) 18f else 13f
            textViewSosoPercentUnit.isSelected = biggest == sosoRatio && sosoRatio != 0f

            imageViewBad.isSelected = biggest == badRatio && badRatio != 0f
            badPercentBar.isSelected = biggest == badRatio && badRatio != 0f
            badPercentBar.layoutParams.height = if (biggest == badRatio && badRatio != 0f) threeDIP else twoDIP
            textViewBadPercent.isSelected = biggest == badRatio && badRatio != 0f
            textViewBadPercent.textSize = if (biggest == badRatio && badRatio != 0f) 18f else 13f
            textViewBadPercentUnit.isSelected = biggest == badRatio && badRatio != 0f

            // 평가 데이터 부족 텍스트
            noStatsLayout.visibility =
                    if (goodRatio + sosoRatio + badRatio == 0f) View.VISIBLE
                    else View.GONE
        }

        private fun createTopicView(parent: ViewGroup, index: Int, image: String, num: Int?): FrameLayout {
            val newView = LayoutInflater.from(context).inflate(R.layout.item_goods_detail_stats_topic_image, parent, false) as FrameLayout
            val offSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f*index, context.resources.displayMetrics).toInt()
            (newView.layoutParams as FrameLayout.LayoutParams).setMargins(offSetDIP, 0, 0, 0)

            val topicImageView = newView.findViewById<ImageView>(R.id.topicImageView)
            val topicImageViewBack = newView.findViewById<ImageView>(R.id.topicImageViewBack)
            val moreOverlay = newView.findViewById<ImageView>(R.id.moreOverlay)
            val textViewMore = newView.findViewById<TextView>(R.id.textViewMore)

            // set topic image
            Glide.with(context)
                    .load(image)
                    .apply(glideOption)
                    .into(topicImageView)

            // set topic image background
            Glide.with(context)
                    .load(topicBackgroundDrawable)
                    .apply(glideOption)
                    .into(topicImageViewBack)

            if (num == null) {
                moreOverlay.visibility = View.GONE
                textViewMore.visibility = View.GONE
            } else {
                moreOverlay.visibility = View.VISIBLE
                textViewMore.visibility = View.VISIBLE
                // set more overlay
                Glide.with(context)
                        .load(moreOverlayDrawable)
                        .apply(glideOption)
                        .into(moreOverlay)

                textViewMore.setText(String.format("+$num"), TextView.BufferType.SPANNABLE)
            }

            return newView
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: GoodsDetailStatsListData, position: Int)
    }
}
