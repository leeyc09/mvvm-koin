package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.CardView
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
import xlab.world.xlab.R
import xlab.world.xlab.adapter.viewPager.ViewImagePagerAdapter
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.font.CustomFont
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.hashTag.HashTagHelper
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import java.util.ArrayList

class PostThumbnailAdapter(private val context: Context,
                           private val changeViewTypeListener: View.OnClickListener?,
                           private val postListener: View.OnClickListener) : RecyclerView.Adapter<PostThumbnailAdapter.ViewHolder>() {

    private val postThumbnailData: PostThumbnailData = PostThumbnailData()
    var dataLoading: Boolean
        get() = this.postThumbnailData.isLoading
        set(value) { this.postThumbnailData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.postThumbnailData.total
    var dataNextPage: Int = 1
        get() = this.postThumbnailData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions().centerCrop()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun updateData(postThumbnailData: PostThumbnailData) {
        this.postThumbnailData.items.clear()
        this.postThumbnailData.items.addAll(postThumbnailData.items)

        this.postThumbnailData.isLoading = false
        this.postThumbnailData.total = postThumbnailData.total
        this.postThumbnailData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(postThumbnailData: PostThumbnailData) {
        val size: Int = itemCount
        this.postThumbnailData.items.addAll(postThumbnailData.items)

        this.postThumbnailData.isLoading = false
        this.postThumbnailData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override fun getItemViewType(position: Int): Int {
        return this.postThumbnailData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            AppConstants.ADAPTER_HEADER -> HeaderViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_post_header, parent, false))

            AppConstants.ADAPTER_CONTENT -> ContentViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_posts_thumb, parent, false))

            else -> ContentViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_posts_thumb, parent, false))
        }
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = postThumbnailData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return postThumbnailData.items.size
    }

    // header view holder
    inner class HeaderViewHolderBind(view: View): ViewHolder(view) {
        private val gridThreeBtn: ImageView = view.findViewById(R.id.gridThreeBtn)
        private val gridOneBtn: ImageView = view.findViewById(R.id.gridOneBtn)
        override fun display(item: PostThumbnailListData, position: Int) {
            // post 디테일로 보기 버튼만 활성화
            gridThreeBtn.isEnabled = false
            gridOneBtn.isEnabled = true

            changeViewTypeListener?.let { gridOneBtn.setOnClickListener(changeViewTypeListener)}
        }
    }

    // content view holder
    inner class ContentViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageViewPostsThumb: ImageView = view.findViewById(R.id.imageViewPostsThumb)
        private val imageViewVideoTag: ImageView = view.findViewById(R.id.imageViewVideoTag)

        override fun display(item: PostThumbnailListData, position: Int) {
            // post type 이 video -> video tag 보여줌
            imageViewVideoTag.visibility =
                    if (item.postType == AppConstants.POSTS_IMAGE) View.GONE
                    else View.VISIBLE

            // post 이미지
            val imageUrl = item.imageURL
//                    when (item.postType) {
//                // 이미지, 비디오 포스트 포스트
//                AppConstants.POSTS_IMAGE,
//                AppConstants.POSTS_VIDEO -> item.imageURL
//                // 유튜브 링크 포스트
//                AppConstants.POSTS_YOUTUBE_LINK -> SupportData.getYoutubeThumbnailUrl(videoId = item.youTubeVideoID, quality = SupportData.YOUTUBE_THUMB_120x90)
//                else -> ""
//            }
            Glide.with(context)
                    .load(imageUrl)
                    .apply(glideOption)
                    .into(imageViewPostsThumb)

            // post 터치 이멘트
            mainLayout.tag = item.postId
            mainLayout.setOnClickListener(postListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: PostThumbnailListData, position: Int)
    }
}
