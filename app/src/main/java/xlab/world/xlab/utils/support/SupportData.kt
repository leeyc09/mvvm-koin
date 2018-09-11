package xlab.world.xlab.utils.support

import java.text.SimpleDateFormat
import java.util.*

object SupportData {
    private const val YOUTUBE_THUMBNAIL_BASE_URL = "https://img.youtube.com/vi/"
    const val YOUTUBE_THUMB_120x90 = 0
    const val YOUTUBE_THUMB_320x180 = 1
    const val YOUTUBE_THUMB_480x360 = 2
    const val YOUTUBE_THUMB_640x480 = 3
    const val YOUTUBE_THUMB_1280x720 = 4

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

    // 1000 이상 k 로 표기
    fun countFormat(count: Int): String {
        return if (count < 1000) count.toString()
        else String.format("%.1fk", (count / 1000).toFloat()) // if count more than 1000, show ?k
    }
}