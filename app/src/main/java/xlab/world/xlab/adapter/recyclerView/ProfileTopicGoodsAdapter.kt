package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
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
import xlab.world.xlab.data.adapter.ProfileTopicGoodsData
import xlab.world.xlab.data.adapter.ProfileTopicGoodsListData
import xlab.world.xlab.data.adapter.ProfileTopicListData
import xlab.world.xlab.utils.support.AppConstants

class ProfileTopicGoodsAdapter(private val context: Context,
                               private val moreItemListener: View.OnClickListener?,
                               private val goodsListener: View.OnClickListener) : RecyclerView.Adapter<ProfileTopicGoodsAdapter.ViewHolder>() {

    private val profileTopicGoodsData: ProfileTopicGoodsData = ProfileTopicGoodsData()
    var dataLoading: Boolean
        get() = this.profileTopicGoodsData.isLoading
        set(value) { this.profileTopicGoodsData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.profileTopicGoodsData.total
    var dataNextPage: Int = 1
        get() = this.profileTopicGoodsData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun updateData(profileTopicGoodsData: ProfileTopicGoodsData) {
        this.profileTopicGoodsData.items.clear()
        this.profileTopicGoodsData.items.addAll(profileTopicGoodsData.items)

        this.profileTopicGoodsData.isLoading = false
        this.profileTopicGoodsData.total = profileTopicGoodsData.total
        this.profileTopicGoodsData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(profileTopicGoodsData: ProfileTopicGoodsData) {
        val size: Int = itemCount
        this.profileTopicGoodsData.items.addAll(profileTopicGoodsData.items)

        this.profileTopicGoodsData.isLoading = false
        this.profileTopicGoodsData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override fun getItemViewType(position: Int): Int {
        return this.profileTopicGoodsData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            AppConstants.ADAPTER_HEADER -> HeaderViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_profile_topic_goods_header, parent, false))

            AppConstants.ADAPTER_CONTENT -> ContentViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_thumb, parent, false))

            else -> ContentViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_thumb, parent, false))
        }
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = profileTopicGoodsData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return profileTopicGoodsData.items.size
    }

    // header View Holder
    inner class HeaderViewHolderBind(view: View) : ViewHolder(view) {
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        private val moreLayout: LinearLayout = view.findViewById(R.id.moreLayout)

        override fun display(item: ProfileTopicGoodsListData, position: Int) {
            textViewTitle.setText(item.headerTitle, TextView.BufferType.SPANNABLE)

            // 더보기 이벤트 있는경우 리스너 설정 & 보이기
            moreItemListener?.let {
                moreLayout.visibility = View.VISIBLE
                moreLayout.setOnClickListener(moreItemListener)
            } ?:let {
                moreLayout.visibility = View.GONE
            }
        }
    }

    // content View Holder
    inner class ContentViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageViewGoodsThumb: ImageView = view.findViewById(R.id.imageViewGoodsThumb)
        private val imageViewGoodsTag: ImageView = view.findViewById(R.id.imageViewGoodsTag)
        private val percentLayout: LinearLayout = view.findViewById(R.id.percentLayout)

        override fun display(item: ProfileTopicGoodsListData, position: Int) {
            // goods tag 이미지
            imageViewGoodsTag.visibility = View.GONE

            // goods 이미지
            Glide.with(context)
                    .load(item.goodsImage)
                    .apply(glideOption)
                    .into(imageViewGoodsThumb)

            // 인기율 안보기 -> % 가림
            percentLayout.visibility = View.GONE

            // goods 터치 이벤트
            mainLayout.tag = item.goodsCd
            mainLayout.setOnClickListener(goodsListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: ProfileTopicGoodsListData, position: Int)
    }
}
