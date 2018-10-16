package xlab.world.xlab.utils.asyncTask

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class DownloadFileTask(private val apkFileName: (String) -> Unit): AsyncTask<String, Void, String> () {

    override fun doInBackground(vararg urls: String): String {
        var myFileUrl: URL? = null
        try {
            myFileUrl = URL(urls[0])
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        try {
            val conn = myFileUrl!!.openConnection() as HttpURLConnection
            conn.doInput = true
            conn.connect()
            val inputStream = conn.inputStream

            // 다운 받는 파일의 경로는 sdcard/ 에 저장되며 sdcard에 접근하려면 uses-permission에
            // android.permission.WRITE_EXTERNAL_STORAGE을 추가해야만 가능.
            val mPath = "sdcard/v3mobile.apk"
            val fos: FileOutputStream
            val f = File(mPath)
            if (f.createNewFile()) {
                fos = FileOutputStream(mPath)
                var read: Int = -1
                while ({read = inputStream.read(); read }() != -1) {
                    fos.write(read)
                }
                fos.close()
            }

            return "v3mobile.apk"
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }

    }

    override fun onPostExecute(filename: String) {
        if (filename.isNotEmpty()) {
            apkFileName(filename)
        }
    }
}