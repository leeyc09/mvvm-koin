package xlab.world.xlab.view.postsUpload

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager
import kotlinx.android.synthetic.main.activity_post_upload_picture.*
import xlab.world.xlab.R
import xlab.world.xlab.utils.camera.CameraHelper
import xlab.world.xlab.view.postsUpload.fragment.GalleryImageSelectFragment

class PostUploadPictureActivity : AppCompatActivity() {

    private lateinit var cameraHelper: CameraHelper

    private lateinit var galleryImageSelectFragment: GalleryImageSelectFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_upload_picture)

        onSetup()

        onBindEvent()

        observeViewModel()

//        captureBtn.setOnClickListener {
//            cameraHelper.takePicture { picturePath ->
//                if (picturePath.isNotEmpty()) {
//                    PrintLog.d("picturePath", picturePath, "Camera")
//                }
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
        cameraHelper.cameraReload()
    }

    private fun onSetup() {
        cameraHelper = CameraHelper(context = this, textureView = cameraTexture)

        // fragment 초기화
        galleryImageSelectFragment = GalleryImageSelectFragment.newFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, galleryImageSelectFragment).commit()
    }

    private fun onBindEvent() {

    }

    private fun observeViewModel() {

    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PostUploadPictureActivity::class.java)
        }
    }
}
