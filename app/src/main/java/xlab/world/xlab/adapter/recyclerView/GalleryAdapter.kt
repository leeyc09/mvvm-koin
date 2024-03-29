package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.AppConstants

class GalleryAdapter(private val context: Context,
                     private val selectListener: View.OnClickListener,
                     private val directSelectListener: View.OnClickListener?) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private var galleryData: GalleryData = GalleryData()
    var dataLoading: Boolean
        get() = this.galleryData.isLoading
        set(value) { this.galleryData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.galleryData.total
    var dataNextPage: Int = 1
        get() = this.galleryData.nextPage

    fun linkData(galleryData: GalleryData) {
        this.galleryData = galleryData
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return this.galleryData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            AppConstants.GALLERY_ONE -> OneSelectViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_gallery_one, parent, false))

            AppConstants.GALLERY_MANY -> ManySelectViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_gallery_many, parent, false))

            else -> OneSelectViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_posts_thumb, parent, false))
        }
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = galleryData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return galleryData.items.size
    }

    // one select image view holder
    inner class OneSelectViewHolderBind(view: View): ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageView: ImageView = view.findViewById(R.id.imageView)
        private val overlayFilter: ImageView = view.findViewById(R.id.overlayFilter)
        private val selectedFilter: ImageView = view.findViewById(R.id.selectedFilter)

        override fun display(item: GalleryListData, position: Int) {
            // gallery image
            Glide.with(context)
                    .load(item.data)
                    .into(imageView)

            overlayFilter.visibility =
                    if (item.isSelect) View.VISIBLE
                    else View.GONE

            selectedFilter.visibility =
                    if (item.isSelect) View.VISIBLE
                    else View.GONE

            mainLayout.tag = position
            mainLayout.setOnClickListener(selectListener)
        }
    }

    // many select image view holder
    inner class ManySelectViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageView: ImageView = view.findViewById(R.id.imageView)
        private val overlayFilter: ImageView = view.findViewById(R.id.overlayFilter)
        private val selectedFilter: ImageView = view.findViewById(R.id.selectedFilter)
        private val imageViewSelect: ImageView = view.findViewById(R.id.imageViewSelect)
        private val textViewNum: TextView = view.findViewById(R.id.textViewNum)

        override fun display(item: GalleryListData, position: Int) {
            // gallery image
            Glide.with(context)
                    .load(item.data)
                    .into(imageView)

            overlayFilter.visibility =
                    if (item.isPreview) View.VISIBLE
                    else View.GONE

            selectedFilter.visibility =
                    if (item.isSelect) View.VISIBLE
                    else View.GONE

            imageViewSelect.isSelected = item.isSelect
            item.selectNum?.let {
                textViewNum.visibility = View.VISIBLE
                textViewNum.setText(it.toString(), TextView.BufferType.SPANNABLE)
            } ?: let {
                textViewNum.visibility = View.GONE
            }

            mainLayout.tag = position
            mainLayout.setOnClickListener(selectListener)

            imageViewSelect.tag = position
            imageViewSelect.setOnClickListener(directSelectListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: GalleryListData, position: Int)
    }
}
