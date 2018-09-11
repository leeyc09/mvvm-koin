package xlab.world.xlab.adapter.recyclerView

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.adapter.viewPager.ViewImagePagerAdapter
import xlab.world.xlab.data.adapter.AllFeedData
import xlab.world.xlab.data.adapter.AllFeedListData
import xlab.world.xlab.data.adapter.PostDetailData
import xlab.world.xlab.data.adapter.PostDetailListData
import xlab.world.xlab.utils.font.CustomFont
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.utils.support.ViewFunction
import java.util.ArrayList

class PostDetailAdapter(private val context: Context,
                        private val profileListener: View.OnClickListener?,
                        private val followListener: View.OnClickListener?,
                        private val moreListener: View.OnClickListener?,
                        private val likePostListener: View.OnClickListener,
                        private val commentsListener: View.OnClickListener,
                        private val savePostListener: View.OnClickListener,
                        private val sharePostListener: View.OnClickListener) : RecyclerView.Adapter<PostDetailAdapter.ViewHolder>() {

    private val postDetailData: PostDetailData = PostDetailData()
    var dataLoading: Boolean
        get() = this.postDetailData.isLoading
        set(value) { this.postDetailData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.postDetailData.total
    var dataNextPage: Int = 1
        get() = this.postDetailData.nextPage

    private val contentLine = 7
    private val profileGlideOption = RequestOptions().circleCrop()
            .placeholder(R.drawable.profile_img_44)
            .error(R.drawable.profile_img_44)
    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val imageGlideOption = RequestOptions().centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)
    private val hashTagCharsColor = hashMapOf(AppConstants.HASH_TAG_SIGN to ResourcesCompat.getColor(context.resources, R.color.color000000, null))
    private val hashTagCharsFont = hashMapOf(AppConstants.HASH_TAG_SIGN to CustomFont.getTypeface(CustomFont.notoSansCJKkrBold, context)!!)
    private val additionalHashTagChar = ArrayList<Char>()

    fun getItem(position: Int): PostDetailListData {
        return postDetailData.items[position]
    }

    fun updateData(postDetailData: PostDetailData) {
        this.postDetailData.items.clear()
        this.postDetailData.items.addAll(postDetailData.items)

        this.postDetailData.isLoading = false
        this.postDetailData.total = postDetailData.total
        this.postDetailData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(postDetailData: PostDetailData) {
        val size: Int = itemCount
        this.postDetailData.items.addAll(postDetailData.items)

        this.postDetailData.isLoading = false
        this.postDetailData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override fun getItemViewType(position: Int): Int {
        return this.postDetailData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
//            AppConstants.ADAPTER_HEADER -> PostsViewHolderBind(LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_posts_thumb, parent, false))

            AppConstants.ADAPTER_CONTENT -> ContentViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_post_detail, parent, false))

            else -> ContentViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_post_detail, parent, false))
        }
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = postDetailData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return postDetailData.items.size
    }

    // View Holder for content
    inner class ContentViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: ScrollView = view.findViewById(R.id.mainLayout)
        // profile layout
        private val profileImageLayout: FrameLayout = view.findViewById(R.id.profileImageLayout)
        private val imageViewProfile: ImageView = view.findViewById(R.id.imageViewProfile)
        private val textViewNickName: TextView = view.findViewById(R.id.textViewNickName)
        private val textViewDate: TextView = view.findViewById(R.id.textViewDate)
        private val imageViewFollowing: ImageView = view.findViewById(R.id.imageViewFollowing)
        private val moreBtn: ImageView = view.findViewById(R.id.moreBtn)
        // content image
        private val youtubeThumbnailLayout: FrameLayout = view.findViewById(R.id.youtubeThumbnailLayout)
        private val youtubeThumbnailView: ImageView = view.findViewById(R.id.youtubeThumbnailView)
        private val imageViewPager: ViewPager = view.findViewById(R.id.imageViewPager)
        private val tabLayoutDot: TabLayout = view.findViewById(R.id.tabLayoutDot)
        // like, comment, save, share post layout
        private val socialLayout: ConstraintLayout = view.findViewById(R.id.socialLayout)
        private val likeLayout: LinearLayout = view.findViewById(R.id.likeLayout)
        private val imageViewLike: ImageView = view.findViewById(R.id.imageViewLike)
        private val textViewLikeNum: TextView = view.findViewById(R.id.textViewLikeNum)
        private val commentLayout: LinearLayout = view.findViewById(R.id.commentLayout)
        private val imageViewSave: ImageView = view.findViewById(R.id.imageViewSave)
        private val textViewCommentNum: TextView = view.findViewById(R.id.textViewCommentNum)
        private val savePostLayout: FrameLayout = view.findViewById(R.id.savePostLayout)
        private val sharePostLayout: FrameLayout = view.findViewById(R.id.sharePostLayout)
        // content
        private val textViewContent: TextView = view.findViewById(R.id.textViewContent)
        // goods
        private val goodsRecyclerView: RecyclerView = view.findViewById(R.id.goodsRecyclerView)

        override fun display(item: PostDetailListData, position: Int) {
            // post 간격 조절
            val offSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, 0, 0, offSetDIP)
            // post user profile 이미지
            Glide.with(context)
                    .load(item.userProfileURL)
                    .apply(profileGlideOption)
                    .into(imageViewProfile)
            // profile listener 존재할 경우 -> listener 설정
            profileListener?.let {
                profileImageLayout.tag = item.userId
                profileImageLayout.setOnClickListener(profileListener)
                textViewNickName.tag = item.userId
                textViewNickName.setOnClickListener(profileListener)
            }
            // post user nick name
            textViewNickName.setText(item.userNickName, TextView.BufferType.SPANNABLE)
            // post time
            val postTime = SupportData.contentDateForm(
                    year = item.uploadYear,
                    month = item.uploadMonth,
                    day = item.uploadDay,
                    hour = item.uploadHour,
                    minute = item.uploadMinute)
            textViewDate.setText(postTime, TextView.BufferType.SPANNABLE)
            // my post 또는 follow listener 없는경우 -> follow button 안보이게
            if (followListener == null || item.isMyPost) {
                imageViewFollowing.visibility = View.GONE
            } else {
                imageViewFollowing.visibility = View.VISIBLE
                imageViewFollowing.isSelected = item.isFollowing

                imageViewFollowing.tag = position
                imageViewFollowing.setOnClickListener(followListener)
            }
            // my post 아니거나 more listener 없는경우 -> more button 안보이게
            if (moreListener == null || !item.isMyPost) {
                moreBtn.visibility = View.GONE
            } else {
                moreBtn.visibility = View.VISIBLE

                moreBtn.tag = position
                moreBtn.setOnClickListener(moreListener)
            }

            // post image
            when (item.postType) {
                AppConstants.POSTS_IMAGE -> {
                    imageViewPager.visibility = View.VISIBLE
                    youtubeThumbnailLayout.visibility = View.GONE

                    val viewPagerAdapter = ViewImagePagerAdapter(context = context, imageUrlList = item.imageURL)
                    imageViewPager.adapter = viewPagerAdapter
                    ViewFunction.onViewPagerChangePosition(viewPager = imageViewPager) {
                        item.lastImageIndex = it
                    }
                    // dot indicator
                    tabLayoutDot.setupWithViewPager(imageViewPager, true)
                    if (item.imageURL.size > 1) { // 이미지가 2장 이상
                        tabLayoutDot.visibility = View.VISIBLE
                        for (i in 0 until tabLayoutDot.tabCount) {
                            val tab: View = (tabLayoutDot.getChildAt(0) as ViewGroup).getChildAt(i)
                            val params = tab.layoutParams as ViewGroup.MarginLayoutParams
                            val offSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context.resources.displayMetrics).toInt()
                            params.setMargins(offSetDIP, 0, offSetDIP, 0)
                            tab.requestLayout()
                        }
                    } else {
                        tabLayoutDot.visibility = View.INVISIBLE
                    }
                    imageViewPager.setCurrentItem(item.lastImageIndex, false)
                }
                AppConstants.POSTS_VIDEO -> {}
                AppConstants.POSTS_YOUTUBE_LINK -> {
                    imageViewPager.visibility = View.GONE
                    youtubeThumbnailLayout.visibility = View.VISIBLE
                    tabLayoutDot.visibility = View.INVISIBLE

                    val youTubeThumbnail = SupportData.getYoutubeThumbnailUrl(videoId = item.youTubeVideoID, quality = SupportData.YOUTUBE_THUMB_480x360)
                    Glide.with(context)
                            .load(youTubeThumbnail)
                            .apply(imageGlideOption)
                            .into(youtubeThumbnailView)

                    youtubeThumbnailLayout.setOnClickListener {
//                        val intent = YouTubeStandalonePlayer.createVideoIntent(context as Activity, context.resources.getString(R.string.app_api_key), item.videoYouTubeID)
//                        context.startActivity(intent)
                    }
                }
            }

            // 좋아요, 저장한 포스트 유무
            imageViewLike.isSelected = item.isLike
            imageViewSave.isSelected = item.isSave

            // 좋아요, 댓글 수 표기
            val likeNumStr = SupportData.countFormat(count = item.likeNum)
            textViewLikeNum.setText(likeNumStr, TextView.BufferType.SPANNABLE)
            val commentNumStr = SupportData.countFormat(count = item.commentsNum)
            textViewCommentNum.setText(commentNumStr, TextView.BufferType.SPANNABLE)

            likeLayout.tag = position
            likeLayout.setOnClickListener(likePostListener)
            commentLayout.tag = item.postId
            commentLayout.setOnClickListener(commentsListener)
            savePostLayout.tag = position
            savePostLayout.setOnClickListener(savePostListener)
            sharePostLayout.tag = position
            sharePostLayout.setOnClickListener(sharePostListener)

            // post 내용
            if (item.content.isEmpty()) {
                textViewContent.visibility = View.GONE
            }
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: PostDetailListData, position: Int)
    }
}
