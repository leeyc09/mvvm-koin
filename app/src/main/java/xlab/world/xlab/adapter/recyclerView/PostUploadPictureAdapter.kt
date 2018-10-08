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

class PostUploadPictureAdapter(private val context: Context) : RecyclerView.Adapter<PostUploadPictureAdapter.ViewHolder>() {

    private val postUploadPictureData: PostUploadPictureData = PostUploadPictureData()
    private val glideOption = RequestOptions().centerCrop()


    fun updateData(postUploadPictureData: PostUploadPictureData) {
        this.postUploadPictureData.items.clear()
        this.postUploadPictureData.items.addAll(postUploadPictureData.items)

        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_post_upload_picture, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = postUploadPictureData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return postUploadPictureData.items.size
    }

    // view holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.imageView)

        override fun display(item: PostUploadPictureListData, position: Int) {
            Glide.with(context)
                    .load(item.imagePath)
                    .apply(glideOption)
                    .into(imageView)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: PostUploadPictureListData, position: Int)
    }
}
