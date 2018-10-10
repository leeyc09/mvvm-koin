package xlab.world.xlab.data.adapter

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class SelectUsedGoodsListData(var dataType: Int,
                                   val goodsCode: String,
                                   val imageURL: String,
                                   val goodsName: String,
                                   val goodsBrand: String,
                                   var isSelect: Boolean = false): Serializable, Parcelable {

    constructor(parcel: Parcel): this(
            parcel.readInt(),
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest!!.writeInt(dataType)
        dest.writeString(goodsCode)
        dest.writeString(imageURL)
        dest.writeString(goodsName)
        dest.writeString(goodsBrand)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR: Parcelable.Creator<SelectUsedGoodsListData> {
        override fun newArray(size: Int): Array<SelectUsedGoodsListData> {
            return newArray(size)
        }

        override fun createFromParcel(source: Parcel?): SelectUsedGoodsListData {
            return SelectUsedGoodsListData(source!!)
        }
    }
}
