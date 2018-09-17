package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.PostDetailGoodsData
import xlab.world.xlab.data.adapter.PostDetailGoodsListData
import xlab.world.xlab.data.adapter.TopicSettingListData

class PostDetailGoodsAdapter(private val context: Context,
                             private val goodsListener: View.OnClickListener) : RecyclerView.Adapter<PostDetailGoodsAdapter.ViewHolder>() {

    private val postDetailGoodsData: PostDetailGoodsData = PostDetailGoodsData()

    private val glideOption = RequestOptions()
            .circleCrop()

    fun updateData(postDetailGoodsData: PostDetailGoodsData) {
        this.postDetailGoodsData.items.clear()
        this.postDetailGoodsData.items.addAll(postDetailGoodsData.items)

        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_post_detail_goods, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = postDetailGoodsData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return postDetailGoodsData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageView: ImageView = view.findViewById(R.id.imageView)

        override fun display(item: PostDetailGoodsListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(offSetDIP, 0, 0, 0)
            // topic 이미지
            Glide.with(context)
                    .load(item.imageURL)
                    .apply(glideOption)
                    .into(imageView)

            mainLayout.tag = item.goodsCode
            mainLayout.setOnClickListener(goodsListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: PostDetailGoodsListData, position: Int)
    }
}
