package xlab.world.xlab.utils.view.hashTag

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import xlab.world.xlab.R
import java.io.Serializable

class EditTextTagHelper (private val context: Context,
                         private val orientation: Int,
                         private val tagClickListener: View.OnClickListener? = null,
                         private val tagDeleteClickListener: View.OnClickListener? = null,
                         private val editorActionListener: TextView.OnEditorActionListener? = null,
                         private val keyListener: View.OnKeyListener? = null,
                         private val tagChangeCallBack: TagChangeCallBack? = null) {

    private var frameLayout: FrameLayout? = null
    private lateinit var viewGroup: ViewGroup
    private lateinit var editText: EditText
    private val tagList: ArrayList<View> = ArrayList()
    private val textTagLayout: Int = R.layout.edit_text_tag
    private val textInputLayout: Int = R.layout.edit_text_tag_input

    val statusRemove = 0
    val statusAdd = 1

    interface TagChangeCallBack {
        fun onTagChanged(status: Int)
    }

    fun handle(layout: FrameLayout) {
        if (frameLayout == null) {
            frameLayout = layout
            // setup view
            if (orientation == LinearLayout.HORIZONTAL) {
                viewGroup = LinearLayout(context)
                viewGroup.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                (viewGroup as LinearLayout).orientation = orientation
            } else if (orientation == LinearLayout.VERTICAL) {
                viewGroup = FlowLayout(context)
                viewGroup.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            }
            frameLayout!!.addView(viewGroup)

            addInputTagView()
        } else {
            throw RuntimeException("FrameLayout is not null. You need to create a unique EditTextTagHelper for every FrameLayout")
        }
    }

    private fun addInputTagView() {
        editText = createInputTag(viewGroup)
        editText.tag = Object()
        setupListener()
        viewGroup.addView(editText)
    }

    private fun setupListener() {
        editorActionListener?.let { listener ->
            editText.setOnEditorActionListener(listener)
        }
        keyListener?.let { listener ->
            editText.setOnKeyListener(listener)
        }
    }

    // create text input field
    private fun createInputTag(parent: ViewGroup): EditText {
        editText = LayoutInflater.from(context).inflate(textInputLayout, parent, false) as EditText
        return editText
    }

    // create text tag field
    private fun createTag(tag: SearchData): Boolean {
        if (tag.text.isEmpty()) {
            // do nothing, or you can tip "can't add empty tag"
            return false
        }

        val tagView = LayoutInflater.from(context).inflate(textTagLayout, viewGroup, false)
        tagView.tag = tag
        val textView = tagView.findViewById<TextView>(R.id.textView)
        textView.text = tag.text
        val deleteView = tagView.findViewById<ImageView>(R.id.deleteImage)
        deleteView.tag = tagView
        // set click listener
        tagClickListener?.let { listener ->
            tagView.setOnClickListener(listener)
        }
        tagDeleteClickListener?.let { listener ->
            deleteView.setOnClickListener(listener)
        }

        viewGroup.addView(tagView, viewGroup.childCount - 1)
        tagList.add(tagView)
        // reset action status
        editText.setText("")
        editText.hint = ""
        editText.performClick()

        return true
    }

    // remove text tag field
    fun removeTag(removeTagView: View) {
        tagList.remove(removeTagView)
        viewGroup.removeView(removeTagView)
        if (tagList.isEmpty())
            editText.hint = context.getString(R.string.search_item)
        tagChangeCallBack?.onTagChanged(statusRemove)
    }

    // remove last tag
    fun removeLastTag(): Boolean {
        val tagContent = editText.text.toString()
        val editTextSelectionPos = editText.selectionEnd
        // if anything input in edit text field or position of edit text selection is first
        if (tagContent.isEmpty() || editTextSelectionPos == 0) {
            if (viewGroup.childCount > 1) { // exist tag
                removeTag(viewGroup.getChildAt(viewGroup.childCount - 2)) // remove last tag
                return true
            }
        }
        return false
    }

    // add only text tag
    fun addTag(): Boolean {
        val tagContent = editText.text.toString()
        return mAddTag(tagContent)
    }

    // add text tag with code
    fun addTag(text: String, code: String = ""): Boolean {
        if (code.isEmpty())
            return mAddTag(text)

        val result = createTag(SearchData(text = text, code = code))
        if (result)
            tagChangeCallBack?.onTagChanged(statusAdd)

        return result
    }

    private fun mAddTag(text: String): Boolean {
        val spaceRegex = Regex("""\s+""")
        var result = false
        text.split(spaceRegex).forEach {
            if (it.trim() != "") {
                result = createTag(SearchData(text = it.trim(), code = ""))
            }
        }
        if (result)
            tagChangeCallBack?.onTagChanged(statusAdd)

        return result
    }

    fun getTagList(): ArrayList<SearchData> {
        val tagValueList: ArrayList<SearchData> = ArrayList()
        tagList.forEach { view ->
            tagValueList.add(view.tag as SearchData)
        }
        return tagValueList
    }

    fun requestInputFieldFocus() {
        editText.requestFocus()
    }

    data class SearchData(val text: String, val code: String): Serializable
}