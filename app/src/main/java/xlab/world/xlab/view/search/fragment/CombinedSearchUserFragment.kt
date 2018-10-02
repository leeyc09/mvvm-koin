package xlab.world.xlab.view.search.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_combined_search.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.button.ScrollUpButtonHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.search.SearchViewModel

class CombinedSearchUserFragment: Fragment() {
    private val searchViewModel: SearchViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var scrollUpButtonHelper: ScrollUpButtonHelper? = null

    private var defaultListener: DefaultListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_combined_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        matchBtnLayout.visibility = View.GONE
        // Toast, Dialog 초기화
        defaultToast = defaultToast ?: DefaultToast(context = context!!)
        progressDialog = progressDialog ?: DefaultProgressDialog(context = context!!)

        defaultListener = defaultListener ?: DefaultListener(context = context as Activity)

        // scroll up button 초기화
        scrollUpButtonHelper?.let {
            scrollUpButtonHelper = it
        }?: let {
            scrollUpButtonHelper = ScrollUpButtonHelper(
                    smoothScroll = true,
                    scrollUpBtn = scrollUpBtn)
            scrollUpButtonHelper?.handle(recyclerView)
        }
    }

    private fun onBindEvent() {
    }

    private fun observeViewModel() {
    }

    fun reloadPostsData(loadingBar: Boolean?) {
        context?.let {
        } ?:let { needInitData = true }
    }

    private fun setLayoutVisibility() {
        textViewNoSearchData?.visibility = getBundleNoSearchDataVisibility()
    }

    private fun getBundleNoSearchDataVisibility(): Int = arguments?.getInt("noSearchDataVisibility") ?: View.GONE

    private fun setBundleVisibilityData(noSearchDataVisibility: Int) {
        arguments?.putInt("noSearchDataVisibility", noSearchDataVisibility)

        setLayoutVisibility()
    }

    companion object {
        fun newFragment(): CombinedSearchUserFragment {
            val fragment = CombinedSearchUserFragment()

            val args = Bundle()
            args.putBoolean("needInitData", true)
            args.putInt("noSearchDataVisibility", View.GONE)
            fragment.arguments = args

            return fragment
        }
    }
}