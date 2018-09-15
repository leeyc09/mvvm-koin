package xlab.world.xlab

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import xlab.world.xlab.utils.support.AppConstants

/**
 * Created by dongunkim on 2018. 2. 21..
 */
class XlabPermission(var context: Context) {
    val cameraPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA) //, Manifest.permission.RECORD_AUDIO)
    val galleryPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//    val callPermissions = arrayOf(Manifest.permission.CALL_PHONE)

    fun hasPermission(permissions: Array<String>): Boolean {
        permissions.forEach { permission ->
            // permission not allow
            if ((context as Activity).checkCallingOrSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED)
                return false
        }
        // permission allow
        return true
    }

    fun resultRequestPermissions(results: IntArray): Boolean {
        results.forEach { result ->
            if (result != PackageManager.PERMISSION_GRANTED)
                return false
        }
        // all permission allow
        return true
    }

    fun requestCameraPermissions() {
        // request permission higher api 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(context as Activity, cameraPermissions, AppConstants.PERMISSION_REQUEST_CAMERA_CODE)
        }
    }

    fun requestGalleryPermissions() {
        // request permission higher api 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(context as Activity, galleryPermissions, AppConstants.PERMISSION_REQUEST_GALLERY_CODE)
        }
    }

//    fun requestCallPermission() {
//        // request permission higher api 23
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ActivityCompat.requestPermissions(context as Activity, callPermissions, SupportData.PERMISSION_REQUEST_CALL_CODE)
//        }
//    }
}