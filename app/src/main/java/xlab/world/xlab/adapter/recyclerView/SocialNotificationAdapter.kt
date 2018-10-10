package xlab.world.xlab.adapter.recyclerView

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
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
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SupportData

class SocialNotificationAdapter(private val context: Context,
                                private val profileListener: View.OnClickListener,
                                private val postListener: View.OnClickListener,
                                private val commentListener: View.OnClickListener) : RecyclerView.Adapter<SocialNotificationAdapter.ViewHolder>() {
    private val fontColorSpan: FontColorSpan by (context as Activity).inject()

    private val socialNotificationData: SocialNotificationData = SocialNotificationData()
    var dataLoading: Boolean
        get() = this.socialNotificationData.isLoading
        set(value) { this.socialNotificationData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.socialNotificationData.total
    var dataNextPage: Int = 1
        get() = this.socialNotificationData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val profileGlideOption = RequestOptions()
            .circleCrop()
            .placeholder(R.drawable.profile_img_44)
            .error(R.drawable.profile_img_44)
    private val postGlideOption = RequestOptions()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun getItem(position: Int): SocialNotificationListData {
        return socialNotificationData.items[position]
    }

    fun updateData(socialNotificationData: SocialNotificationData) {
        this.socialNotificationData.items.clear()
        this.socialNotificationData.items.addAll(socialNotificationData.items)

        this.socialNotificationData.isLoading = false
        this.socialNotificationData.total = socialNotificationData.total
        this.socialNotificationData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(socialNotificationData: SocialNotificationData) {
        val size: Int = itemCount
        this.socialNotificationData.items.addAll(socialNotificationData.items)

        this.socialNotificationData.isLoading = false
        this.socialNotificationData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_social_notification, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = socialNotificationData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return socialNotificationData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        private val profileLayout: FrameLayout = view.findViewById(R.id.profileLayout)
        private val imageViewProfile: ImageView = view.findViewById(R.id.imageViewProfile)
        private val textViewContent: TextView = view.findViewById(R.id.textViewContent)
        private val textViewTime: TextView = view.findViewById(R.id.textViewTime)
        private val thumbnailLayout: FrameLayout = view.findViewById(R.id.thumbnailLayout)
        private val imageViewPostsThumb: ImageView = view.findViewById(R.id.imageViewPostsThumb)

        override fun display(item: SocialNotificationListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()

            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, offSetDIP, 0, 0)

            // 프로필 이미지
            Glide.with(context)
                    .load(item.userProfileUrl)
                    .apply(profileGlideOption)
                    .into(imageViewProfile)

            // 내용 텍스트 value & 포스트 썸네일
            var contentStr = "${item.userNick} "
            when (item.type) {
                AppConstants.SOCIAL_NOTIFICATION_LIKE_POST,
                AppConstants.SOCIAL_NOTIFICATION_POST_COMMENT,
                AppConstants.SOCIAL_NOTIFICATION_TAG -> { // like post, post comment, post user tag
                    thumbnailLayout.visibility = View.VISIBLE
                    Glide.with(context)
                            .load(item.postImageUrl)
                            .apply(postGlideOption)
                            .into(imageViewPostsThumb)

                    when (item.type) {
                        AppConstants.SOCIAL_NOTIFICATION_LIKE_POST-> { // like post
                            contentStr += context.resources.getString(R.string.noti_social_like_post)
                        }
                        AppConstants.SOCIAL_NOTIFICATION_POST_COMMENT -> { // post comment
                            contentStr += context.resources.getString(R.string.noti_social_post_comment)
                            val comment =
                                    if (item.commentContent.length > 15) "${item.commentContent.substring(0, 15)}..."
                                    else item.commentContent
                            contentStr += "\"$comment\""
                        }
                        AppConstants.SOCIAL_NOTIFICATION_TAG -> { // post user tag
                            contentStr += context.resources.getString(R.string.noti_social_tag)
                        }
                    }
                }
                AppConstants.SOCIAL_NOTIFICATION_FOLLOW -> { // user follow
                    thumbnailLayout.visibility = View.INVISIBLE
                    contentStr += context.resources.getString(R.string.noti_social_follow)
                }
            }

            // notification 내용
            val contentSpannableStr = SpannableString(contentStr)
            contentSpannableStr.setSpan(fontColorSpan.notoBold000000, 0, item.userNick.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            contentSpannableStr.setSpan(fontColorSpan.notoRegular000000, item.userNick.length, contentSpannableStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textViewContent.setText(contentSpannableStr, TextView.BufferType.SPANNABLE)

            // notification 시간
            textViewTime.setText(SupportData.contentDateForm(year = item.year, month = item.month, day = item.day, hour = item.hour, minute = item.minute), TextView.BufferType.SPANNABLE)

            // set click listener
            when (item.type) {
                AppConstants.SOCIAL_NOTIFICATION_LIKE_POST -> { // like post
                    mainLayout.tag = item.postId
                    mainLayout.setOnClickListener(postListener)
                }
                AppConstants.SOCIAL_NOTIFICATION_POST_COMMENT -> { // post comment
                    mainLayout.tag = item.postId
                    mainLayout.setOnClickListener(commentListener)
                }
                AppConstants.SOCIAL_NOTIFICATION_TAG -> { // post user tag
                    mainLayout.tag = item.postId
                    mainLayout.setOnClickListener(postListener)
                }
                AppConstants.SOCIAL_NOTIFICATION_FOLLOW -> { // user follow
                    mainLayout.tag = item.userId
                    mainLayout.setOnClickListener(profileListener)
                }
            }

            profileLayout.tag = item.userId
            profileLayout.setOnClickListener(profileListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: SocialNotificationListData, position: Int)
    }
}
