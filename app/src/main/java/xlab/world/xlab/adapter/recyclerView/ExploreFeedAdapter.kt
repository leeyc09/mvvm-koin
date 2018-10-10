package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SupportData

class ExploreFeedAdapter(private val context: Context,
                         private val postListener: View.OnClickListener,
                         private val goodsListener: View.OnClickListener) : RecyclerView.Adapter<ExploreFeedAdapter.ViewHolder>() {

    private val exploreFeedData: ExploreFeedData = ExploreFeedData()
    var dataLoading: Boolean
        get() = this.exploreFeedData.isLoading
        set(value) { this.exploreFeedData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.exploreFeedData.total
    var dataNextPage: Int = 1
        get() = this.exploreFeedData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun updateData(exploreFeedData: ExploreFeedData) {
        this.exploreFeedData.items.clear()
        this.exploreFeedData.items.addAll(exploreFeedData.items)

        this.exploreFeedData.isLoading = false
        this.exploreFeedData.total = exploreFeedData.total
        this.exploreFeedData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(exploreFeedData: ExploreFeedData) {
        val size: Int = itemCount
        this.exploreFeedData.items.addAll(exploreFeedData.items)

        this.exploreFeedData.isLoading = false
        this.exploreFeedData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override fun getItemViewType(position: Int): Int {
        return this.exploreFeedData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            AppConstants.FEED_POST -> PostsViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_posts_thumb, parent, false))

            AppConstants.FEED_GOODS -> GoodsViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_goods_thumb, parent, false))

            else -> GoodsViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_posts_thumb, parent, false))
        }
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = exploreFeedData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return exploreFeedData.items.size
    }

    // posts view holder
    inner class PostsViewHolderBind(view: View): ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageViewPostsThumb: ImageView = view.findViewById(R.id.imageViewPostsThumb)
        private val imageViewVideoTag: ImageView = view.findViewById(R.id.imageViewVideoTag)

        override fun display(item: ExploreFeedListData, position: Int) {
            // post type 이 video -> video tag 보여줌
            imageViewVideoTag.visibility =
                    if (item.postsType == AppConstants.POSTS_IMAGE) View.GONE
                    else View.VISIBLE

            // post 이미지
            val imageUrl = item.imageURL
//                    when (item.postsType) {
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

    // goods view holder
    inner class GoodsViewHolderBind(view: View): ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageViewGoodsThumb: ImageView = view.findViewById(R.id.imageViewGoodsThumb)
        private val imageViewGoodsTag: ImageView = view.findViewById(R.id.imageViewGoodsTag)
        private val percentLayout: LinearLayout = view.findViewById(R.id.percentLayout)

        override fun display(item: ExploreFeedListData, position: Int) {
            // goods tag 이미지
            imageViewGoodsTag.visibility = View.VISIBLE

            // goods 이미지
            Glide.with(context)
                    .load(item.imageURL)
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
        abstract fun display(item: ExploreFeedListData, position: Int)
    }
}
