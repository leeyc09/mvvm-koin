package xlab.world.xlab.utils.support

import android.content.Context
import com.opencsv.CSVReader
import xlab.world.xlab.data.adapter.PetBreedListData
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Serializable
import java.util.HashMap

object UserInfo {
    // gender type
    const val NO_GENDER_SELECT = -1
    const val FEMALE = 0
    const val MALE = 1
    const val ETC_GENDER = 2

    val genderMap = hashMapOf(
            NO_GENDER_SELECT to "",
            FEMALE to "여성",
            MALE to "남성",
            ETC_GENDER to "기타"
    )
}