package xlab.world.xlab.utils.support

object AppConstants {
    const val LOCAL_HTML_URL = "file:///android_asset/html/"

    // permission request code
    const val PERMISSION_REQUEST_CAMERA_CODE = 1
    const val PERMISSION_REQUEST_GALLERY_CODE = 2

    // user login type
    const val LOCAL_LOGIN = 0
    const val FACEBOOK_LOGIN = 1
    const val KAKAO_LOGIN = 2

    // user level
    const val NORMAL_USER_LEVEL = 1
    const val ADMIN_USER_LEVEL = 99

    // adapter type
    const val ADAPTER_HEADER = 0
    const val ADAPTER_CONTENT = 1
    const val ADAPTER_FOOTER = 2

    // feed type
    const val FEED_GOODS = 0
    const val FEED_POST = 1
    const val FEED_AD = 2

    // post type
    const val POSTS_IMAGE = 0
    const val POSTS_VIDEO = 1
    const val POSTS_YOUTUBE_LINK = 2

    // tag sign
    const val HASH_TAG_SIGN = '#'
    const val USER_TAG_SIGN = '@'

    // gallery adapter type
    const val GALLERY_ONE = 0
    const val GALLERY_MANY = 1

    // goods sort type
    const val SORT_MATCH = 1
    const val SORT_DOWN_PRICE = 2
    const val SORT_UP_PRICE = 3

    // profile type
    const val MY_PROFILE = 0
    const val OTHER_PROFILE = 1

    // used goods topic type
    const val USED_GOODS_PET = 1
}