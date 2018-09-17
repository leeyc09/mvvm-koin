package xlab.world.xlab.data.adapter

import java.io.Serializable

data class UserDefaultListData(val userId: String,
                               val profileImage: String,
                               val nickName: String,
                               val topic: ArrayList<String>,
                               var isFollowing: Boolean): Serializable
