package xlab.world.xlab.adapter.recyclerView

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.PetInfo

class PetBreedAdapter(val context: Context,
                      petType: String,
                      private val selectListener: View.OnClickListener) : RecyclerView.Adapter<PetBreedAdapter.ViewHolder>() {

    private val petInfo: PetInfo by (context as Activity).inject()
    private val petBreedData: PetBreedData = PetBreedData()
    private val petBreedFinalData: PetBreedData = PetBreedData()

    init {
        petBreedData.items.addAll(
                if (petType == petInfo.dogCode) petInfo.dogBreedInfo
                else petInfo.catBreedInfo)

        petBreedFinalData.items.addAll(petBreedData.items)
    }

    fun searchData(search: String) {
        petBreedFinalData.items.clear()
        if (search.isEmpty()) {
            petBreedFinalData.items.addAll(petBreedData.items)
        } else {
            petBreedData.items.forEach { breed ->
                if (breed.nameKor.contains(search) || breed.nameEn.toUpperCase().contains(search))
                    petBreedFinalData.items.add(breed)
            }
        }
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_pet_breed, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = petBreedFinalData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return petBreedFinalData.items.size
    }

    // view holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        private val textViewBreed: TextView = view.findViewById(R.id.textViewBreed)
        private val viewUnderBar: View = view.findViewById(R.id.viewUnderBar)

        override fun display(item: PetBreedListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()

            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, offSetDIP, 0, 0)

            // 검색 결과가 전부일떄(검색 안했을 경우) -> 맨 위 품종 밑줄
            viewUnderBar.visibility =
                    if (item.dataType == AppConstants.ADAPTER_HEADER &&
                            (petBreedData.items.size == petBreedFinalData.items.size)) View.VISIBLE
                    else View.GONE

            // 품종 이름
            textViewBreed.setText(String.format("%s(%s)", item.nameKor, item.nameEn), TextView.BufferType.SPANNABLE)

            mainLayout.tag = item.code
            mainLayout.setOnClickListener(selectListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: PetBreedListData, position: Int)
    }
}
