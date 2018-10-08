package xlab.world.xlab.view.postsUpload.picture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import kotlinx.android.synthetic.main.activity_post_upload_picture.*
import xlab.world.xlab.R
import xlab.world.xlab.utils.camera.CameraHelper
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.RequestCodeData
import xlab.world.xlab.view.postsUpload.picture.fragment.CameraCaptureFragment
import xlab.world.xlab.view.postsUpload.picture.fragment.GalleryImageSelectFragment

class PostUploadPictureActivity : AppCompatActivity() {

    lateinit var cameraHelper: CameraHelper

    private lateinit var galleryImageSelectFragment: GalleryImageSelectFragment
    private lateinit var cameraCaptureFragment: CameraCaptureFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_upload_picture)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        cameraHelper.cameraReload()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)

        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.POST_UPLOAD -> { // upload finish
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }

    private fun onSetup() {
        cameraHelper = CameraHelper(context = this, textureView = cameraTexture)

        // fragment 초기화
        galleryImageSelectFragment = GalleryImageSelectFragment.newFragment()
        cameraCaptureFragment = CameraCaptureFragment.newFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, galleryImageSelectFragment).commit()
    }

    private fun onBindEvent() {

    }

    private fun observeViewModel() {

    }

    fun switchFragment(fragment: Fragment) {
        if (fragment is GalleryImageSelectFragment){
            supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, cameraCaptureFragment).commit()
        } else if (fragment is CameraCaptureFragment) {
            supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, galleryImageSelectFragment).commit()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PostUploadPictureActivity::class.java)
        }
    }
}
