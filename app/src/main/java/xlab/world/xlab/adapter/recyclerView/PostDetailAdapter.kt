package xlab.world.xlab.adapter.recyclerView

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.youtube.player.YouTubeStandalonePlayer
import xlab.world.xlab.R
import xlab.world.xlab.adapter.viewPager.ViewImagePagerAdapter
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.font.CustomFont
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.hashTag.HashTagHelper
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import java.util.ArrayList

class PostDetailAdapter(private val context: Context,
                        private val changeViewTypeListener: View.OnClickListener?,
                        private val profileListener: View.OnClickListener?,
                        private val followListener: View.OnClickListener?,
                        private val moreListener: View.OnClickListener?,
                        private val likePostListener: View.OnClickListener,
                        private val commentsListener: View.OnClickListener,
                        private val savePostListener: View.OnClickListener,
                        private val sharePostListener: View.OnClickListener,
                        private val hashTagListener: HashTagHelper.ClickListener,
                        private val goodsListener: View.OnClickListener) : RecyclerView.Adapter<PostDetailAdapter.ViewHolder>() {

    private var postDetailData: PostDetailData = PostDetailData()
    var dataLoading: Boolean
        get() = this.postDetailData.isLoading
        set(value) { this.postDetailData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.postDetailData.total
    var dataNextPage: Int = 1
        get() = this.postDetailData.nextPage

    private val contentMaxLine = 7
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

    fun linkData(postDetailData: PostDetailData) {
        this.postDetailData = postDetailData
        notifyDataSetChanged()
    }

    fun getItem(position: Int): PostDetailListData {
        return postDetailData.items[position]
    }

    override fun getItemViewType(position: Int): Int {
        return this.postDetailData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            AppConstants.ADAPTER_HEADER -> HeaderViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_post_header, parent, false))

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

    // header view holder
    inner class HeaderViewHolderBind(view: View): ViewHolder(view) {
        private val gridThreeBtn: ImageView = view.findViewById(R.id.gridThreeBtn)
        private val gridOneBtn: ImageView = view.findViewById(R.id.gridOneBtn)
        override fun display(item: PostDetailListData, position: Int) {
            // post 썸네일로 보기 버튼만 활성화
            gridThreeBtn.isEnabled = true
            gridOneBtn.isEnabled = false

            changeViewTypeListener?.let { gridThreeBtn.setOnClickListener(changeViewTypeListener)}
        }
    }

    // content view holder
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
                            val tabOffSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context.resources.displayMetrics).toInt()
                            params.setMargins(tabOffSetDIP, 0, tabOffSetDIP, 0)
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
                            .load(item.imageURL.first())
                            .apply(imageGlideOption)
                            .into(youtubeThumbnailView)

                    youtubeThumbnailLayout.setOnClickListener {
                        RunActivity.youtubePlayerActivity(context = context as Activity, youTubeVideoId = item.youTubeVideoID)
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
            if (item.content.isEmpty()) { // 내용 없는 경우
                textViewContent.visibility = View.GONE
            } else {
                textViewContent.visibility = View.VISIBLE

                textViewContent.setText(item.content, TextView.BufferType.SPANNABLE)
                textViewContent.post {
                    val lineCnt = textViewContent.lineCount
                    if (lineCnt > contentMaxLine) { // 내용 줄 길이가 n(7) 줄 이상일 경우
                        if (!item.showAllContent) {
                            item.hideContent = true // 더보기 처리 체크
                            textViewContent.maxLines = contentMaxLine

                            val start = textViewContent.layout.getLineEnd(contentMaxLine - 2)
                            val end = textViewContent.layout.getLineEnd(contentMaxLine - 1)
                            val maxLineText = textViewContent.text.toString().substring(start, end)
                            val minusCnt =
                                    if (maxLineText.length > 6) 7
                                    else 3

                            val endLineCharCnt = textViewContent.layout.getLineEnd(contentMaxLine - 1)
                            item.content = textViewContent.text.subSequence(0, endLineCharCnt - minusCnt).toString() + "... 더보기"

                            // 더보기 touch 이벤트
                            setMoreContent(item)
                        } else { // 내용 모두 보기 상태일 경우
                            textViewContent.maxLines = Integer.MAX_VALUE
                            item.content = item.contentOrigin
                            textViewContent.setText(item.content, TextView.BufferType.SPANNABLE)
                        }
                    } else { // 내용 줄 길이가 n(7) 줄 이상이 아닌 경우
                        if (!item.showAllContent && item.hideContent) { // 내용 모두 보기 상태 아니고 더보기 처리 된 경우
                            // 더보기 touch 이벤트
                            setMoreContent(item)
                        } else {
                            textViewContent.maxLines = Integer.MAX_VALUE
                            item.content = item.contentOrigin
                            textViewContent.setText(item.content, TextView.BufferType.SPANNABLE)
                        }
                    }
                }
                val hashTagHelper = HashTagHelper(
                        hashTagCharsColor = hashTagCharsColor,
                        hashTagCharsFont = hashTagCharsFont,
                        onHashTagWritingListener = null,
                        onHashTagClickListener = hashTagListener,
                        additionalHashTagChar = additionalHashTagChar)
                hashTagHelper.handle(textViewContent)
            }

            // goods
            if (item.goodsList.isEmpty()) {
                goodsRecyclerView.visibility = View.GONE
            } else {
                goodsRecyclerView.visibility = View.VISIBLE
                val goodsData = PostDetailGoodsData()
                item.goodsList.forEach { goods ->
                    goodsData.items.add(PostDetailGoodsListData(
                            goodsCode = goods.code,
                            imageURL = goods.image
                    ))
                }
                val postDetailGoodsAdapter = PostDetailGoodsAdapter(context = context, goodsListener = goodsListener)
                postDetailGoodsAdapter.updateData(postDetailGoodsData = goodsData)
                goodsRecyclerView.adapter = postDetailGoodsAdapter
                goodsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                if (goodsRecyclerView.itemDecorationCount < 1)
                    goodsRecyclerView.addItemDecoration(CustomItemDecoration(context = context, right = 0.5f))
            }
        }

        private fun setMoreContent(item: PostDetailListData) {
            val contentSpannableStr = SpannableString(item.content)
            val moreListener = object: ClickableSpan() {
                override fun onClick(widget: View?) {
                    item.showAllContent = !item.showAllContent
                    notifyItemChanged(position)
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ResourcesCompat.getColor(context.resources, R.color.colorA4A4A4, null)
                    ds.isUnderlineText = false
                }
            }
            contentSpannableStr.setSpan(
                    moreListener,
                    contentSpannableStr.length - 3,
                    contentSpannableStr.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            textViewContent.movementMethod = LinkMovementMethod.getInstance()
            textViewContent.setText(contentSpannableStr, TextView.BufferType.SPANNABLE)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: PostDetailListData, position: Int)
    }
}
