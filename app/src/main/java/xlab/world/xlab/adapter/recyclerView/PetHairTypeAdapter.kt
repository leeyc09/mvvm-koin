package xlab.world.xlab.adapter.recyclerView

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.request.RequestOptions
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.PetInfo

class PetHairTypeAdapter(val context: Context,
                         private val selectListener: View.OnClickListener) : RecyclerView.Adapter<PetHairTypeAdapter.ViewHolder>() {

    private val petInfo: PetInfo by (context as Activity).inject()
    private val petHairTypeData: PetHairTypeData = PetHairTypeData()

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions().centerCrop()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun updateData(petHairTypeData: PetHairTypeData) {
        this.petHairTypeData.items.clear()
        this.petHairTypeData.items.addAll(petHairTypeData.items)

        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_pet_hair_type, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = petHairTypeData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return petHairTypeData.items.size
    }

    // view holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val textViewType: TextView = view.findViewById(R.id.textViewType)

        override fun display(item: PetHairTypeListData, position: Int) {
            textViewType.isSelected = item.isSelect
            textViewType.setText(petInfo.petHairType[item.hairTypeCode], TextView.BufferType.SPANNABLE)

            textViewType.tag = position
            textViewType.setOnClickListener(selectListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: PetHairTypeListData, position: Int)
    }
}
