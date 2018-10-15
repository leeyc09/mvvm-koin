package xlab.world.xlab.view.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.action_bar_profile.*
import kotlinx.android.synthetic.main.activity_profile.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.ProfileTopicAdapter
import xlab.world.xlab.adapter.viewPager.ViewStatePagerAdapter
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.TwoSelectBottomDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.tabLayout.TabLayoutHelper
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.profile.fragment.ProfileAlbumFragment
import xlab.world.xlab.view.profile.fragment.ProfilePetFragment

class ProfileActivity : AppCompatActivity(), View.OnClickListener {
    private val profileViewModel: ProfileViewModel by viewModel()
    private val fontColorSpan: FontColorSpan by inject()
    private val spHelper: SPHelper by inject()
    private val permissionHelper: PermissionHelper by inject()

    private val profileGlideOption = RequestOptions()
            .circleCrop()
            .placeholder(R.drawable.profile_img_100)
            .error(R.drawable.profile_img_100)

    private var resultCode = Activity.RESULT_CANCELED
    private var userId = ""

    private lateinit var tabLayoutHelper: TabLayoutHelper

    private lateinit var profileTopicAdapter: ProfileTopicAdapter

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var postUploadTypeSelectDialog: TwoSelectBottomDialog

    private lateinit var viewPagerAdapter: ViewStatePagerAdapter
    private lateinit var profileAlbumFragment: ProfileAlbumFragment
    private lateinit var profilePetFragment: ProfilePetFragment

    private val topicSelectListener = View.OnClickListener { view ->
        RunActivity.petDetailActivity(context = this@ProfileActivity, userId = userId, petNo = view.tag as Int, petTotal = profileTopicAdapter.dataTotal)
    }
    private val topicAddListener = View.OnClickListener {
        RunActivity.petEditActivity(context = this@ProfileActivity, petNo = null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.PERMISSION_REQUEST_CAMERA_CODE -> {
                if (permissionHelper.resultRequestPermissions(results = grantResults))
                    actionPostUploadBtn.performClick()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)

        when (resultCode) {
            Activity.RESULT_OK -> {
                if (this.resultCode == Activity.RESULT_CANCELED)
                    this.resultCode = Activity.RESULT_OK
                when (requestCode) {
                    RequestCodeData.PROFILE_EDIT -> { // 프로필 수정
                        profileViewModel.loadUserData(context = this, authorization = spHelper.authorization, userId = userId, loadingBar = null)
                    }
                    RequestCodeData.USED_GOODS, // 사용한 제품 더보기
                    RequestCodeData.FOLLOW, // 팔로우, 팔로잉
                    RequestCodeData.POST_DETAIL, // 포스트 상세
                    RequestCodeData.TAG_POST, // 포스트 태그
                    RequestCodeData.SAVED_POST, // 저장한 포스트
                    RequestCodeData.SETTING, // 설정
                    RequestCodeData.TOPIC_DETAIL, // 토픽 상세
                    RequestCodeData.POST_COMMENT, // 댓글
                    RequestCodeData.GOODS_DETAIL, // 상품 상세
                    RequestCodeData.MY_SHOP -> {
                        profileViewModel.loadUserData(context = this, authorization = spHelper.authorization, userId = userId, loadingBar = null)
                        profileViewModel.loadUserTopicData(userId = userId, page = 1, topicDataCount = 0, loginUserId = spHelper.userId, loadingBar = null)
                        profileAlbumFragment.reloadPetUsedGoodsData(loadingBar = null)
                        profilePetFragment.reloadPetUsedGoodsData(loadingBar = null)
                    }
                    RequestCodeData.TOPIC_ADD -> { // 펫 추가
                        profileViewModel.loadUserTopicData(userId = userId, page = 1, topicDataCount = 0, loginUserId = spHelper.userId, loadingBar = null)
                    }
                    RequestCodeData.POST_UPLOAD -> { // 포스트 업로드
                        // set user post data
                        profileAlbumFragment.reloadPetUsedGoodsData(loadingBar = null)
                    }
                }
            }
            ResultCodeData.TOPIC_DELETE -> {
//                if (this.resultCode == Activity.RESULT_CANCELED)
//                    this.resultCode = Activity.RESULT_OK
                profileViewModel.loadUserTopicData(userId = userId, page = 1, topicDataCount = 0, loginUserId = spHelper.userId, loadingBar = null)
            }
            ResultCodeData.LOGIN_SUCCESS -> { // login -> reload all data
                this.resultCode = ResultCodeData.LOGIN_SUCCESS
                profileViewModel.setProfileType(profileUserId = userId, loginUserId = spHelper.userId, loadingBar = null)
                profileViewModel.loadUserData(context = this, authorization = spHelper.authorization, userId = userId, loadingBar = null)
                profileViewModel.loadUserTopicData(userId = userId, page = 1, topicDataCount = 0, loginUserId = spHelper.userId, loadingBar = null)
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                setResult(ResultCodeData.LOGOUT_SUCCESS)
                finish()
            }
        }
    }

    private fun onSetup() {
        userId = intent.getStringExtra(IntentPassName.USER_ID)

        // appBarLayout 애니메이션 없애기
        appBarLayout.stateListAnimator = null

        // set tool bar
        setSupportActionBar(topicToolbar)

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        postUploadTypeSelectDialog = DialogCreator.postUploadTypeSelectDialog(context = this)

        // 프래그먼트 초기화
        profileAlbumFragment = ProfileAlbumFragment.newFragment(userId = userId)
        profilePetFragment = ProfilePetFragment.newFragment(userId = userId)

        viewPagerAdapter = ViewStatePagerAdapter(manager = supportFragmentManager)
        viewPagerAdapter.addFragment(fragment = profileAlbumFragment, title = getString(R.string.profile_tab_album))
        viewPagerAdapter.addFragment(fragment = profilePetFragment, title = getString(R.string.profile_tab_pet))
        viewPager.adapter = viewPagerAdapter

        // tab layout 초기화
        tabLayoutHelper = TabLayoutHelper(context = this,
                defaultSelectFont = fontColorSpan.notoBold000000,
                defaultUnSelectFont = fontColorSpan.notoMediumBFBFBF,
                useDefaultEvent = true,
                listener = null)
        tabLayoutHelper.handle(layout = tabLayout, viewPager = viewPager)
        tabLayoutHelper.addTab(tabName = getString(R.string.profile_tab_album), tabLayout = null, fontSize = 18f, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.addTab(tabName = getString(R.string.profile_tab_pet), tabLayout = null, fontSize = 18f, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.changeSelectedTab(selectIndex = 0)

        // topic recycler view & adapter 초기화
        profileTopicAdapter = ProfileTopicAdapter(context = this,
                selectListener = topicSelectListener,
                addListener = topicAddListener)
        topicRecyclerView.adapter = profileTopicAdapter
        topicRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        topicRecyclerView.addItemDecoration(CustomItemDecoration(context = this, right = 4f))

        profileViewModel.setProfileType(profileUserId = userId, loginUserId = spHelper.userId)
        profileViewModel.loadUserData(context = this, authorization = spHelper.authorization, userId = userId)
        profileViewModel.loadUserTopicData(userId = userId, page = 1, topicDataCount = 0, loginUserId = spHelper.userId)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionPostUploadBtn.setOnClickListener(this) // 포스트 업로드
        actionMyShopBtn.setOnClickListener(this) // 마이 쇼핑
        actionSavedPostBtn.setOnClickListener(this) // 저장한 포스트
        actionSettingBtn.setOnClickListener(this) // 설정 화면 버튼
        actionMoreBtn.setOnClickListener(this) // 더보기 (신고, 공유)
        profileEditBtn.setOnClickListener(this) // 프로필 수정
        followBtn.setOnClickListener(this) // 팔로우 버튼
        followerLayout.setOnClickListener(this) // 팔로워
        followingLayout.setOnClickListener(this) // 팔로잉

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = topicRecyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = profileTopicAdapter.dataLoading, total = profileTopicAdapter.dataTotal) { _ ->
                profileViewModel.loadUserTopicData(userId = userId, page = profileTopicAdapter.dataNextPage, topicDataCount = profileTopicAdapter.itemCount, loginUserId = spHelper.userId)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        profileViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.profileType?.let {
                    when(it) {
                        AppConstants.MY_PROFILE -> {
                            myProfileActionLayout.visibility = View.VISIBLE
                            otherProfileActionLayout.visibility = View.GONE

                            followBtn.visibility = View.GONE
                            profileEditBtn.visibility = View.VISIBLE
                        }
                        AppConstants.OTHER_PROFILE -> {
                            myProfileActionLayout.visibility = View.GONE
                            otherProfileActionLayout.visibility = View.VISIBLE

                            followBtn.visibility = View.VISIBLE
                            profileEditBtn.visibility = View.GONE
                        }
                    }
                }
                uiData.topicData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        profileTopicAdapter.updateData(profileTopicData = it)
                    }
                    else
                        profileTopicAdapter.addData(profileTopicData = it)
                }
                uiData.profileImage?.let {
                    Glide.with(this)
                            .load(it)
                            .apply(profileGlideOption)
                            .into(imageViewProfile)
                }
                uiData.nickName?.let {
                    textViewNickName.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.introduction?.let {
                    textViewIntroduction.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.followState?.let {
                    followBtn.isSelected = it
                }
                uiData.followerCnt?.let {
                    textViewFollowerNum.setText(SupportData.countFormat(it), TextView.BufferType.SPANNABLE)
                }
                uiData.followingCnt?.let {
                    textViewFollowingNum.setText(SupportData.countFormat(it), TextView.BufferType.SPANNABLE)
                }
            }
        })

        // load user data 이벤트 observe
        profileViewModel.loadUserDataEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadUserDataEvent ->
            loadUserDataEvent?.let { _ ->
                loadUserDataEvent.status?.let { isSuccess ->
                    if (!isSuccess)
                        actionBackBtn.performClick()
                }
            }
        })

        // load user topic 이벤트 observe
        profileViewModel.loadUserPetEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadUserPetEvent ->
            loadUserPetEvent?.let { _ ->
                loadUserPetEvent.status?.let { isLoading ->
                    profileTopicAdapter.dataLoading = isLoading
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    setResult(resultCode)
                    finish()
                }
                R.id.actionPostUploadBtn -> { // 포스트 업로드
                    // 권한 체크
                    if (!permissionHelper.hasPermission(context = this, permissions = permissionHelper.cameraPermissions)) {
                        permissionHelper.requestCameraPermissions(context = this)
                        return
                    }

                    postUploadTypeSelectDialog.show(supportFragmentManager, "postUploadTypeSelectDialog")
                }
                R.id.actionMyShopBtn -> { // 마이쇼핑
                    RunActivity.myShoppingActivity(context = this)
                }
                R.id.actionSavedPostBtn -> { // 저장한 포스트
                    RunActivity.savedPostActivity(context = this)
                }
                R.id.actionSettingBtn -> { // 설정 화면 버튼
                    RunActivity.settingActivity(context = this)
                }
                R.id.actionMoreBtn -> { // 더보기 (신고, 공유

                }
                R.id.followBtn -> { // 팔로우 버튼
                    profileViewModel.userFollow(authorization = spHelper.authorization, userId = userId)
                }
                R.id.followerLayout -> { // 팔로워
                    RunActivity.followerActivity(context = this, userId = userId)
                }
                R.id.followingLayout -> { // 팔로잉
                    RunActivity.followingActivity(context = this, userId = userId)
                }
                R.id.profileEditBtn -> { // 프로필 수정
                    RunActivity.profileEditActivity(context = this)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, userId: String): Intent {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(IntentPassName.USER_ID, userId)

            return intent
        }
    }
}
