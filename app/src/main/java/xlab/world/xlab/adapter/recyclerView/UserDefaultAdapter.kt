package xlab.world.xlab.adapter.recyclerView

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.TopicSettingListData
import xlab.world.xlab.data.adapter.UserDefaultData
import xlab.world.xlab.data.adapter.UserDefaultListData
import xlab.world.xlab.utils.support.SPHelper

class UserDefaultAdapter(private val context: Context,
                         private val followListener: View.OnClickListener,
                         private val profileListener: View.OnClickListener) : RecyclerView.Adapter<UserDefaultAdapter.ViewHolder>() {

    private val spHelper: SPHelper by (context as Activity).inject()

    private val userDefaultData: UserDefaultData = UserDefaultData()
    var dataLoading: Boolean
        get() = this.userDefaultData.isLoading
        set(value) { this.userDefaultData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.userDefaultData.total
    var dataNextPage: Int = 1
        get() = this.userDefaultData.nextPage

    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(R.drawable.profile_img_44)
            .error(R.drawable.profile_img_44)

    fun getItem(position: Int): UserDefaultListData {
        return userDefaultData.items[position]
    }

    fun updateData(userDefaultData: UserDefaultData) {
        this.userDefaultData.items.clear()
        this.userDefaultData.items.addAll(userDefaultData.items)

        this.userDefaultData.isLoading = false
        this.userDefaultData.total = userDefaultData.total
        this.userDefaultData.nextPage = 2

        notifyDataSetChanged()
    }

    fun addData(userDefaultData: UserDefaultData) {
        val size: Int = itemCount
        this.userDefaultData.items.addAll(userDefaultData.items)

        this.userDefaultData.isLoading = false
        this.userDefaultData.nextPage += 1

        notifyItemRangeChanged(size, itemCount)
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_user_default, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = userDefaultData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return userDefaultData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        private val profileBtn: ConstraintLayout = view.findViewById(R.id.profileBtn)
        private val imageViewProfile: ImageView = view.findViewById(R.id.imageViewProfile)
        private val textViewNick: TextView = view.findViewById(R.id.textViewNick)
        private val textViewTopic: TextView = view.findViewById(R.id.textViewTopic)
        private val textViewCenterNick: TextView = view.findViewById(R.id.textViewCenterNick)
        private val followBtn: ImageView = view.findViewById(R.id.followBtn)

        override fun display(item: UserDefaultListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, offSetDIP, 0, 0)

            // user profile 이미지
            Glide.with(context)
                    .load(item.profileImage)
                    .apply(glideOption)
                    .into(imageViewProfile)

            // topic 없는 유저 -> 닉네임만 노출
            if (item.topic.isEmpty()) {
                textViewNick.visibility = View.GONE
                textViewTopic.visibility = View.GONE
                textViewCenterNick.visibility = View.VISIBLE

                textViewCenterNick.setText(item.nickName, TextView.BufferType.SPANNABLE)
            } else {
                textViewNick.visibility = View.VISIBLE
                textViewTopic.visibility = View.VISIBLE
                textViewCenterNick.visibility = View.GONE

                textViewNick.setText(item.nickName, TextView.BufferType.SPANNABLE)

                val topicMap = HashMap<String, Int>()
                // topic (pet) 종에 따른 숫자 카운
                item.topic.forEach { topic ->
                    val value = topicMap[topic]
                    topicMap[topic] =
                            if (value == null) 1
                            else value + 1
                }
                // topic 갯수 적은 순서대로 sort
                val sortedTopicMap = topicMap.toList().sortedBy { (_, value) -> value }.reversed().toMap()
                var topicStr = ""
                var count = 1
                sortedTopicMap.forEach { topic ->
                    topicStr += topic.key
                    if (topic.value > 1) // 토픽 갯수 1보다 크면 뒤에 숫자 표시
                        topicStr += " ${topic.value}"
                    if (count < sortedTopicMap.size)
                        topicStr += " ・ "
                    count++
                }
                textViewTopic.setText(topicStr, TextView.BufferType.SPANNABLE)
            }

            // follow 상태
            followBtn.isSelected = item.isFollowing
            // 자신 프로필이면 follow 안보이게
            followBtn.visibility =
                    if (item.userId == spHelper.userId) View.GONE
                    else View.VISIBLE

            // profile touch 이벤트
            profileBtn.tag = item.userId
            profileBtn.setOnClickListener(profileListener)

            // follow touch 이벤트
            followBtn.tag = position
            followBtn.setOnClickListener(followListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: UserDefaultListData, position: Int)
    }
}
