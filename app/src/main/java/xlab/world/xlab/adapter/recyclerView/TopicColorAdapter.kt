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

class TopicColorAdapter(private val context: Context,
                        private val selectIndex: Int,
                        private val selectListener: View.OnClickListener) : RecyclerView.Adapter<TopicColorAdapter.ViewHolder>() {

    private val topicColorData: TopicColorData = TopicColorData()
    private val glideOption = RequestOptions().circleCrop()

    init {
        val topicColorStrArray = context.resources.getStringArray(R.array.topicColorStringList)
        topicColorStrArray.forEach { color ->
            topicColorData.items.add(TopicColorListData(
                    colorStr = color,
                    isSelect = false))
        }
        topicColorData.items[selectIndex].isSelect = true
    }

    fun getItem(position: Int): TopicColorListData {
        return topicColorData.items[position]
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_topic_color, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = topicColorData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return topicColorData.items.size
    }

    // view holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageViewSelected: ImageView = view.findViewById(R.id.imageViewSelected)
        private val imageViewColor: ImageView = view.findViewById(R.id.imageViewColor)

        override fun display(item: TopicColorListData, position: Int) {
            val offSetDIP =
                    if (position == 0 || position == 1) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(offSetDIP, 0, 0, 0)

            // 선택 레이아웃
            imageViewSelected.visibility =
                    if (item.isSelect) View.VISIBLE
                    else View.GONE

            Glide.with(context)
                    .load(ColorDrawable(Color.parseColor("#${item.colorStr}")))
                    .apply(glideOption)
                    .into(imageViewColor)

            mainLayout.tag = position
            mainLayout.setOnClickListener(selectListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: TopicColorListData, position: Int)
    }
}
