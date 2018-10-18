package xlab.world.xlab.utils.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_select_list_bottom.*
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.ListSelectDialogAdapter
import xlab.world.xlab.utils.support.PrintLog

class ListSelectBottomDialog: BottomSheetDialogFragment(), View.OnClickListener {

    interface Listener {
        fun onListSelect(text: String)
    }

    lateinit var listener: Listener

    val data = ArrayList<String>()

    private var listData
        get() = arguments?.getStringArrayList("listData")?: ArrayList<String>()
        set(value) {
            arguments?.putStringArrayList("listData", value)
        }

    lateinit var adapter: ListSelectDialogAdapter

    private val selectListener = View.OnClickListener { view ->
        listener.onListSelect(text = view.tag as String)
        dismiss()
    }

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_select_list_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()
    }

    private fun onSetup() {
        adapter = ListSelectDialogAdapter(context = context!!, selectListener = selectListener)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)

        adapter.linkData(listData = listData)
        adapter.notifyDataSetChanged()
    }

    private fun onBindEvent() {
        cancelBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.cancelBtn -> {
                    dismiss()
                }
            }
        }
    }

    fun handle(listener: Listener) {
        this.listener = listener
    }

    fun changeListData(listData: ArrayList<String>) {
        context?.let {
            adapter.linkData(listData = listData)
            adapter.notifyDataSetChanged()
        } ?: let {
            this.listData = listData
        }
    }

    companion object {
        fun newDialog(): ListSelectBottomDialog {
            val dialog = ListSelectBottomDialog()

            val args = Bundle()
            args.putStringArrayList("listData", ArrayList<String>())
            dialog.arguments = args

            return dialog
        }
    }
}