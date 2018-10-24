package xlab.world.xlab.data.adapter

import java.io.Serializable

data class PetHairFeatureData(val items: ArrayList<PetHairFeatureListData> = ArrayList()): Serializable {

    fun updateData(petHairFeatureData: PetHairFeatureData) {
        items.clear()
        petHairFeatureData.items.forEach {
            items.add(it.copy())
        }
    }
}
