package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.CommentData
import xlab.world.xlab.data.adapter.CommentListData
import xlab.world.xlab.data.adapter.TopicSettingListData
import xlab.world.xlab.utils.support.SupportData

class ListSelectDialogAdapter(private val context: Context,
                              private val selectListener: View.OnClickListener) : RecyclerView.Adapter<ListSelectDialogAdapter.ViewHolder>() {

    private var listData = ArrayList<String>()

    fun linkData(listData: ArrayList<String>) {
        this.listData = listData
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_list_select_dialog, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = listData[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return listData.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        private val textView: TextView = view.findViewById(R.id.textView)

        override fun display(item: String, position: Int) {
            textView.setText(item, TextView.BufferType.SPANNABLE)

            mainLayout.tag = item
            mainLayout.setOnClickListener(selectListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: String, position: Int)
    }
}
