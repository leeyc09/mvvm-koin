package xlab.world.xlab.data.adapter

import java.io.Serializable

data class NoticeListData(var isRead: Boolean,
                          var isSelect: Boolean = false,
                          val noticeId: String,
                          val title: String,
                          val content: String,
                          val date: String): Serializable
