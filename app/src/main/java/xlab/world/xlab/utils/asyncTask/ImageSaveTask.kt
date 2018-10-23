package xlab.world.xlab.utils.asyncTask

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import java.io.File

class ImageSaveTask(private val data: ByteArray,
                    private val pictureFile: File,
                    private val isFrontCam: Boolean,
                    private val saveRotate: Float,
                    private val picturePath: (String) -> Unit): AsyncTask<Void, Void, String>() {
    private val tag = "ImageSaveTask"

    override fun doInBackground(vararg params: Void?): String {
        PrintLog.d("isFrontCam", isFrontCam.toString(), tag)

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap: Bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)
        val saveBitmap: Bitmap = SupportData.rotateCropSquareImage(bitmap, saveRotate, isFrontCam)
        bitmap.recycle()

        SupportData.saveFile(saveBitmap, pictureFile.absolutePath)
        saveBitmap.recycle()

        return pictureFile.absolutePath
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        picturePath(result!!)
    }
}