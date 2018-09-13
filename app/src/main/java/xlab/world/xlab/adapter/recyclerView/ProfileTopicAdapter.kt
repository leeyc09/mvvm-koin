package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
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
import xlab.world.xlab.data.adapter.ProfileTopicData
import xlab.world.xlab.data.adapter.ProfileTopicListData
import xlab.world.xlab.utils.support.AppConstants

class ProfileTopicAdapter(private val context: Context,
                          private val addListener: View.OnClickListener,
                          private val selectListener: View.OnClickListener) : RecyclerView.Adapter<ProfileTopicAdapter.ViewHolder>() {

    private val profileTopicData: ProfileTopicData = ProfileTopicData()
    var dataLoading: Boolean
        get() = this.profileTopicData.isLoading
        set(value) { this.profileTopicData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.profileTopicData.total
    var dataNextPage: Int = 1
        get() = this.profileTopicData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun updateData(profileTopicData: ProfileTopicData) {
        this.profileTopicData.items.clear()
        this.profileTopicData.items.addAll(profileTopicData.items)

        this.profileTopicData.isLoading = false
        this.profileTopicData.total = profileTopicData.total
        this.profileTopicData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(profileTopicData: ProfileTopicData) {
        val size: Int = itemCount
        this.profileTopicData.items.addAll(profileTopicData.items)

        this.profileTopicData.isLoading = false
        this.profileTopicData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override fun getItemViewType(position: Int): Int {
        return this.profileTopicData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            AppConstants.ADAPTER_CONTENT -> ContentViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_profile_topic_content, parent, false))

            AppConstants.ADAPTER_FOOTER -> FooterViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_profile_topic_footer, parent, false))

            else -> ContentViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_profile_topic_content, parent, false))
        }
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = profileTopicData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return profileTopicData.items.size
    }

    // content View Holder
    inner class ContentViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageView: ImageView = view.findViewById(R.id.imageView)

        override fun display(item: ProfileTopicListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(offSetDIP, 0, 0, 0)

            // topic 이미지
            Glide.with(context)
                    .load(item.imageURL)
                    .apply(glideOption)
                    .into(imageView)

            // touch 이벤트
            mainLayout.tag = position + 1
            mainLayout.setOnClickListener(selectListener)
        }
    }

    // footer View Holder
    inner class FooterViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)

        override fun display(item: ProfileTopicListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(offSetDIP, 0, 0, 0)

            // touch 이벤트
            mainLayout.setOnClickListener(addListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: ProfileTopicListData, position: Int)
    }
}
