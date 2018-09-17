package xlab.world.xlab.data.adapter

import java.io.Serializable

data class UserRecommendListData(val userId: String,
                                 val profileImage: String,
                                 val nickName: String,
                                 var isFollowing: Boolean): Serializable
