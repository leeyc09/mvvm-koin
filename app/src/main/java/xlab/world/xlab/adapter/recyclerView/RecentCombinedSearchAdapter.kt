package xlab.world.xlab.adapter.recyclerView

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.SPHelper

class RecentCombinedSearchAdapter(val context: Context,
                                  private val selectListener: View.OnClickListener,
                                  private val deleteListener: View.OnClickListener) : RecyclerView.Adapter<RecentCombinedSearchAdapter.ViewHolder>() {

    private val spHelper: SPHelper by (context as Activity).inject()
    private val recentCombinedSearchData: RecentCombinedSearchData = RecentCombinedSearchData()

    init {
        spHelper.recentSearch.reversed().forEach { searchText ->
            recentCombinedSearchData.items.add(RecentCombinedSearchListData(searchText = searchText))
        }
    }

    fun getItem(position: Int): RecentCombinedSearchListData {
        return recentCombinedSearchData.items[position]
    }

    fun removeData(position: Int) {
        recentCombinedSearchData.items.removeAt(position)
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recent_combined_search, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = recentCombinedSearchData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return recentCombinedSearchData.items.size
    }

    // view holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.textView)
        private val deleteBtn: ImageView = view.findViewById(R.id.deleteBtn)

        override fun display(item: RecentCombinedSearchListData, position: Int) {
            textView.setText(item.searchText, TextView.BufferType.SPANNABLE)
            textView.tag = item.searchText
            textView.setOnClickListener(selectListener)


            deleteBtn.tag = position
            deleteBtn.setOnClickListener(deleteListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: RecentCombinedSearchListData, position: Int)
    }
}
