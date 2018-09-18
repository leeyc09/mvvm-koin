package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.SupportData

class GalleryAdapter(private val context: Context,
                     private val selectListener: View.OnClickListener) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private val galleryData: GalleryData = GalleryData()
    var dataLoading: Boolean
        get() = this.galleryData.isLoading
        set(value) { this.galleryData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.galleryData.total
    var dataNextPage: Int = 1
        get() = this.galleryData.nextPage

    fun getItem(position: Int): GalleryListData {
        return galleryData.items[position]
    }

    fun updateData(galleryData: GalleryData) {
        this.galleryData.items.clear()
        this.galleryData.items.addAll(galleryData.items)

        this.galleryData.isLoading = false
        this.galleryData.total = galleryData.total
        this.galleryData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(galleryData: GalleryData) {
        val size: Int = itemCount
        this.galleryData.items.addAll(galleryData.items)

        this.galleryData.isLoading = false
        this.galleryData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override fun getItemViewType(position: Int): Int {
        return this.galleryData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            AppConstants.GALLERY_ONE -> OneSelectViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_gallery_one, parent, false))

            AppConstants.ADAPTER_CONTENT -> ManySelectViewHolderBind(LayoutInflater.from(parent.context)
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
                    .load(galleryData.items[position].data)
                    .into(imageView)

            overlayFilter.visibility =
                    if (galleryData.items[position].selected) View.VISIBLE
                    else View.GONE

            selectedFilter.visibility =
                    if (galleryData.items[position].selected) View.VISIBLE
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
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: GalleryListData, position: Int)
    }
}
