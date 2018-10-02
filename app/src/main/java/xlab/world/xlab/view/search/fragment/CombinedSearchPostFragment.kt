package xlab.world.xlab.view.search.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_combined_search.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.PostThumbnailAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.search.SearchViewModel

class CombinedSearchPostFragment: Fragment() {
    private val searchViewModel: SearchViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var searchPostAdapter: PostThumbnailAdapter? = null

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
        // Toast, Dialog 초기화
        defaultToast = defaultToast ?: DefaultToast(context = context!!)
        progressDialog = progressDialog ?: DefaultProgressDialog(context = context!!)

        defaultListener = defaultListener ?: DefaultListener(context = context as Activity)

        // search post adapter & recycler view 초기화
        searchPostAdapter = searchPostAdapter ?: PostThumbnailAdapter(context = context!!,
                changeViewTypeListener = null,
                postListener = defaultListener!!.postListener)
        recyclerView.adapter = searchPostAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        if (recyclerView.itemDecorationCount < 1)
            recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, offset = 0.5f))
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
        fun newFragment(): CombinedSearchPostFragment {
            val fragment = CombinedSearchPostFragment()

            val args = Bundle()
            args.putBoolean("needInitData", true)
            args.putInt("noSearchDataVisibility", View.GONE)
            fragment.arguments = args

            return fragment
        }
    }
}