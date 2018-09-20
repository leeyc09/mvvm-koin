package xlab.world.xlab.adapter.recyclerView

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.PetInfo

class PetHairColorAdapter(val context: Context,
                          private val selectListener: View.OnClickListener) : RecyclerView.Adapter<PetHairColorAdapter.ViewHolder>() {

    private val petInfo: PetInfo by (context as Activity).inject()
    private val petHairColorData: PetHairFeatureData = PetHairFeatureData()

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions().centerCrop()
            .circleCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    fun getItem(position: Int): PetHairFeatureListData {
        return petHairColorData.items[position]
    }
    fun updateData(petHairColorData: PetHairFeatureData) {
        this.petHairColorData.items.clear()
        this.petHairColorData.items.addAll(petHairColorData.items)

        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_pet_hair_color, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = petHairColorData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return petHairColorData.items.size
    }

    // view holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: CardView = view.findViewById(R.id.mainLayout)
        private val imageViewSelected: ImageView = view.findViewById(R.id.imageViewSelected)
        private val imageViewColor: ImageView = view.findViewById(R.id.imageViewColor)
        private val textViewEtc: TextView = view.findViewById(R.id.textViewEtc)

        override fun display(item: PetHairFeatureListData, position: Int) {
            imageViewSelected.visibility =
                    if (item.isSelect) View.VISIBLE
                    else View.GONE

            // 기타 색상 -> '기타' text 보이게
            petInfo.petHairColor[item.hairFeatureCode]?.let {
                textViewEtc.visibility =
                        if (it.name == context.getString(R.string.etc)) View.VISIBLE
                        else View.GONE

                Glide.with(context)
                        .load(ColorDrawable(Color.parseColor("#${it.colorCode}")))
                        .apply(glideOption)
                        .into(imageViewColor)
            }

            mainLayout.tag = position
            mainLayout.setOnClickListener(selectListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: PetHairFeatureListData, position: Int)
    }
}
