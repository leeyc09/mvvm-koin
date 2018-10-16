package xlab.world.xlab.server

/**
 * Created by kdu01 on 2018-01-26.
 */
object ApiURL {
    private const val MAIN_SCHEME = "http"
    private const val MAIN_SCHEME_SSL = "https"

    // TODO: xlab shop api server url
    private const val XLAB_GODO_DOMAIN = "shop.xlab.io"
    private const val XLAB_GODO_MOBILE_DOMAIN = "m.$XLAB_GODO_DOMAIN"
    const val XLAB_GODO_URL_SSL =  "$MAIN_SCHEME_SSL://$XLAB_GODO_DOMAIN"
    const val XLAB_GODO_MOBILE_URL_SSL =  "$MAIN_SCHEME_SSL://$XLAB_GODO_MOBILE_DOMAIN"
//    const val XLAB_GODO_MOBILE_URL =  "$MAIN_SCHEME://$XLAB_GODO_MOBILE_DOMAIN"
    const val GODO_IMAGE_HEADER_URL = "$XLAB_GODO_URL_SSL/data/goods/"

    // TODO: xlab shop member
    private const val GODO_MEM = "/api/mem"
    const val GODO_REGISTER = "$GODO_MEM/autoRegister.php"

    // TODO: goods detail
    private const val GODO_GOODS = "/api/goods"
    const val GODO_GOODS_DETIAL_CD = "$GODO_GOODS/view_cd.php"

    // TODO: xlab api server url
    const val XLAB_API_DOMEN = "xlab.io"
    const val XLAB_API_URL_SSL = "$MAIN_SCHEME_SSL://$XLAB_API_DOMEN"

    // TODO: User
    private const val USERS = "/users"
    const val CHECK_VALID_TOKEN = "$USERS/checkValidToken"
    const val USER_REFRESH_TOKEN = "/refreshToken"
    const val GENERATE_TOKEN = "/generateToken"
    const val USER_CHECK_PASSWORD = "$USERS/checkPassword"
    const val USER_CHANGE_PASSWORD = "$USERS/changePassword"
    const val USER_AUTH_EMAIL = "$USERS/authEmail"
    const val USER_AUTH_EMAIL_CODE = "$USERS/authEmailCode"
    const val USER_LOGOUT = "$USERS/logout"
    const val USER_LOGIN = "$USERS/login"
    const val USER_REGISTER = "$USERS/register"
    const val USER_WITHDRAW = "$USERS/withdraw"
    const val USER_PROFILE_MAIN = "$USERS/profile/main"
    const val USER_PROFILE_EDIT = "$USERS/profile/edit"
    const val USER_PROFILE_UPDATE = "$USERS/profile"
    const val USER_SEARCH = "$USERS/search"
    const val USER_USED_ITEM = "$USERS/usedItem"
    const val USER_RECOMMEND = "$USERS/recommend"
    const val USER_SETTING = "$USERS/setting"
    const val USER_SETTING_PUSH = "$USERS/setting/push"

    // TODO: User Activity
    private const val ACTIVITY = "/activity"
    private const val ACTIVITY_POST = "$ACTIVITY/post"
    const val ACTIVITY_POST_LIKE = "$ACTIVITY_POST/like"
    const val ACTIVITY_POST_SAVE = "$ACTIVITY_POST/save"
    const val USER_REPORT = "$ACTIVITY/report"
    private const val ACTIVITY_SHOP = "$ACTIVITY/shop"
    const val ACTIVITY_USED_ITEM = "$ACTIVITY_SHOP/usedItem"
    const val ACTIVITY_RECENT_DELIVER_PLACE = "$ACTIVITY_SHOP/recentDeliverPlace"

    // TODO: Post
    const val POSTS = "/posts"
    const val POST_VIEW = "$POSTS/view"
    const val POST_UPDATE = "$POSTS/update"
    const val POSTS_ALBUM = "$POSTS/profile/album"
    const val POSTS_ALBUM_DETAIL = "$POSTS_ALBUM/detail"
    const val POSTS_SEARCH = "$POSTS/search"
    const val POSTS_CHECK_MINE = "$POSTS/mine"
    const val POSTS_FOLLOWING = "$POSTS/following"
    const val POSTS_MAIN = "$POSTS/main"
    const val POSTS_EXPLORE = "$POSTS/explore"

    // TODO: Pet
    const val PETS = "/pets"
    const val PETS_LIST = "$PETS/list"
    const val PETS_TOGGLE = "$PETS/toggle"
    const val PET_GOODS_DETAIL = "$PETS/goodsDetail"
    const val PET_USED_ITEMS = "$PETS/usedItems"

    // TODO: Follow
    const val FOLLOW = "/follow"
    const val FOLLOWER_LIST = "$FOLLOW/follower"
    const val FOLLOWING_LIST = "$FOLLOW/following"

    // TODO: Hash Tag
    private const val HASH_TAG = "/hashTags"
    const val HASH_TAG_RECENT = "$HASH_TAG/recent"
    const val HASH_TAG_COUNT = "$HASH_TAG/count"
    const val USER_HASH_TAG_UPDATE = "$HASH_TAG/userHashTag"

    // TODO: Comment
    const val COMMENT = "/comments"

    // TODO: Shop
    private const val SHOP = "/shop"
    const val SHOP_GOODS = "$SHOP/goods"
    const val SHOP_GOODS_DETAIL = "$SHOP/goods/detail"
    const val SHOP_MAIN = "$SHOP/main"
    const val SHOP_SEARCH = "$SHOP/search"
    const val SHOP_GOODS_STATS = "$SHOP/goods/stats"
    const val SHOP_TAGGED_POSTS = "$SHOP/tagged/posts"
    const val SHOP_VIEW_GOODS = "$SHOP/viewGoods"

    private const val GODO = "/godo"
    const val SHOP_LOGIN = "$XLAB_GODO_URL_SSL/Users/UsersLoginController"
    const val SHOP_LOGOUT = "$XLAB_GODO_URL_SSL/Users/UsersLogoutController"
    const val SHOP_MY_CART = "$GODO/cart"
    const val SHOP_MY_CART_COUNT = "$SHOP_MY_CART/count"
    const val SHOP_MY_CART_UPDATE = "$SHOP_MY_CART/update"
    const val SHOP_MY_CART_ADD = "$SHOP_MY_CART/add"
    const val SHOP_MY_CART_DELETE = "$SHOP_MY_CART/delete"
    const val SHOP_ORDER = "$GODO/order"
    const val SHOP_ORDER_HISTORY = "$SHOP_ORDER/history"
    const val SHOP_ORDER_STATUS_NUM = "$SHOP_ORDER/history/status/num"
    const val SHOP_ORDER_STATUS_HISTORY = "$SHOP_ORDER/history/status"
    const val SHOP_ORDER_CRR_DETAIL = "$SHOP_ORDER/crrDetail"
    private const val SHOP_ORDER_REQUEST= "$SHOP_ORDER/request"
    const val SHOP_ORDER_CANCEL = "$SHOP_ORDER_REQUEST/cancel"
    const val SHOP_ORDER_DECIDE = "$SHOP_ORDER_REQUEST/decide"
    const val SHOP_ORDER_RECEIVE_CONFIRM = "$SHOP_ORDER_REQUEST/receiveConfirm"
    const val SHOP_ORDER_REFUND_RETURN_CHANGE = "$SHOP_ORDER_REQUEST/crr"

    const val SHOP_PROFILE = "$GODO/profile"

    // TODO: Notification
    private const val NOTIFICATION = "/notification"
    const val EXIST_NEW_NOTIFICATION = "$NOTIFICATION/exist/new"
    const val SOCIAL_NOTIFICATION = "$NOTIFICATION/social"

    // TODO: Notices
    const val NOTICE = "/notices"
    const val NOTICE_READ = "$NOTICE/read"
    const val EXIST_NEW_NOTICE = "$NOTICE/exist/new"
}
