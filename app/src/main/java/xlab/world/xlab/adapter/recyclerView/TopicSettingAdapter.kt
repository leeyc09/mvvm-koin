package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.Color
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.TopicSettingData
import xlab.world.xlab.data.adapter.TopicSettingListData

class TopicSettingAdapter(private val context: Context,
                          private val topicToggleListener: View.OnClickListener) : RecyclerView.Adapter<TopicSettingAdapter.ViewHolder>() {

    private var topicSettingData: TopicSettingData = TopicSettingData()
    var dataLoading: Boolean
        get() = this.topicSettingData.isLoading
        set(value) { this.topicSettingData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.topicSettingData.total
    var dataNextPage: Int = 1
        get() = this.topicSettingData.nextPage

    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(R.drawable.profile_image_default)
            .error(R.drawable.profile_image_default)

    fun linkData(topicSettingData: TopicSettingData) {
        this.topicSettingData = topicSettingData
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_topic_setting, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = topicSettingData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return topicSettingData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val imageViewProfileImage: ImageView = view.findViewById(R.id.imageViewProfileImage)
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        private val topicToggleBtn: ImageView = view.findViewById(R.id.topicToggleBtn)

        override fun display(item: TopicSettingListData, position: Int) {
            // topic 이미지
            Glide.with(context)
                    .load(item.topicImage)
                    .apply(glideOption)
                    .into(imageViewProfileImage)

            // topic 타이틀 (pet - 이름)
            textViewTitle.setText(item.title, TextView.BufferType.SPANNABLE)
            textViewTitle.setTextColor(Color.parseColor("#${item.topicColor}"))

            topicToggleBtn.isSelected = !item.isHidden

            topicToggleBtn.tag = position
            topicToggleBtn.setOnClickListener(topicToggleListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: TopicSettingListData, position: Int)
    }
}
