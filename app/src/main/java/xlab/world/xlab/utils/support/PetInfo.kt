package xlab.world.xlab.utils.support

import android.content.Context
import com.opencsv.CSVReader
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.PetBreedListData
import xlab.world.xlab.data.response.ResUserDefaultData
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Serializable
import java.util.HashMap

class PetInfo(context: Context) {
    private val tag = "PetInfo"

    // pet type
    val dogCode: String = context.getString(R.string.dog_code)
    val catCode: String = context.getString(R.string.cat_code)
    val petType: HashMap<String, String> = HashMap() // code, name

    // pet gender type
    val femaleCode: String = context.getString(R.string.pet_female_code)
    val maleode: String = context.getString(R.string.pet_male_code)
    val petGender: HashMap<String, String> = HashMap() // code, name

    // pet hair type
    private val hairShort: String = "PAD01"
    private val hairLong: String = "PAD02"
    private val hairCurl: String = "PAD03"
    val petHairType: HashMap<String, String> = HashMap() // code, name

    // pet hair color
    val petHairColor : HashMap<String, HairColorData> = HashMap() // code, name

    // pet size
    private val sizeXSmall: String = "PAB01"
    private val sizeSmall: String = "PAB02"
    private val sizeMedium: String = "PAB03"
    private val sizeLarge: String = "PAB04"
    private val sizeXLarge: String = "PAB05"
    val petSize = HashMap<String, String>() // code, name

    // pet info list
    val dogBreedInfo = ArrayList<PetBreedListData>()
    val catBreedInfo = ArrayList<PetBreedListData>()

    init {
        petType.putAll(getPetData(context.assets.open("petdata/type.csv")))
        petSize.putAll(getPetData(context.assets.open("petdata/size.csv")))
        petGender.putAll(getPetData(context.assets.open("petdata/gender.csv")))
        petHairType.putAll(getPetData(context.assets.open("petdata/hair_type.csv")))
        petHairColor.putAll(getPetHairColorData(context.assets.open("petdata/hair_color.csv")))
        dogBreedInfo.addAll(getPetBreedData(context.assets.open("petdata/dog_breed.csv")))
        catBreedInfo.addAll(getPetBreedData(context.assets.open("petdata/cat_breed.csv")))
    }

    // hash map key 로 value 찾기
    fun findKeyByValue(map: HashMap<String, String>, value: String): String {
        for (key in map.keys) {
            if (map[key] == value) {
                return key
            }
        }
        return ""
    }

    // pet data csv 파일 읽기
    private fun getPetData(inputStream: InputStream): HashMap<String, String> {
        val petDataMap = HashMap<String, String>()
        try {
            val csvReader = CSVReader(InputStreamReader(inputStream, "UTF-8"))
            var rawData: Array<String>? = null
            var isFirstLine = true
            while({rawData = csvReader.readNext(); rawData}() != null) {
                if (isFirstLine) { // skip first line
                    isFirstLine = false
                }
                else {
                    petDataMap[rawData!![1]] = rawData!![0]
                    PrintLog.d("petData",  "${rawData!![0]} - ${rawData!![1]}", tag)
                }
            }
        } catch (e: FileNotFoundException) {
            PrintLog.e("error csv convert", e.message!!, tag)
        } finally {
            return petDataMap
        }
    }

    // pet hair color data csv 파일 읽기
    private fun getPetHairColorData(inputStream: InputStream): HashMap<String, HairColorData> {
        val petDataMap = HashMap<String, HairColorData>()
        try {
            val csvReader = CSVReader(InputStreamReader(inputStream, "UTF-8"))
            var rawData: Array<String>? = null
            var isFirstLine = true
            while({rawData = csvReader.readNext(); rawData}() != null) {
                if (isFirstLine) { // skip first line
                    isFirstLine = false
                }
                else {
                    petDataMap[rawData!![2]] = HairColorData(name = rawData!![0], colorCode = rawData!![1])
                    PrintLog.d("petData",  "${rawData!![2]} - (${rawData!![0]}, ${rawData!![1]})", tag)
                }
            }
        } catch (e: FileNotFoundException) {
            PrintLog.e("error csv convert", e.message!!, tag)
        } finally {
            return petDataMap
        }
    }

    // pet breed data csv 파일 읽기
    private fun getPetBreedData(inputStream: InputStream): ArrayList<PetBreedListData> {
        val breedInfoList = ArrayList<PetBreedListData>()
        try {
            val csvReader = CSVReader(InputStreamReader(inputStream, "UTF-8"))
            var breedInfo: Array<String>? = null
            var isFirstLine = true
            var dataType = AppConstants.ADAPTER_HEADER
            while({breedInfo = csvReader.readNext(); breedInfo}() != null) {
                if (isFirstLine) {
                    isFirstLine = false
                }
                else {
                    val hairType = ArrayList<String>()
                    hairType.addAll(breedInfo!![4].split(","))

                    val hairColor = ArrayList<String>()
                    hairColor.addAll(breedInfo!![5].split(","))

                    val size = ArrayList<String>()
                    size.addAll(breedInfo!![6].split(","))
                    breedInfoList.add(PetBreedListData(
                            dataType = dataType,
                            code = breedInfo!![1],
                            nameKor = breedInfo!![2],
                            nameEn = breedInfo!![3],
                            hairType = hairType,
                            hairColor = hairColor,
                            size = size
                    ))
                    dataType = AppConstants.ADAPTER_CONTENT
                    PrintLog.d("breedInfo", breedInfoList.last().toString(), tag)
                }
            }
        } catch (e: FileNotFoundException) {
            PrintLog.e("error csv convert", e.message!!, tag)
        } finally {
            return breedInfoList
        }
    }

    // topic(pet) 종 이름 가져오기
    fun getTopicBreed(petData: ArrayList<ResUserDefaultData.UserPetInfo>?): ArrayList<String>{
        val topicBreed = ArrayList<String>()
        petData?.forEach { pet ->
            val breedStr = when (pet.type) {
                dogCode -> dogBreedInfo[pet.breed.toInt()].nameKor
                catCode -> catBreedInfo[pet.breed.toInt()].nameKor
                else -> ""
            }
            if (breedStr.isNotEmpty())
                topicBreed.add(breedStr)
        }
        return topicBreed
    }
}


data class HairColorData(val name: String, val colorCode: String): Serializable