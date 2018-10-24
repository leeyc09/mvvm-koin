package xlab.world.xlab.view.profileEdit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_profile_edit.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultDialog
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.GenderSelectDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.register.RegisterViewModel

class ProfileEditActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private val profileEditViewModel: ProfileEditViewModel by viewModel()
    private val registerViewModel: RegisterViewModel by viewModel()
    private val spHelper: SPHelper by inject()
    private val letterOrDigitInputFilter: LetterOrDigitInputFilter by inject()
    private val permissionHelper: PermissionHelper by inject()

    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(R.drawable.profile_image_default)
            .error(R.drawable.profile_image_default)

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var editCancelDialog: DefaultDialog
    private lateinit var genderSelectDialog: GenderSelectDialog

    private val genderDialogListener = object : GenderSelectDialog.Listener {
        override fun onGenderSelect(genderTag: String, genderStr: String?) {
            genderStr?.let {
                textViewGender.tag = genderTag
                textViewGender.setText(genderStr, TextView.BufferType.SPANNABLE)
                textViewGender.isSelected = true
                profileEditViewModel.existChangedData(gender = genderTag.toInt())
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()

        profileEditViewModel.deleteProfileImage()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.PERMISSION_REQUEST_GALLERY_CODE -> {
                if (permissionHelper.resultRequestPermissions(results = grantResults))
                    profileImageLayout.performClick()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)

        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.GALLARY_IMAGE_SELECT -> { // 프로필 이미지 수정
                        data?.let {
                            val imageUri = data.getStringExtra(IntentPassName.IMAGE_URL)
                            profileEditViewModel.setNewProfileImage(profileImage = imageUri)
                            profileEditViewModel.existChangedData()
                        }
                    }
                }
            }
        }
    }

    private fun onSetup() {
        // 타이틀 설정
        actionBarTitle.setText(getString(R.string.profile_edit), TextView.BufferType.SPANNABLE)

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        editCancelDialog = DialogCreator.editCancelDialog(context= this)
        genderSelectDialog = DialogCreator.genderSelectDialog(listener = genderDialogListener)

        // 출생연도 숫자만 가능, 4글자 제한
        editTextBirth.filters = arrayOf(letterOrDigitInputFilter, InputFilter.LengthFilter(4))

        profileEditViewModel.loadProfileEditData(authorization = spHelper.authorization)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionBtn.setOnClickListener(this) // 프로필 변경
        profileImageLayout.setOnClickListener(this) // 프로필 이미지 변경
        editTextNick.setOnTouchListener(this) // 닉네임 지우기
        textViewGender.setOnClickListener(this) // 성별 선택

        // 닉네임 지우기 이미지 활성화 이벤트
        ViewFunction.onFocusChange(editText = editTextNick) { hasFocus ->
            editTextNick.setCompoundDrawablesWithIntrinsicBounds(0,0,
                    if (hasFocus) R.drawable.textdelete_black
                    else 0,0)
            confirmNickLayout.visibility =
                    if (!hasFocus && textViewConfirmNick.isSelected) View.INVISIBLE
                    else View.VISIBLE
        }
        // 출생연도 힌트 이벤트
        ViewFunction.onFocusChange(editText = editTextBirth) { hasFocus ->
            editTextBirth.hint =
                    if(hasFocus) getString(R.string.birth_hint)
                    else getString(R.string.none_type)
        }

        // 닉네임, 소개, 출생연도 입력 이벤트
        ViewFunction.onTextChange(editText = editTextNick) { _ ->
            if (editTextNick.hasFocus()) {
                registerViewModel.nickNameRegexCheck(context = this, nickName = getNickNameText())
                profileEditViewModel.existChangedData(nickName = getNickNameText())
            }
        }
        ViewFunction.onTextChange(editText = editTextIntroduction) { _ ->
            if (editTextIntroduction.hasFocus()) {
                textViewIntroductionNum.text = (100 - getIntroductionText().length).toString()
                profileEditViewModel.existChangedData(introduction = getIntroductionText())
            }
        }
        ViewFunction.onTextChange(editText = editTextBirth) { _ ->
            if (editTextBirth.hasFocus()) {
                profileEditViewModel.birthRegexCheck(birth = getBirthText())
                profileEditViewModel.existChangedData(birth = getBirthText())
            }
        }
    }

    private fun observeViewModel() {
        // TODO: Profile Edit View Model
        // UI 이벤트 observe
        profileEditViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                }
                uiData.resultCode?.let {
                    setResult(it)
                    finish()
                }
                uiData.profileImage?.let {
                    Glide.with(this)
                            .load(it)
                            .apply(glideOption)
                            .into(imageViewProfile)
                }
                uiData.nickName?.let {
                    editTextNick.setText(it)
                }
                uiData.introduction?.let {
                    editTextIntroduction.setText(it)
                }
                uiData.detailInfoVisibility?.let {
                    textViewProfileDetail.visibility = it
                }
                uiData.gender?.let {
                    if (it.isNotEmpty())
                        textViewGender.setText(it, TextView.BufferType.SPANNABLE)
                    textViewGender.isSelected = it.isNotEmpty()
                }
                uiData.birth?.let {
                    if (it.isNotEmpty())
                        editTextBirth.setText(it)
                }
                uiData.birthConfirmVisibility?.let {
                    confirmBirthLayout.visibility = it
                }
                uiData.saveEnable?.let {
                    actionBtn.isEnabled = it
                }
                uiData.editCancelDialogShow?.let {
                    editCancelDialog.show()
                }
            }
        })

        // TODO: Register View Model
        // UI 이벤트 observe
        registerViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                }
                uiData.nickNameRegexText?.let {
                    textViewConfirmNick.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.nickNameRegex?.let {
                    confirmNickLayout.visibility = View.VISIBLE
                    textViewConfirmNick.isSelected = it
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    profileEditViewModel.backBtnAction()
                }
                R.id.actionBtn -> { // 프로필 변경
                    profileEditViewModel.changeProfileData(
                            authorization = spHelper.authorization,
                            userId = spHelper.userId)
                }
                R.id.profileImageLayout -> { // 프로필 이미지 변경
                    currentFocus?.clearFocus()
                    v.requestFocus()

                    if (!permissionHelper.hasPermission(context = this, permissions = permissionHelper.galleryPermissions)) {
                        permissionHelper.requestGalleryPermissions(context = this)
                        return
                    }

                    RunActivity.galleryImageSelectActivity(context = this, withOverlay = true)
                }
                R.id.textViewGender -> { // 성별 선택
                    currentFocus?.clearFocus()
                    v.requestFocus()
                    genderSelectDialog.show(supportFragmentManager, "GenderSelectDialog")
                }
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.let {
            when (v.id) {
                R.id.editTextNick -> { // 닉네임 지우기
                    ViewFunction.onDrawableTouch(v as EditText, event!!) { isTouch ->
                        if (isTouch) {
                            v.setText("")
                            v.performClick()
                        }
                    }
                }
            }
        }
        return false
    }

    private fun getNickNameText() = editTextNick.text?.trim().toString()
    private fun getIntroductionText() = editTextIntroduction.text?.trim().toString()
    private fun getGender(): String? {
        return UserInfo.genderMap[(textViewGender.tag as String).toInt()]
    }
    private fun getBirthText() = editTextBirth.text?.trim().toString()

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ProfileEditActivity::class.java)
        }
    }
}
