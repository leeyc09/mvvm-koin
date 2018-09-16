package xlab.world.xlab.utils.support

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat

class PermissionHelper {
    val cameraPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA) //, Manifest.permission.RECORD_AUDIO)
    val galleryPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    // 해당 권한 허가 체크
    fun hasPermission(context: Activity, permissions: Array<String>): Boolean {
        permissions.forEach { permission ->
            // permission not allow
            if (context.checkCallingOrSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED)
                return false
        }
        // permission allow
        return true
    }

    // 권한 요청 결과
    fun resultRequestPermissions(results: IntArray): Boolean {
        results.forEach { result ->
            if (result != PackageManager.PERMISSION_GRANTED)
                return false
        }
        // all permission allow
        return true
    }

    // 카메라 권한 요청
    fun requestCameraPermissions(context: Activity) {
        // request permission higher api 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(context, cameraPermissions, AppConstants.PERMISSION_REQUEST_CAMERA_CODE)
        }
    }

    // 갤러리 권한 요청
    fun requestGalleryPermissions(context: Activity) {
        // request permission higher api 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(context, galleryPermissions, AppConstants.PERMISSION_REQUEST_GALLERY_CODE)
        }
    }
}