package xlab.world.xlab.utils.support

object AppConstants {
    const val LOCAL_HTML_URL = "file:///android_asset/html/"
    const val TRACKING_DELIVER_URL = "https://www.doortodoor.co.kr/parcel/doortodoor.do?fsp_action=PARC_ACT_002&fsp_cmd=retrieveInvNoACT&invc_no="

    // kakao plus chat
    const val KAKO_PLUS_CHAT = "http://plus.kakao.com/talk/bot/"

    // cs company delivery code
    const val CS_XLAB = "7"
    const val CS_HOLAPET = "2"

    // buy goods from intent type
    const val FROM_GOODS_DETAIL = 0
    const val FROM_CART = 1

    // used goods from type
    const val FROM_BUY    = 1
    const val FROM_RATING = 2

    // used goods type
    const val GOODS_PET = 1

    // link data type
    const val LINK_PROFILE = "profile"
    const val LINK_POST = "post"
    const val LINK_GOODS = "goods"

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

    // selected goods type
    const val SELECTED_GOODS_ONLY_THUMB = 0
    const val SELECTED_GOODS_WITH_INFO = 1

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

    // goods rating
    const val GOODS_RATING_NONE = -1
    const val GOODS_RATING_BAD = 0
    const val GOODS_RATING_SOSO = 1
    const val GOODS_RATING_GOOD = 2

    // social notification type
    const val SOCIAL_NOTIFICATION_LIKE_POST = 0
    const val SOCIAL_NOTIFICATION_POST_COMMENT = 1
    const val SOCIAL_NOTIFICATION_TAG = 2
    const val SOCIAL_NOTIFICATION_FOLLOW = 3

    // shop notification type
    const val SHOP_NOTIFICATION_LEAD_DECIDE = 0
    const val SHOP_NOTIFICATION_LEAD_RATING = 1

    // media save type
    const val MEDIA_VIDEO = 1
    const val MEDIA_GIF = 2
    const val MEDIA_IMAGE = 3

    // media save folders
    const val TMP_VITAMIO_FILES_FOLDER = "/.xlab/tmp_video/"
    const val TMP_VIDEO_FOLDER = "/.xlab/video/"
    const val TMP_RAW_FOLDER = "/.xlab/raw/"
    const val TMP_PICTURE_FOLDER = "/.xlab/pictures/"
    const val TMP_GIF_FOLDER = "/.xlab/gif/"
    const val PICTURE_FOLDER = "/xlab/pictures/"
    const val VIDEO_FOLDER = "/xlab/video/"
    const val GIF_FOLDER = "/xlab/gif/"

}