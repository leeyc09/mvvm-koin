package xlab.world.xlab.data.adapter

import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class SocialNotificationListData(val type: Int,
                                      val userId: String,
                                      val userProfileUrl: String,
                                      val userNick: String,
                                      val postId: String = "",
                                      val postType: Int = AppConstants.POSTS_IMAGE,
                                      val postImageUrl: String = "",
                                      val youTubeVideoID: String = "",
                                      val commentContent: String = "",
                                      val year: Int,
                                      val month: Int,
                                      val day: Int,
                                      val hour: Int,
                                      val minute: Int): Serializable
