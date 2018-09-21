package xlab.world.xlab.view.topicDetail.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_image_pager.*
import org.koin.android.architecture.ext.viewModel
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.TopicUsedGoodsData
import xlab.world.xlab.data.response.ResUserPetData
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.topicDetail.TopicPetDetailViewModel

class TopicPetDetailFragment: Fragment() {
    private val topicPetDetailViewModel: TopicPetDetailViewModel by viewModel()

    var petData: ResUserPetData?
        get() = arguments?.getSerializable("petData")?.let{it as ResUserPetData}
        set(value) {
            arguments?.putSerializable("petData", value)
        }
    var petUsedGoods: TopicUsedGoodsData?
        get() = arguments?.getSerializable("petUsedGoods")?.let{it as TopicUsedGoodsData}
        set(value) {
            arguments?.putSerializable("petUsedGoods", value)
        }
    private var petNo: Int
        get() = arguments?.getInt("petNo") ?: 1
        set(value) {
            arguments?.putInt("petNo", value)
        }
    private var reload: Boolean
        get() = arguments?.getBoolean("reload") ?: false
        set(value) {
            arguments?.putBoolean("reload", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var glideOption = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.pet_profile_image_default)
            .error(R.drawable.pet_profile_image_default)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        // Toast, Dialog 초기화
        defaultToast = defaultToast ?: DefaultToast(context = context!!)
        progressDialog = progressDialog ?: DefaultProgressDialog(context = context!!)

        topicPetDetailViewModel.loadPetDetailData(userId = getBundleUserId(), petNo = petNo, reLoad = reload)
    }

    private fun onBindEvent() {

    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        topicPetDetailViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog!!.isShowing)
                        progressDialog!!.show()
                    else if (!it && progressDialog!!.isShowing)
                        progressDialog!!.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast?.showToast(message = it)
                }
                uiData.petImage?.let {
                    Glide.with(context!!)
                            .load(it)
                            .apply(glideOption)
                            .into(imageView)
                }
            }
        })

        // load pet data 이벤트 observe
        topicPetDetailViewModel.loadPetDataEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadPetDataEvent ->
            loadPetDataEvent?.let { _ ->
                loadPetDataEvent.petData?.let {
                    petData = it
                }
                loadPetDataEvent.petUsedGoods?.let {
                    petUsedGoods = it
                }
            }
        })
    }

    fun resetPetData(petNo: Int?) {
        petNo?.let { this.petNo = petNo }
        reload = true
        context?.let {
            topicPetDetailViewModel.loadPetDetailData(userId = getBundleUserId(), petNo = this.petNo, reLoad = reload)
        }
    }

    private fun getBundleUserId(): String = arguments?.getString("userId") ?: ""

    companion object {
        fun newFragment(userId: String, petNo: Int): TopicPetDetailFragment {
            val fragment =  TopicPetDetailFragment()

            val args = Bundle()
            args.putString("userId", userId)
            args.putInt("petNo", petNo)
            args.putBoolean("reload", false)
            fragment.arguments = args

            return fragment
        }
    }
}