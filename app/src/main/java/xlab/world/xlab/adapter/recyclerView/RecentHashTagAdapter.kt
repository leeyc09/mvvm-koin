package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*

class RecentHashTagAdapter(private val context: Context,
                           private val selectListener: View.OnClickListener) : RecyclerView.Adapter<RecentHashTagAdapter.ViewHolder>() {

    private val recentHashTagData: RecentHashTagData = RecentHashTagData()

    fun updateData(recentHashTagData: RecentHashTagData) {
        this.recentHashTagData.items.clear()
        this.recentHashTagData.items.addAll(recentHashTagData.items)

        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_hash_tag, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = recentHashTagData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return recentHashTagData.items.size
    }

    // view holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        private val textViewHashTag: TextView = view.findViewById(R.id.textViewHashTag)
        private val textViewHashTagCnt: TextView = view.findViewById(R.id.textViewHashTagCnt)

        override fun display(item: RecentHashTagListData, position: Int) {
            textViewHashTagCnt.visibility = View.GONE

            textViewHashTag.setText(String.format("#%s", item.hashTag), TextView.BufferType.SPANNABLE)

            mainLayout.tag = "${item.hashTag} "
            mainLayout.setOnClickListener(selectListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: RecentHashTagListData, position: Int)
    }
}
