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
import xlab.world.xlab.adapter.recyclerView.SearchGoodsAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.button.MatchButtonHelper
import xlab.world.xlab.utils.view.button.ScrollUpButtonHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.search.SearchViewModel

class CombinedSearchGoodsFragment: Fragment() {
    private val searchViewModel: SearchViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }
    private var matchVisibility
        get() = arguments?.getInt("matchVisibility") ?: View.VISIBLE
        set(value) {
            arguments?.putInt("matchVisibility", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var searchGoodsAdapter: SearchGoodsAdapter? = null

    private var matchButtonHelper: MatchButtonHelper? = null
    private var scrollUpButtonHelper: ScrollUpButtonHelper? = null

    private var defaultListener: DefaultListener? = null

    private val matchButtonListener = object: MatchButtonHelper.Listener {
        override fun matchVisibility(visibility: Int) {
            searchGoodsAdapter?.changeMatchVisible(visibility)
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_combined_search, container, false)
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

        defaultListener = defaultListener ?: DefaultListener(context = context as Activity)

        // match button 초기화
        matchButtonHelper = matchButtonHelper ?: MatchButtonHelper(
                context = context as Activity,
                rootView = matchBtnLayout,
                isMatchShow = true,
                listener = matchButtonListener)

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
        fun newFragment(): CombinedSearchGoodsFragment {
            val fragment = CombinedSearchGoodsFragment()

            val args = Bundle()
            args.putBoolean("needInitData", true)
            args.putInt("noSearchDataVisibility", View.GONE)
            args.putInt("matchVisibility", View.VISIBLE)
            fragment.arguments = args

            return fragment
        }
    }
}