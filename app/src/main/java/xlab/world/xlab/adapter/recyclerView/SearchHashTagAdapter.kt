package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*

class SearchHashTagAdapter(private val context: Context,
                           private val selectListener: View.OnClickListener) : RecyclerView.Adapter<SearchHashTagAdapter.ViewHolder>() {

    private val searchHashTagData: SearchHashTagData = SearchHashTagData()
    var dataLoading: Boolean
        get() = this.searchHashTagData.isLoading
        set(value) { this.searchHashTagData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.searchHashTagData.total
    var dataNextPage: Int = 1
        get() = this.searchHashTagData.nextPage

    fun updateData(searchHashTagData: SearchHashTagData) {
        this.searchHashTagData.items.clear()
        this.searchHashTagData.items.addAll(searchHashTagData.items)

        this.searchHashTagData.isLoading = false
        this.searchHashTagData.total = searchHashTagData.total
        this.searchHashTagData.searchText = searchHashTagData.searchText
        this.searchHashTagData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(searchHashTagData: SearchHashTagData) {
        val size: Int = itemCount
        this.searchHashTagData.items.addAll(searchHashTagData.items)

        this.searchHashTagData.isLoading = false
        this.searchHashTagData.searchText = searchHashTagData.searchText
        this.searchHashTagData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_hash_tag, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = searchHashTagData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return searchHashTagData.items.size
    }

    // view holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        private val textViewHashTag: TextView = view.findViewById(R.id.textViewHashTag)
        private val textViewHashTagCnt: TextView = view.findViewById(R.id.textViewHashTagCnt)

        override fun display(item: SearchHashTagListData, position: Int) {
            val hashTagStr = SpannableString(String.format("#%s", item.hashTag))
            val charStyle = ForegroundColorSpan(ResourcesCompat.getColor(context.resources, R.color.color5300ED, null))
            hashTagStr.setSpan(charStyle, 0, searchHashTagData.searchText.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textViewHashTag.setText(hashTagStr, TextView.BufferType.SPANNABLE)

            textViewHashTagCnt.setText(item.hashTagCnt.toString(), TextView.BufferType.SPANNABLE)

            mainLayout.tag = "${item.hashTag} "
            mainLayout.setOnClickListener(selectListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: SearchHashTagListData, position: Int)
    }
}
