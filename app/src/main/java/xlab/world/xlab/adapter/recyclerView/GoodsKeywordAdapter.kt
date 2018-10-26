package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.TypedValue
import android.view.Gravity
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
import xlab.world.xlab.data.adapter.GoodsKeywordData
import xlab.world.xlab.data.adapter.GoodsKeywordListData
import xlab.world.xlab.data.adapter.SearchGoodsData
import xlab.world.xlab.data.adapter.SearchGoodsListData
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.utils.view.hashTag.EditTextTagHelper

class GoodsKeywordAdapter(private val context: Context,
                          private val selectListener: View.OnClickListener,
                          private val moreListener: View.OnClickListener) : RecyclerView.Adapter<GoodsKeywordAdapter.ViewHolder>() {

    private var goodsKeywordData: GoodsKeywordData = GoodsKeywordData()
    private val showKeywordData: GoodsKeywordData = GoodsKeywordData()

    var dataLoading: Boolean
        get() = this.goodsKeywordData.isLoading
        set(value) { this.goodsKeywordData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.goodsKeywordData.total
    var dataNextPage: Int = 1
        get() = this.goodsKeywordData.nextPage

    fun getItem(position: Int): GoodsKeywordListData = showKeywordData.items[position]

    fun linkData(goodsKeywordData: GoodsKeywordData) {
        this.goodsKeywordData = goodsKeywordData

        this.showKeywordData.items.clear()

        (0 until 7).forEach { index ->
            if (index < this.goodsKeywordData.items.size) {
                this.showKeywordData.items.add(this.goodsKeywordData.items[index])
            }
        }
        // 보여진 키워드 이외 남은 키워드 있으면 ">" 화살표 표시
        if (this.goodsKeywordData.items.size > this.showKeywordData.items.size)
            this.showKeywordData.items.add(GoodsKeywordListData(dataType = AppConstants.ADAPTER_FOOTER))


        notifyDataSetChanged()
    }

    fun showMoreData() {
        val startIndex = this.showKeywordData.items.size - 1
        val endIndex = startIndex + 5
        if (startIndex <= this.goodsKeywordData.items.size) {
            (startIndex until endIndex).forEach { index ->
                if (index < this.goodsKeywordData.items.size) {
                    this.showKeywordData.items.add(index, this.goodsKeywordData.items[index])
                }
            }

            // 보여진 키워드 이외 남은 키워드 없으면 > 화살표 삭제
            if (this.goodsKeywordData.items.size < this.showKeywordData.items.size)
                this.showKeywordData.items.removeAt(this.showKeywordData.items.size - 1)

            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return this.showKeywordData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            AppConstants.ADAPTER_CONTENT -> ContentViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_goods_keyword_content, parent, false))

            AppConstants.ADAPTER_FOOTER -> FooterViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_goods_keyword_footer, parent, false))

            else -> ContentViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_goods_keyword_content, parent, false))
        }
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = goodsKeywordData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return showKeywordData.items.size
    }

    // content view holder
    inner class ContentViewHolderBind(view: View): ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.textView)
        override fun display(item: GoodsKeywordListData, position: Int) {
            val offSetDIP = when (position) {
                0 -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics).toInt()
                1 -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36f, context.resources.displayMetrics).toInt()
                else -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            }
            (textView.layoutParams as RecyclerView.LayoutParams).setMargins(offSetDIP, 0, 0, 0)
            textView.setText(item.keywordText, TextView.BufferType.SPANNABLE)

            textView.tag = EditTextTagHelper.SearchData(text = item.keywordText, code = item.keywordCode)
            textView.setOnClickListener(selectListener)
        }
    }

    // footer view holder
    inner class FooterViewHolderBind(view: View): ViewHolder(view) {
        private val mainLayout: FrameLayout = view.findViewById(R.id.mainLayout)
        override fun display(item: GoodsKeywordListData, position: Int) {
            val offSetDIP = when (position) {
                0 -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics).toInt()
                1 -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36f, context.resources.displayMetrics).toInt()
                else -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            }
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(offSetDIP, 0, 0, 0)
            val layoutParams = mainLayout.layoutParams as StaggeredGridLayoutManager.LayoutParams
            layoutParams.isFullSpan = true

            mainLayout.setOnClickListener(moreListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: GoodsKeywordListData, position: Int)
    }
}
