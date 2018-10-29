package xlab.world.xlab.view.notification.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_notification.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.SocialNotificationAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.notification.NotificationActivity
import xlab.world.xlab.view.notification.NotificationViewModel

class SocialNotificationFragment: Fragment() {
    private val notificationViewModel: NotificationViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var defaultListener: DefaultListener? = null

    private var socialNotificationAdapter: SocialNotificationAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
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

        // listener 초기화
        defaultListener = defaultListener ?: DefaultListener(context = context as Activity)

        // notification adapter & recycler 초기화
        socialNotificationAdapter = socialNotificationAdapter?: SocialNotificationAdapter(context = context!!,
                profileListener = defaultListener!!.profileListener,
                postListener = defaultListener!!.postListener,
                commentListener = defaultListener!!.commentsFromPostListener)
        recyclerView.adapter = socialNotificationAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        if (recyclerView.itemDecorationCount < 1)
            recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, bottom = 4f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        if (needInitData)
            reloadNotification(loadingBar = true)
        else
            setLayoutVisibility()
    }

    private fun onBindEvent() {
        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = socialNotificationAdapter!!.dataLoading, total = socialNotificationAdapter!!.dataTotal) {_->
                notificationViewModel.loadSocialNotificationData(authorization = spHelper.authorization, page = socialNotificationAdapter!!.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        notificationViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.socialNotificationData?.let {
                    socialNotificationAdapter?.linkData(socialNotificationData = it)
                }
                uiData.socialNotificationDataUpdate?.let {
                    socialNotificationAdapter?.notifyDataSetChanged()
                }
                uiData.existNewSocialNotification?.let {
                    (context as NotificationActivity).tabDataUpdate(index = 0, extraData = it)
                }
                uiData.noNotificationVisibility?.let {
                    setBundleVisibilityData(noNotificationLayout = it)
                }
            }
        })

        // load notification 이벤트 observe
        notificationViewModel.loadNotificationEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isLoading ->
                socialNotificationAdapter?.dataLoading = isLoading
                needInitData = false
            }
        })
    }

    fun reloadNotification(loadingBar: Boolean?) {
        context?.let {
            notificationViewModel.loadSocialNotificationData(authorization = spHelper.authorization, page = 1, loadingBar = loadingBar)
        } ?: let {
            needInitData = true
        }
    }

    private fun setLayoutVisibility() {
        noNotificationLayout?.visibility = getBundleNoNotificationVisibility()
    }

    private fun getBundleNoNotificationVisibility(): Int = arguments?.getInt("noNotificationLayout") ?: View.INVISIBLE

    private fun setBundleVisibilityData(noNotificationLayout: Int) {
        arguments?.putInt("noNotificationLayout", noNotificationLayout)

        setLayoutVisibility()
    }

    companion object {
        fun newFragment(): SocialNotificationFragment {
            val fragment =  SocialNotificationFragment()

            val args = Bundle()
            args.putBoolean("needInitData", true)
            args.putInt("noNotificationLayout", View.INVISIBLE)
            fragment.arguments = args

            return fragment
        }
    }
}