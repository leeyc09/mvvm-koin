package xlab.world.xlab.adapter.recyclerView

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
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
import xlab.world.xlab.utils.support.SPHelper

class SelectUsedGoodsAdapter(private val context: Context,
                             private val selectListener: View.OnClickListener) : RecyclerView.Adapter<SelectUsedGoodsAdapter.ViewHolder>() {

    private val selectUsedGoodsData: SelectUsedGoodsData = SelectUsedGoodsData()
    var dataLoading: Boolean
        get() = this.selectUsedGoodsData.isLoading
        set(value) { this.selectUsedGoodsData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.selectUsedGoodsData.total
    var dataNextPage: Int = 1
        get() = this.selectUsedGoodsData.nextPage

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun getItem(position: Int): SelectUsedGoodsListData {
        return selectUsedGoodsData.items[position]
    }

    fun getSelectedGoods(): HashMap<Int, SelectUsedGoodsListData>{
        val selectedGoods = HashMap<Int, SelectUsedGoodsListData>()
        selectUsedGoodsData.items.forEachIndexed { index, goods ->
            if (goods.isSelect)
                selectedGoods[index] = goods
        }

        return selectedGoods
    }

    fun updateData(selectUsedGoodsData: SelectUsedGoodsData) {
        this.selectUsedGoodsData.items.clear()
        this.selectUsedGoodsData.items.addAll(selectUsedGoodsData.items)

        this.selectUsedGoodsData.isLoading = false
        this.selectUsedGoodsData.total = selectUsedGoodsData.total
        this.selectUsedGoodsData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(selectUsedGoodsData: SelectUsedGoodsData) {
        val size: Int = itemCount
        this.selectUsedGoodsData.items.addAll(selectUsedGoodsData.items)

        this.selectUsedGoodsData.isLoading = false
        this.selectUsedGoodsData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_select_used_goods, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = selectUsedGoodsData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return selectUsedGoodsData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        private val checkBoxBtn: ImageView = view.findViewById(R.id.checkBoxBtn)
        private val imageViewGoods: ImageView = view.findViewById(R.id.imageViewGoods)
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        private val textViewBrand: TextView = view.findViewById(R.id.textViewBrand)

        override fun display(item: SelectUsedGoodsListData, position: Int) {
            checkBoxBtn.isSelected = item.isSelect

            Glide.with(context)
                    .load(item.imageURL)
                    .apply(glideOption)
                    .into(imageViewGoods)

            textViewTitle.setText(item.goodsName, TextView.BufferType.SPANNABLE)
            textViewBrand.setText(item.goodsBrand, TextView.BufferType.SPANNABLE)

            mainLayout.tag = position
            mainLayout.setOnClickListener(selectListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: SelectUsedGoodsListData, position: Int)
    }
}
