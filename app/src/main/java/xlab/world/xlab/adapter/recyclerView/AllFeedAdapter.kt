package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.Color
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
import com.google.android.youtube.player.YouTubeThumbnailView
import kotlinx.android.synthetic.main.item_goods_thumb.view.*
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.AllFeedData
import xlab.world.xlab.data.adapter.AllFeedListData
import xlab.world.xlab.data.adapter.TopicSettingListData
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SupportData

class AllFeedAdapter(private val context: Context,
                     private val postListener: View.OnClickListener,
                     private val goodsListener: View.OnClickListener,
                     private val questionListener: View.OnClickListener,
                     private var matchVisible: Int = View.VISIBLE) : RecyclerView.Adapter<AllFeedAdapter.ViewHolder>() {

    private val allFeedData: AllFeedData = AllFeedData()
    var dataLoading: Boolean
        get() = this.allFeedData.isLoading
        set(value) { this.allFeedData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.allFeedData.total
    var dataNextPage: Int = 1
        get() = this.allFeedData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    // goods feed 인기도 % visible 변경
    fun changeMatchVisible(visibility: Int) {
        matchVisible = visibility
        for ((index, item) in allFeedData.items.withIndex()) {
            if (item.dataType == AppConstants.FEED_GOODS)
                notifyItemChanged(index)
        }
    }

    fun getItem(position: Int): AllFeedListData {
        return allFeedData.items[position]
    }

    fun updateData(allFeedData: AllFeedData) {
        this.allFeedData.items.clear()
        this.allFeedData.items.addAll(allFeedData.items)

        this.allFeedData.isLoading = false
        this.allFeedData.total = allFeedData.total
        this.allFeedData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(allFeedData: AllFeedData) {
        val size: Int = itemCount
        this.allFeedData.items.addAll(allFeedData.items)

        this.allFeedData.isLoading = false
        this.allFeedData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override fun getItemViewType(position: Int): Int {
        return this.allFeedData.items[position].dataType
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
        holder.display(item = allFeedData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return allFeedData.items.size
    }

    // posts view holder
    inner class PostsViewHolderBind(view: View): ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageViewPostsThumb: ImageView = view.findViewById(R.id.imageViewPostsThumb)
        private val imageViewVideoTag: ImageView = view.findViewById(R.id.imageViewVideoTag)

        override fun display(item: AllFeedListData, position: Int) {
            // post type 이 video -> video tag 보여줌
            imageViewVideoTag.visibility =
                    if (item.postsType == AppConstants.POSTS_IMAGE) View.GONE
                    else View.VISIBLE

            // post 이미지
            val imageUrl = when (item.postsType) {
                // 이미지, 비디오 포스트 포스트
                AppConstants.POSTS_IMAGE,
                AppConstants.POSTS_VIDEO -> item.imageURL
                // 유튜브 링크 포스트
                AppConstants.POSTS_YOUTUBE_LINK -> SupportData.getYoutubeThumbnailUrl(videoId = item.youTubeVideoID, quality = SupportData.YOUTUBE_THUMB_120x90)
                else -> ""
            }
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
        private val matchBarLayout: LinearLayout = view.findViewById(R.id.matchBarLayout)
        private val textViewMatchValue: TextView = view.findViewById(R.id.textViewMatchValue)
        private val percentEmptyLayout: View = view.findViewById(R.id.percentEmptyLayout)

        override fun display(item: AllFeedListData, position: Int) {
            // goods tag 이미지
            imageViewGoodsTag.visibility = View.VISIBLE

            // goods 이미지
            Glide.with(context)
                    .load(item.imageURL)
                    .apply(glideOption)
                    .into(imageViewGoodsThumb)

            // 인기율 안보기 -> % 가림
            if (matchVisible != View.VISIBLE) {
                percentLayout.visibility = View.GONE
            } else {
                // guest or topic 없는 유저 -> question mark 보이기
                if (item.showQuestionMark) {
                    percentLayout.visibility = View.VISIBLE
                    setPercentBar(percentValue = "? ", percentColor = item.matchColor, percentWeight = 90f)

                    // ? 터치 이벤트
                    matchBarLayout.setOnClickListener(questionListener)
                } else {
                    // 인기도 50 이하 -> % bar 안보이게
                    if (item.matchingPercent < 50) {
                        percentLayout.visibility = View.GONE
                    } else {
                        percentLayout.visibility = View.VISIBLE
                        setPercentBar(percentValue = item.matchingPercent.toString(), percentColor = item.matchColor, percentWeight = item.matchingPercent.toFloat())
                    }
                }
            }
            // goods 터치 이벤트
            mainLayout.tag = item.goodsCd
            mainLayout.setOnClickListener(goodsListener)
        }

        private fun setPercentBar(percentValue: String, percentColor: Int, percentWeight: Float) {
            // 인기도 & topic color 설정
            textViewMatchValue.setText(percentValue, TextView.BufferType.SPANNABLE)
            matchBarLayout.setBackgroundColor(percentColor)

            // percent bar 길이 설정
            val percentParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, percentWeight)
            percentParams.gravity = Gravity.BOTTOM
            val percentEmptyParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, (100f - percentWeight))
            percentEmptyParams.gravity = Gravity.BOTTOM
            matchBarLayout.layoutParams = percentParams
            percentEmptyLayout.layoutParams = percentEmptyParams
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: AllFeedListData, position: Int)
    }
}
