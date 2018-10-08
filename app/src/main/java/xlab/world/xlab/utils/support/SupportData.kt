package xlab.world.xlab.utils.support

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object SupportData {
    private const val YOUTUBE_THUMBNAIL_BASE_URL = "https://img.youtube.com/vi/"
    const val YOUTUBE_THUMB_120x90 = 0
    const val YOUTUBE_THUMB_320x180 = 1
    const val YOUTUBE_THUMB_480x360 = 2
    const val YOUTUBE_THUMB_640x480 = 3
    const val YOUTUBE_THUMB_1280x720 = 4

    private val timeStamp: String
        get() {
            return "xlab_" + System.currentTimeMillis()
        }

    // youtube id 로 해당 영상 썸네일 가져오기
    fun getYoutubeThumbnailUrl(videoId: String, quality: Int): String {
        return when (quality) {
            YOUTUBE_THUMB_120x90 -> "$YOUTUBE_THUMBNAIL_BASE_URL$videoId/default.jpg" // (120x90)
            YOUTUBE_THUMB_320x180 -> "$YOUTUBE_THUMBNAIL_BASE_URL$videoId/mqdefault.jpg" // (320x180)
            YOUTUBE_THUMB_480x360 -> "$YOUTUBE_THUMBNAIL_BASE_URL$videoId/hqdefault.jpg" // (480x360)
            YOUTUBE_THUMB_640x480 -> "$YOUTUBE_THUMBNAIL_BASE_URL$videoId/sddefault.jpg" // (640x480)
            YOUTUBE_THUMB_1280x720 -> "$YOUTUBE_THUMBNAIL_BASE_URL$videoId/maxresdefault.jpg" // (1280x720, 1920x1080)
            else -> "$YOUTUBE_THUMBNAIL_BASE_URL$videoId/default.jpg"
        }
    }

    // 시간기준 날짜 표기 가져오기
    fun contentDateForm(year: Int, month: Int, day: Int, hour: Int, minute: Int): String {
        // get current time
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm")
        val dateStr = timeFormat.format(calendar.time)
        val timeList = ArrayList<Int>()
        dateStr.split("-").forEach { // year,month,day,hour,min
            timeList.add(it.toInt())
        }

        return if (year == timeList[0] && month == timeList[1] && day == timeList[2]) { // same day
            if (timeList[3] - hour > 0) { // other hour
                "${(timeList[3] - hour)}시간"
            } else { // same hour
                if (timeList[4] - minute > 0) { // other minute
                    "${(timeList[4] - minute)}분"
                } else { // same minute
                    "방금전"
                }
            }
        } else { // other day
            String.format("%d.%s.%s",year,
                    if (month > 9) month.toString()
                    else "0$month",
                    if (day > 9) day.toString()
                    else "0$day")
        }
    }

    // 날짜 표기 (같은 연도 생략)
    fun contentDateForm(year: Int, month: Int, day: Int): String {
        // get current time
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("yyyy-MM-dd")
        val dateStr = timeFormat.format(calendar.time)
        val timeList = ArrayList<Int>()
        dateStr.split("-").forEach { // year,month,day,hour,min
            timeList.add(it.toInt())
        }

        val monthDayStr = String.format("%s.%s",
                if (month > 9) month.toString()
                else "0$month",
                if (day > 9) day.toString()
                else "0$day")

        return if (year == timeList[0]) { // 연도 생략
            monthDayStr
        } else { // other day
            String.format("%d.%s",year, monthDayStr)
        }
    }

    // 생년월일 표기
    fun birthDayForm(year: Int, month: Int, day: Int): String {
        return String.format(
                "%d-%s-%s",
                year,
                if (month > 9) month.toString()
                else "0$month",
                if (day > 9) day.toString()
                else "0$day")
    }

    // 1000 이상 k 로 표기
    fun countFormat(count: Int): String {
        return if (count < 1000) count.toString()
        else String.format("%.1fk", (count / 1000).toFloat()) // if count more than 1000, show ?k
    }


    // 원화 표기
    fun applyPriceFormat(price: Int): String {
        val priceFormat = NumberFormat.getInstance(Locale.KOREA) as DecimalFormat
        priceFormat.applyPattern("#,###,###,###")
        return priceFormat.format(price)
    }

    // authorization 으로 guest 판단하기
    fun isGuest(authorization: String): Boolean {
        return authorization.replace("Bearer", "").trim().isEmpty()
    }

    // folder 없으면 생성
    private fun createFolder(path: String): File {
        val mediaStorageDirPic = File(path)

        if (!mediaStorageDirPic.exists())
            mediaStorageDirPic.mkdirs()

        return mediaStorageDirPic
    }


    fun rotateCropSquareImage(src: Bitmap, degree: Float, flip: Boolean): Bitmap {
        // create new matrix
        val matrix = Matrix()
        // flip horizontal
        if (flip)
            matrix.preScale(-1.0f, 1.0f)
        // setup rotation degree
        matrix.postRotate(degree)

        // rotate bitmap
        val rotateBitmap = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)

        // crop bitmap
        val croppedSize = if (src.width > src.height) src.height else src.width
        return Bitmap.createBitmap(rotateBitmap, 0, 0, croppedSize, croppedSize)
    }

    // temp file 생성
    fun createTmpFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), ".xlab")

        if (!mediaStorageDir.exists())
            mediaStorageDir.mkdirs()

        // Create a media file name
        return when (type) {
            AppConstants.MEDIA_VIDEO -> File(createFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + AppConstants.TMP_VIDEO_FOLDER).absolutePath + File.separator +
                    timeStamp + ".mp4")
            AppConstants.MEDIA_GIF -> File(createFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + AppConstants.TMP_GIF_FOLDER).absolutePath + File.separator +
                    timeStamp + ".gif")
            AppConstants.MEDIA_IMAGE -> File(createFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + AppConstants.TMP_PICTURE_FOLDER).absolutePath + File.separator +
                    timeStamp + ".jpg")
            else -> File(createFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + AppConstants.TMP_PICTURE_FOLDER).absolutePath + File.separator +
                    timeStamp + ".jpg")
        }
    }

    // release file 생성
    fun createReleaseFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "xlab")

        if (!mediaStorageDir.exists())
            mediaStorageDir.mkdirs()

        // Create a media file name
        return when (type) {
            AppConstants.MEDIA_VIDEO -> File(createFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + AppConstants.VIDEO_FOLDER).absolutePath + File.separator +
                    timeStamp + ".mp4")
            AppConstants.MEDIA_GIF -> File(createFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + AppConstants.GIF_FOLDER).absolutePath + File.separator +
                    timeStamp + ".gif")
            AppConstants.MEDIA_IMAGE -> File(createFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + AppConstants.PICTURE_FOLDER).absolutePath + File.separator +
                    timeStamp + ".jpg")
            else -> File(createFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + AppConstants.PICTURE_FOLDER).absolutePath + File.separator +
                    timeStamp + ".jpg")
        }
    }

    // file 저장
    fun saveFile(bitmap: Bitmap, path: String, quality: Int = 90) {
        val file = File(path)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.flush()
            out.close()
            PrintLog.d("save file", file.absolutePath, "File")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // file 삭제
    fun deleteFile(path: String) {
        val file = File(path)
        if (file.exists()) {
            PrintLog.d("delete file", file.absolutePath, "File")
            file.delete()
        }
    }

    @Throws(IOException::class)
    fun moveFile(src: File, dst: File): String {
        src.renameTo(dst)
        return dst.absolutePath
    }
}