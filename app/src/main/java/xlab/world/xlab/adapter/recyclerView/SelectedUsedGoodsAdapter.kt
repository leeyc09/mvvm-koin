package xlab.world.xlab.adapter.recyclerView

import android.content.Context
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
import xlab.world.xlab.data.adapter.SelectUsedGoodsData
import xlab.world.xlab.data.adapter.SelectUsedGoodsListData
import xlab.world.xlab.utils.support.AppConstants

class SelectedUsedGoodsAdapter(private val context: Context,
                               private val deleteListener: View.OnClickListener) : RecyclerView.Adapter<SelectedUsedGoodsAdapter.ViewHolder>() {

    private val selectUsedGoodsData: SelectUsedGoodsData = SelectUsedGoodsData()

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun getItem(position: Int): SelectUsedGoodsListData {
        return selectUsedGoodsData.items[position]
    }

    fun updateData(selectUsedGoodsData: ArrayList<SelectUsedGoodsListData>) {
        this.selectUsedGoodsData.items.clear()
        selectUsedGoodsData.forEach { data ->
            this.selectUsedGoodsData.items.add(data)
        }

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return this.selectUsedGoodsData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            AppConstants.SELECTED_GOODS_ONLY_THUMB -> ThumbViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_selected_used_goods_thumb, parent, false))

            AppConstants.SELECTED_GOODS_WITH_INFO -> InfoViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_selected_used_goods_info, parent, false))

            else -> ThumbViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_selected_used_goods_thumb, parent, false))
        }
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = selectUsedGoodsData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return selectUsedGoodsData.items.size
    }

    // View Holder for only thumb
    inner class ThumbViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageViewGoods: ImageView = view.findViewById(R.id.imageViewGoods)
        private val deleteBnt: ImageView = view.findViewById(R.id.deleteBnt)

        override fun display(item: SelectUsedGoodsListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(offSetDIP, 0, 0, 0)

            Glide.with(context)
                    .load(item.imageURL)
                    .apply(glideOption)
                    .into(imageViewGoods)

            deleteBnt.tag = position
            deleteBnt.setOnClickListener(deleteListener)
        }
    }

    // View Holder for with info data
    inner class InfoViewHolderBind(view: View) : ViewHolder(view) {
        private val imageViewGoods: ImageView = view.findViewById(R.id.imageViewGoods)
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        private val textViewBrand: TextView = view.findViewById(R.id.textViewBrand)
        private val deleteBnt: ImageView = view.findViewById(R.id.deleteBtn)

        override fun display(item: SelectUsedGoodsListData, position: Int) {
            Glide.with(context)
                    .load(item.imageURL)
                    .apply(glideOption)
                    .into(imageViewGoods)

            textViewTitle.setText(item.goodsName, TextView.BufferType.SPANNABLE)
            textViewBrand.setText(item.goodsBrand, TextView.BufferType.SPANNABLE)

            deleteBnt.tag = position
            deleteBnt.setOnClickListener(deleteListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: SelectUsedGoodsListData, position: Int)
    }
}
