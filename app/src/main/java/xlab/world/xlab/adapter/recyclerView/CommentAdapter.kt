package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.CommentData
import xlab.world.xlab.data.adapter.CommentListData
import xlab.world.xlab.data.adapter.TopicSettingListData
import xlab.world.xlab.utils.support.SupportData

class CommentAdapter(private val context: Context,
                     private val profileListener: View.OnClickListener) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private var commentData: CommentData = CommentData()
    var dataLoading: Boolean
        get() = this.commentData.isLoading
        set(value) { this.commentData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.commentData.total
    var dataNextPage: Int = 1
        get() = this.commentData.nextPage

    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(R.drawable.profile_img_44)
            .error(R.drawable.profile_img_44)

    fun linkData(commentData: CommentData) {
        this.commentData = commentData
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_comment, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = commentData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return commentData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val profileLayout: FrameLayout = view.findViewById(R.id.profileLayout)
        private val imageViewProfile: ImageView = view.findViewById(R.id.imageViewProfile)
        private val textViewNickName: TextView = view.findViewById(R.id.textViewNickName)
        private val textViewDate: TextView = view.findViewById(R.id.textViewDate)
        private val textViewComment: TextView = view.findViewById(R.id.textViewComment)

        override fun display(item: CommentListData, position: Int) {
            // user profile 이미지
            Glide.with(context)
                    .load(item.userProfileUrl)
                    .apply(glideOption)
                    .into(imageViewProfile)

            // user nick name
            textViewNickName.setText(item.userNickName, TextView.BufferType.SPANNABLE)
            // comment 날짜
            textViewDate.setText(SupportData.contentDateForm(year = item.uploadYear, month = item.uploadMonth, day = item.uploadDay,
                    hour = item.uploadHour, minute = item.uploadMinute), TextView.BufferType.SPANNABLE)
            // comment 내용
            textViewComment.setText(item.comment, TextView.BufferType.SPANNABLE)

            // touch 이벤트
            profileLayout.tag = item.userId
            textViewNickName.tag = item.userId
            profileLayout.setOnClickListener(profileListener)
            textViewNickName.setOnClickListener(profileListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: CommentListData, position: Int)
    }
}
