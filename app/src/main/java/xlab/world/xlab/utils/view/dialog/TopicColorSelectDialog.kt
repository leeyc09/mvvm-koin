package xlab.world.xlab.utils.view.dialog

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_select_topic_color.*
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.TopicColorAdapter
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration

class TopicColorSelectDialog: BottomSheetDialogFragment(), View.OnClickListener {

    interface Listener {
        fun onTopicColorSelect(selectColorStr: String)
    }

    private lateinit var listener: Listener

    private var selectPosition
        get() = arguments?.getInt("selectPosition") ?: 0
        set(value) {
            arguments?.putInt("selectPosition", value)
        }

    private lateinit var topicColorAdapter: TopicColorAdapter

    private val topicColorListener = View.OnClickListener { view ->
        val newSelectPosition = view.tag as Int
        if (selectPosition != newSelectPosition) {
            topicColorAdapter.getItem(position = selectPosition).isSelect = false
            topicColorAdapter.getItem(position = newSelectPosition).isSelect = true

            topicColorAdapter.notifyItemChanged(selectPosition)
            topicColorAdapter.notifyItemChanged(newSelectPosition)

            selectPosition = newSelectPosition
        }
    }

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_select_topic_color, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isCancelable = false

        onSetup()

        onBindEvent()
    }

    private fun onSetup() {
        PrintLog.d("selectPosition", selectPosition.toString(), "TopicColorSelect")
        // topic color recycler view & adapter 초기화
        topicColorAdapter = TopicColorAdapter(context = context!!, selectIndex = selectPosition, selectListener = topicColorListener)
        recyclerView.adapter = topicColorAdapter
        recyclerView.layoutManager = GridLayoutManager(context!!, 2, GridLayoutManager.HORIZONTAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, right = 30f, bottom = 30f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun onBindEvent() {
        finishBtn.setOnClickListener(this) // 색상 선택
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.finishBtn -> { // 색상 선택
                    listener.onTopicColorSelect(selectColorStr = topicColorAdapter.getItem(position = selectPosition).colorStr)
                    dismiss()
                }
            }
        }
    }

    fun handle(listener: Listener) {
        this.listener = listener
    }

    companion object {
        fun newDialog(selectPosition: Int): TopicColorSelectDialog {
            val dialog = TopicColorSelectDialog()

            val args = Bundle()
            args.putInt("selectPosition", selectPosition)
            dialog.arguments = args

            return dialog
        }
    }
}
