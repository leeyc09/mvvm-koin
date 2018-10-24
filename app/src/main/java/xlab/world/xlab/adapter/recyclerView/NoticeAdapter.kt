package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.NoticeData
import xlab.world.xlab.data.adapter.NoticeListData
import xlab.world.xlab.data.adapter.TopicSettingListData

class NoticeAdapter(private val context: Context,
                    private val selectListener: View.OnClickListener) : RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {

    private var noticeData: NoticeData = NoticeData()
    var dataLoading: Boolean
        get() = this.noticeData.isLoading
        set(value) { this.noticeData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.noticeData.total
    var dataNextPage: Int = 1
        get() = this.noticeData.nextPage

    fun linkData(noticeData: NoticeData) {
        this.noticeData = noticeData
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_notice, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = noticeData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return noticeData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val noticeHeaderLayout: LinearLayout = view.findViewById(R.id.noticeHeaderLayout)
        private val imageViewNoticeArrow: ImageView = view.findViewById(R.id.imageViewNoticeArrow)
        private val textViewNoticeTitle: TextView = view.findViewById(R.id.textViewNoticeTitle)
        private val textViewNoticeDate: TextView = view.findViewById(R.id.textViewNoticeDate)
        private val divideView: View = view.findViewById(R.id.divideView)
        private val noticeContentLayout: LinearLayout = view.findViewById(R.id.noticeContentLayout)
        private val textViewNoticeContent: TextView = view.findViewById(R.id.textViewNoticeContent)

        override fun display(item: NoticeListData, position: Int) {
            // 공지 제목, 날짜, 내용
            textViewNoticeTitle.setText(noticeData.items[position].title, TextView.BufferType.SPANNABLE)
            textViewNoticeContent.setText(noticeData.items[position].content, TextView.BufferType.SPANNABLE)
            textViewNoticeDate.setText(noticeData.items[position].date, TextView.BufferType.SPANNABLE)

            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            (noticeHeaderLayout.layoutParams as LinearLayout.LayoutParams).setMargins(0, offSetDIP, 0, 0)

            // 읽은 공지 화살표 색상 변경
           imageViewNoticeArrow.isSelected = item.isRead

            // 선택한 공지 내용 보이게
            if (item.isSelect) {
                noticeContentLayout.visibility = View.VISIBLE
                divideView.visibility = View.INVISIBLE
                imageViewNoticeArrow.rotation = 180.toFloat()

                textViewNoticeContent.visibility = View.VISIBLE
            } else {
                noticeContentLayout.visibility = View.GONE
                divideView.visibility = View.VISIBLE
                imageViewNoticeArrow.rotation = 0.toFloat()

                textViewNoticeContent.visibility = View.GONE
            }

            noticeHeaderLayout.tag = position
            noticeHeaderLayout.setOnClickListener(selectListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: NoticeListData, position: Int)
    }
}
