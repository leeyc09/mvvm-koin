package xlab.world.xlab.data.adapter

import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class PetBreedListData(var dataType: Int = AppConstants.ADAPTER_CONTENT,
                            val code: String,
                            val nameKor: String,
                            val nameEn: String,
                            val hairType: ArrayList<String>,
                            val hairColor: ArrayList<String>,
                            val size: ArrayList<String>): Serializable {
    override
    fun toString(): String = "$code $nameKor $nameEn $hairType $hairColor $size"
}