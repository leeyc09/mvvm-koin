package xlab.world.xlab.utils.font

import android.content.Context
import android.graphics.Typeface
import java.lang.reflect.Field
import java.util.HashMap

object CustomFont {
    const val notoSansCJKkrBlack = "fonts/NotoSansCJKkrBlack.otf"
    const val notoSansCJKkrBold = "fonts/NotoSansCJKkrBold.otf"
    const val notoSansCJKkrMedium = "fonts/NotoSansCJKkrMedium.otf"
    const val notoSansCJKkrRegular = "fonts/NotoSansCJKkrRegular.otf"

    const val spoqaHanSansBold = "fonts/SpoqaHanSansBold.ttf"
    const val spoqaHanSansRegular = "fonts/SpoqaHanSansRegular.ttf"

    private val fontCache = HashMap<String, Typeface>()

    fun getTypeface(fontName: String, context: Context): Typeface?{
        var typeface: Typeface? = fontCache[fontName]

        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.assets, fontName)
            } catch (e: Exception) {
                return null
            }
            fontCache[fontName] = typeface
        }

        return typeface
    }

    fun replaceDefaultFont(context: Context, nameOffFontBeingReplaced: String, nameOffFontInAsset: String) {
        val customFontTypeface: Typeface = Typeface.createFromAsset(context.assets, nameOffFontInAsset)
        replaceFont(nameOffFontBeingReplaced, customFontTypeface)
    }

    private fun replaceFont(nameOffFontBeingReplaced: String, customFontTypeface: Typeface) {
        val myField: Field = Typeface::class.java.getDeclaredField(nameOffFontBeingReplaced)
        myField.isAccessible = true
        myField.set(null, customFontTypeface)
    }
}