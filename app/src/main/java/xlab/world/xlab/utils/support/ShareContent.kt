package xlab.world.xlab.utils.support

import android.net.Uri
import com.facebook.share.model.ShareLinkContent
import com.kakao.message.template.*

object ShareContent {
    fun kakaoProfileShareBuild(title: String, description: String,
                               profileImage: String, userId: String): FeedTemplate {
        val contentObject = ContentObject
                .newBuilder(title, profileImage, LinkObject.newBuilder().setAndroidExecutionParams("type=${AppConstants.LINK_PROFILE}&code=$userId").build())
                .setDescrption(description).build()

        val buttonObject = ButtonObject("앱에서 보기", LinkObject.newBuilder()
                .setAndroidExecutionParams("type=${AppConstants.LINK_PROFILE}&code=$userId")
                .build())

        return FeedTemplate
                .newBuilder(contentObject)
                .addButton(buttonObject).build()
    }

    fun kakaoGoodsShareBuild(title: String, description: String, price: Int,
                             goodsImage: String, goodsCode: String): CommerceTemplate {
        val contentObject = ContentObject
                .newBuilder(title, goodsImage, LinkObject.newBuilder().setAndroidExecutionParams("type=${AppConstants.LINK_GOODS}&code=$goodsCode").build())
                .setDescrption(description).build()

        val commerceDetailObject = CommerceDetailObject.newBuilder(price).build()

        val buttonObject = ButtonObject("앱에서 보기", LinkObject.newBuilder()
                .setAndroidExecutionParams("type=${AppConstants.LINK_GOODS}&code=$goodsCode")
                .build())

        return CommerceTemplate
                .newBuilder(contentObject, commerceDetailObject)
                .addButton(buttonObject)
                .build()
    }

    fun kakaoPostShareBuild(title: String, description: String,
                            likeCount: Int, commentCount: Int,
                            postImage: String, postId: String): FeedTemplate {
        val contentObject = ContentObject
                .newBuilder(title, postImage, LinkObject.newBuilder().setAndroidExecutionParams("type=${AppConstants.LINK_POST}&code=$postId").build())
                .setDescrption(description).build()

        val socialObject = SocialObject
                .newBuilder()
                .setLikeCount(likeCount)
                .setCommentCount(commentCount)
                .build()

        val buttonObject = ButtonObject("앱에서 보기", LinkObject.newBuilder()
                .setAndroidExecutionParams("type=${AppConstants.LINK_POST}&code=$postId")
                .build())

        return FeedTemplate
                .newBuilder(contentObject)
                .setSocial(socialObject)
                .addButton(buttonObject).build()
    }

    fun facebookShareBuild(title: String, description: String,
                           profileImage: String, userId: String) {
    }

    fun facebookProfileShareBuild(title: String, description: String,
                                  profileImage: String, userId: String): ShareLinkContent {

        val appLink = "https://play.google.com/store/apps/details?id=xlab.world.xlab?type=${AppConstants.LINK_PROFILE}&code=$userId"

        val content = ShareLinkContent.Builder()
                .setQuote(title + "\n" + description)
                .setImageUrl(Uri.parse(profileImage))
                .setContentUrl(Uri.parse(appLink))
                .build()

        return content
    }
}