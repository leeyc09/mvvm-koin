package xlab.world.xlab.view.topicEdit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_topic_pet_edit.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.PetHairTypeAdapter
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.TopicColorSelectDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class TopicPetEditActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private val topicPetEditViewModel: TopicPetEditViewModel by viewModel()
    private val spHelper: SPHelper by inject()
    private val petInfo: PetInfo by inject()
    private val permissionHelper: PermissionHelper by inject()

    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.pet_profile_image_default)
            .error(R.drawable.pet_profile_image_default)

    private var petNo: Int? = null

    private lateinit var petHairTypeAdapter: PetHairTypeAdapter

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var topicColorSelectDialog: TopicColorSelectDialog

    private val topicColorListener = object:TopicColorSelectDialog.Listener {
        override fun onTopicColorSelect(selectColorStr: String) {
            topicPetEditViewModel.existChangedData(isAddPet = petNo?.let{_->false}?:let{_->true},
                    topicColor = selectColorStr)
            topicColorBtn.tag = selectColorStr
            topicColorBtn.setBackgroundColor(Color.parseColor("#$selectColorStr"))
//            actionBtn.enableSave()
        }
    }
    private val petHairTypeListener = View.OnClickListener { view ->

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_pet_edit)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()

        topicPetEditViewModel.deleteProfileImage()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.PERMISSION_REQUEST_GALLERY_CODE -> {
                if (permissionHelper.resultRequestPermissions(results = grantResults))
                    imageViewPet.performClick()
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
                    RequestCodeData.GALLARY_IMAGE_SELECT -> { // pet 이미지 수정
                        // profile image change success
                        val imageUri = data!!.getStringExtra(IntentPassName.IMAGE_URL)
                        topicPetEditViewModel.setNewProfileImage(petImage = imageUri)
                    }
                    RequestCodeData.TOPIC_BREED -> {
                        val breedIndex = data!!.getStringExtra(IntentPassName.BREED_INDEX)
                        topicPetEditViewModel.setBreedDetailData(breedIndex = breedIndex)
                    }
                }
            }
        }
    }

    private fun onSetup() {
        petNo = intent.getStringExtra(IntentPassName.PET_NO)?.toInt()

        val topicColorStrArray = resources.getStringArray(R.array.topicColorStringList)
        petNo?.let { // pet 수정
            actionBarTitle.setText(getString(R.string.topic_pet_edit), TextView.BufferType.SPANNABLE)
//            breedDetailVisibility = View.VISIBLE
            breedDetailLayout.visibility = View.VISIBLE
            topicDeleteBtn.visibility = View.VISIBLE
        } ?:let { // pet 추가
            actionBarTitle.setText(getString(R.string.topic_pet_add), TextView.BufferType.SPANNABLE)
//            breedDetailVisibility = View.GONE
            breedDetailLayout.visibility = View.GONE
            topicDeleteBtn.visibility = View.GONE
            topicColorBtn.tag = topicColorStrArray[0]
            topicColorBtn.setBackgroundColor(Color.parseColor("#${topicColorStrArray[0]}"))
        }

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        topicColorSelectDialog = DialogCreator.topicColorSelectDialog(selectPosition = 0, listener = topicColorListener)

        // petHairTypeAdapter adapter & recycler 초기화
        petHairTypeAdapter = PetHairTypeAdapter(context = this,
                selectListener = petHairTypeListener)
        recyclerViewHairType.adapter = petHairTypeAdapter
        recyclerViewHairType.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewHairType.addItemDecoration(CustomItemDecoration(context = this, right = 20f))
        (recyclerViewHairType.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        petNo?.let {
            topicPetEditViewModel.loadPetData(userId = spHelper.userId, petNo = it, topicColorList = resources.getStringArray(R.array.topicColorStringList))
            topicPetEditViewModel.existChangedData(isAddPet = false)
        } ?: let {
            topicPetEditViewModel.existChangedData(isAddPet = true)
        }
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionBtn.setOnClickListener(this) // 토픽 (추가, 수정)
        topicColorBtn.setOnClickListener(this) // 토픽 색상 변경
        imageViewPet.setOnClickListener(this) // 토픽 이미지 변경
        textViewDog.setOnClickListener(this) // 강아지 선택
        textViewCat.setOnClickListener(this) // 고양이 선택
        editTextPetName.setOnTouchListener(this) // 이름 지우기
        textViewPetFemale.setOnClickListener(this) // 암컷 선택
        textViewPetMale.setOnClickListener(this) // 수컷 선택
        neuteredBtn.setOnClickListener(this) // 중성화 선택
        breedLayout.setOnClickListener(this) // 품종 선택

        // pet 이름 지우기 버튼 활성화
        ViewFunction.onFocusChange(editText = editTextPetName) { hasFocus ->
            editTextPetName.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    if (hasFocus) R.drawable.textdelete_black else 0, 0)
        }

        ViewFunction.onTextChange(editText = editTextPetName) {
            topicPetEditViewModel.existChangedData(isAddPet = petNo?.let{_->false}?:let{_->true},
                    petName = it)
        }

        // pet 생년월일 입력 이벤트
        ViewFunction.birthWatcher(editText = editTextPetBirth) {
            topicPetEditViewModel.birthRegex(petBirth = it)
        }
        // pet weight 입력 이벤트
        ViewFunction.onTextChange(editText = editTextPetWeight) {
            topicPetEditViewModel.existChangedData(isAddPet = petNo?.let{_->false}?:let{_->true},
                    petWeight = try { it.toFloat()} catch (e: NumberFormatException) {-1f})
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        topicPetEditViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.topicColorData?.let {
                    topicColorBtn.tag = it.topicColor
                    topicColorBtn.setBackgroundColor(Color.parseColor("#${it.topicColor}"))
                    topicColorSelectDialog = DialogCreator.topicColorSelectDialog(selectPosition = it.colorIndex, listener = topicColorListener)
                }
                uiData.petImage?.let {
                    Glide.with(this)
                            .load(it)
                            .apply(glideOption)
                            .into(imageViewPet)
                }
                uiData.petType?.let {
                    textViewDog.isSelected = it
                    textViewCat.isSelected = !it
                }
                uiData.petName?.let {
                    editTextPetName.setText(it)
                }
                uiData.petGender?.let {
                    textViewPetFemale.isSelected = it
                    textViewPetMale.isSelected = !it
                }
                uiData.petNeutered?.let {
                    neuteredBtn.isSelected = it
                }
                uiData.breedName?.let {
                    textViewPetBreed.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.isBreedSelect?.let {
                    textViewPetBreed.isSelected = it
                }
                uiData.breedDetailVisibility?.let {
                    breedDetailLayout.visibility = it
                }
                uiData.hairTypeData?.let {
                    petHairTypeAdapter.updateData(petHairTypeData = it)
                }
                uiData.hairTypeVisibility?.let {
                    hairTypeLayout.visibility = it
                }
                uiData.petBirth?.let {
                    editTextPetBirth.setText(it)
                }
                uiData.birthRegexVisibility?.let {
                    layoutConfirmBirth.visibility = it
                    topicPetEditViewModel.existChangedData(isAddPet = petNo?.let{_->false}?:let{_->true})
                }
                uiData.petWeight?.let {
                    editTextPetWeight.setText(it)
                }

                uiData.saveEnable?.let {
                    actionBtn.isEnabled = it
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            ViewFunction.hideKeyboard(context = this, view = v)
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
//                    if (actionBtn.isEnabled) {
//                        editCancelDialog.show()
//                        return
//                    }
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                R.id.actionBtn -> { // 토픽 (추가, 수정)

                }
                R.id.topicColorBtn -> { // 토픽 색상 변경
                    topicColorSelectDialog.show(supportFragmentManager, "TopicColorSelectDialog")
                }
                R.id.imageViewPet -> { // 토픽 이미지 변경
                    currentFocus?.clearFocus()
                    v.requestFocus()

                    if (!permissionHelper.hasPermission(context = this, permissions = permissionHelper.galleryPermissions)) {
                        permissionHelper.requestGalleryPermissions(context = this)
                        return
                    }

                    RunActivity.galleryImageSelectActivity(context = this, withOverlay = false)
                }
                R.id.textViewDog, R.id.textViewCat -> { // 강아지, 고양이 선택
                    topicPetEditViewModel.existChangedData(isAddPet = petNo?.let{_->false}?:let{_->true},
                            petType = v.tag as String)
                }
                R.id.textViewPetFemale, R.id.textViewPetMale -> { // 암컷, 수컷 선택
                    topicPetEditViewModel.existChangedData(isAddPet = petNo?.let{_->false}?:let{_->true},
                            petGender = v.tag as String)
                }
                R.id.neuteredBtn -> { // 중성화 선택
                    topicPetEditViewModel.existChangedData(isAddPet = petNo?.let{_->false}?:let{_->true},
                            petNeutered = v.isSelected)
                }
                R.id.breedLayout -> { // 품종 선택
                    if (textViewDog.isSelected) RunActivity.petBreedActivity(context= this, petType = petInfo.dogCode)
                    else if (textViewCat.isSelected) RunActivity.petBreedActivity(context = this, petType = petInfo.catCode)
                }
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.let {
            when (v.id) {
                R.id.editTextPetName -> { // 이름 지우기
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

    companion object {
        fun newIntent(context: Context, petNo: Int?): Intent {
            val intent = Intent(context, TopicPetEditActivity::class.java)
            intent.putExtra(IntentPassName.PET_NO, petNo?.toString())

            return intent
        }
    }
}
