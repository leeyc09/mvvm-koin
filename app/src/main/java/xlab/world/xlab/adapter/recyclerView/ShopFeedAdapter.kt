package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.AllFeedListData
import xlab.world.xlab.data.adapter.ShopFeedData
import xlab.world.xlab.data.adapter.ShopFeedListData
import xlab.world.xlab.data.request.ReqSearchGoodsData
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration

class ShopFeedAdapter(private val context: Context,
                      private val searchEditorActionListener: TextView.OnEditorActionListener,
                      private val categoryListener: View.OnClickListener,
                      private val goodsListener: View.OnClickListener,
                      private val questionListener: View.OnClickListener,
                      private var matchVisible: Int) : RecyclerView.Adapter<ShopFeedAdapter.ViewHolder>() {

    private val shopFeedData: ShopFeedData = ShopFeedData()

    // goods 인기도 % visible 변경
    fun changeMatchVisible(visibility: Int) {
        matchVisible = visibility
        notifyDataSetChanged()
    }

    fun updateData(shopFeedData: ShopFeedData) {
        this.shopFeedData.items.clear()
        this.shopFeedData.items.addAll(shopFeedData.items)

        this.shopFeedData.isLoading = false
        this.shopFeedData.total = shopFeedData.total
        this.shopFeedData.nextPage = 2

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return this.shopFeedData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            AppConstants.ADAPTER_HEADER -> HeaderViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_shop_feed_header, parent, false))

            AppConstants.ADAPTER_CONTENT -> ContentViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_shop_feed_content, parent, false))

            else -> ContentViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_shop_feed_content, parent, false))
        }
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = shopFeedData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return shopFeedData.items.size
    }

    // header view holder
    inner class HeaderViewHolderBind(view: View): ViewHolder(view) {
        private val editTextSearch: EditText = view.findViewById(R.id.editTextSearch)
        private val searchDeleteBtn: ImageView = view.findViewById(R.id.searchDeleteBtn)

        override fun display(item: ShopFeedListData, position: Int) {
            // x 버튼 활성 & 비활성
            ViewFunction.onTextChange(editText = editTextSearch) { searchText ->
                searchDeleteBtn.visibility =
                        if (searchText.isEmpty()) View.INVISIBLE
                        else View.VISIBLE
            }
            editTextSearch.setOnEditorActionListener(searchEditorActionListener)
            searchDeleteBtn.setOnClickListener { _ ->
                editTextSearch.setText("")
            }
        }
    }

    // content view holder
    inner class ContentViewHolderBind(view: View): ViewHolder(view) {
        private val categoryBtn: LinearLayout = view.findViewById(R.id.categoryBtn)
        private val textViewCategory: TextView = view.findViewById(R.id.textViewCategory)
        private val goodsRecyclerView: RecyclerView = view.findViewById(R.id.goodsRecyclerView)

        override fun display(item: ShopFeedListData, position: Int) {
            // 카테고리 이름
            textViewCategory.setText(item.categoryText, TextView.BufferType.SPANNABLE)
            // goods list
            val goodsAdapter = SearchGoodsAdapter(context = context,
                    sortListener = null,
                    goodsListener = goodsListener,
                    questionListener = questionListener,
                    contentBottomMargin = 36f,
                    matchVisible = matchVisible)
            goodsRecyclerView.adapter = goodsAdapter
            goodsAdapter.linkData(searchGoodsData = item.goodsData)

            if (goodsRecyclerView.layoutManager == null)
                goodsRecyclerView.layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)

            if (goodsRecyclerView.itemDecorationCount < 1)
                goodsRecyclerView.addItemDecoration(CustomItemDecoration(context = context, left = 0.5f, right = 0.5f))
            goodsRecyclerView.isNestedScrollingEnabled = false

            // set category listener
            categoryBtn.tag = ReqSearchGoodsData(text = item.categoryText,
                    code = item.categoryCode)
            categoryBtn.setOnClickListener(categoryListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: ShopFeedListData, position: Int)
    }
}
