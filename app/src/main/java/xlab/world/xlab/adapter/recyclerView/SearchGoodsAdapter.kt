package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.SearchGoodsData
import xlab.world.xlab.data.adapter.SearchGoodsListData
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData

class SearchGoodsAdapter(private val context: Context,
                         private val sortListener: View.OnClickListener?,
                         private val goodsListener: View.OnClickListener,
                         private val questionListener: View.OnClickListener,
                         private val contentBottomMargin: Float,
                         private var matchVisible: Int) : RecyclerView.Adapter<SearchGoodsAdapter.ViewHolder>() {

    private var searchGoodsData: SearchGoodsData = SearchGoodsData()
    var dataLoading: Boolean
        get() = this.searchGoodsData.isLoading
        set(value) { this.searchGoodsData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.searchGoodsData.total
    var dataNextPage: Int = 1
        get() = this.searchGoodsData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    // 인기도 % visible 변경
    fun changeMatchVisible(visibility: Int) {
        matchVisible = visibility
        notifyDataSetChanged()
    }

    fun linkData(searchGoodsData: SearchGoodsData) {
        this.searchGoodsData = searchGoodsData
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return this.searchGoodsData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            AppConstants.ADAPTER_HEADER -> HeaderViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_goods_header, parent, false))

            AppConstants.ADAPTER_CONTENT -> ContentViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_goods_thumb_with_info, parent, false))

            else -> ContentViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_goods_thumb_with_info, parent, false))
        }
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = searchGoodsData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return searchGoodsData.items.size
    }

    // header view holder
    inner class HeaderViewHolderBind(view: View): ViewHolder(view) {
        private val textViewItemNum: TextView = view.findViewById(R.id.textViewItemNum)
        private val sortLayout: LinearLayout = view.findViewById(R.id.sortLayout)
        private val textViewSort: TextView = view.findViewById(R.id.textViewSort)

        override fun display(item: SearchGoodsListData, position: Int) {
            // 검색 상품 갯수
            textViewItemNum.setText(searchGoodsData.total.toString(), TextView.BufferType.SPANNABLE)
            // 검색 정렬 기준
            val sortStr = when (item.sortType) {
                AppConstants.SORT_MATCH -> context.getString(R.string.sort_match)
                AppConstants.SORT_DOWN_PRICE -> context.getString(R.string.sort_down_price)
                AppConstants.SORT_UP_PRICE -> context.getString(R.string.sort_up_price)
                else -> ""
            }
            textViewSort.setText(sortStr, TextView.BufferType.SPANNABLE)

            sortListener?.let {
                sortLayout.setOnClickListener(sortListener)
            }
        }
    }

    // content view holder
    inner class ContentViewHolderBind(view: View): ViewHolder(view) {
        private val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        private val goodsThumbLayout: View = view.findViewById(R.id.goodsThumbLayout)
        private val imageViewGoodsThumb: ImageView = goodsThumbLayout.findViewById(R.id.imageViewGoodsThumb)
        private val imageViewGoodsTag: ImageView = goodsThumbLayout.findViewById(R.id.imageViewGoodsTag)
        private val percentLayout: LinearLayout = goodsThumbLayout.findViewById(R.id.percentLayout)
        private val matchBarLayout: LinearLayout = goodsThumbLayout.findViewById(R.id.matchBarLayout)
        private val textViewMatchValue: TextView = goodsThumbLayout.findViewById(R.id.textViewMatchValue)
        private val textViewMatchUnit: TextView = view.findViewById(R.id.textViewMatchUnit)
        private val percentEmptyLayout: View = goodsThumbLayout.findViewById(R.id.percentEmptyLayout)

        private val textViewPrice: TextView = view.findViewById(R.id.textViewPrice)
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        private val textViewBrand: TextView = view.findViewById(R.id.textViewBrand)

        override fun display(item: SearchGoodsListData, position: Int) {
            val offSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, contentBottomMargin, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, 0, 0, offSetDIP)

            // goods tag 이미지
            imageViewGoodsTag.visibility = View.GONE

            // goods 이미지
            Glide.with(context)
                    .load(item.imageURL)
                    .apply(glideOption)
                    .into(imageViewGoodsThumb)

            // 인기율 안보기 -> % 가림
            if (matchVisible != View.VISIBLE) {
                // 안보이는 애니메이션은 한번만 동작하도록
                // 이후에는 애니메이션 없이 바로 뷰 안보이게
                if (item.withAnimation) {
                    setPercentBar(percentValue = if (item.showQuestionMark) "? " else item.matchingPercent.toString(), percentColor = item.matchColor,
                            percentWeight = if (item.showQuestionMark) 90f else item.matchingPercent.toFloat())

                    val percentBarAni = AnimationUtils.loadAnimation(context, R.anim.goods_match_bar_hide)
                    // 애니매이션 유지
                    percentBarAni.fillAfter = true
                    percentBarAni.isFillEnabled = true
                    percentBarAni.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(p0: Animation?) {
                        }

                        override fun onAnimationEnd(p0: Animation?) {
                            percentLayout.visibility = View.GONE
                            item.withAnimation = false
                        }

                        override fun onAnimationRepeat(p0: Animation?) {
                        }
                    })
                    val percentAni = AnimationUtils.loadAnimation(context, R.anim.goods_match_percent_hide)
                    // 애니매이션 유지
                    percentAni.fillAfter = true
                    percentAni.isFillEnabled = true

                    matchBarLayout.startAnimation(percentBarAni)
                    textViewMatchValue.startAnimation(percentAni)
                    textViewMatchUnit.startAnimation(percentAni)
                } else {
                    percentLayout.visibility = View.GONE
                }
            } else {
                // guest or topic 없는 유저 -> question mark 보이기
                if (item.showQuestionMark) {
                    setPercentBar(percentValue = "? ", percentColor = item.matchColor, percentWeight = 90f)
                    // 애니매이션 동작 꺼져있으면 on 으로
                    if (!item.withAnimation) item.withAnimation = true

                    val percentBarAni = AnimationUtils.loadAnimation(context, R.anim.goods_match_bar_show)
                    matchBarLayout.startAnimation(percentBarAni)
                    val percentAni = AnimationUtils.loadAnimation(context, R.anim.goods_match_percent_show)
                    textViewMatchValue.startAnimation(percentAni)
                    textViewMatchUnit.startAnimation(percentAni)

                    // ? 터치 이벤트
                    matchBarLayout.setOnClickListener(questionListener)
                } else {
                    // 인기도 50 이하 -> % bar 안보이게 (애니메이션 X)
                    if (item.matchingPercent < 50) {
                        percentLayout.visibility = View.GONE
                    } else {
                        setPercentBar(percentValue = item.matchingPercent.toString(), percentColor = item.matchColor, percentWeight = item.matchingPercent.toFloat())
                        // 애니매이션 동작 꺼져있으면 on 으로
                        if (!item.withAnimation) item.withAnimation = true

                        val percentBarAni = AnimationUtils.loadAnimation(context, R.anim.goods_match_bar_show)
                        matchBarLayout.startAnimation(percentBarAni)
                        val percentAni = AnimationUtils.loadAnimation(context, R.anim.goods_match_percent_show)
                        textViewMatchValue.startAnimation(percentAni)
                        textViewMatchUnit.startAnimation(percentAni)
                    }
                }
            }

            textViewPrice.setText(SupportData.applyPriceFormat(price = item.price), TextView.BufferType.SPANNABLE)
            textViewTitle.setText(item.title, TextView.BufferType.SPANNABLE)
            textViewBrand.setText(item.brand, TextView.BufferType.SPANNABLE)

            // goods 터치 이벤트
            mainLayout.tag = item.goodsCd
            mainLayout.setOnClickListener(goodsListener)
        }

        private fun setPercentBar(percentValue: String, percentColor: Int, percentWeight: Float) {
            percentLayout.visibility = View.VISIBLE

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
        abstract fun display(item: SearchGoodsListData, position: Int)
    }
}
