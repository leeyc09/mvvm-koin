package xlab.world.xlab.utils.support

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONException
import xlab.world.xlab.utils.fcm.FcmSetting
import xlab.world.xlab.utils.support.AppConstants.LOCAL_LOGIN
import xlab.world.xlab.utils.support.AppConstants.NORMAL_USER_LEVEL

class SPHelper(context: Context) {
    val tag = "SPHelper"
    private var pref: SharedPreferences

    val authorization: String
        get() =  "Bearer $accessToken"

    init {
        pref = context.getSharedPreferences(SP_NAME, 0)
    }

    fun clearData() {
        pref.edit().clear().apply()
    }

    fun login(accessToken: String, userType: Int, userId: String, socialId: String,
              userLevel: Int, userEmail: String, push: Boolean) {
        PrintLog.d("user login", "save user login data")
        this.accessToken = accessToken
        this.userType = userType
        this.userId = userId
        this.socialId = socialId
        this.userLevel = userLevel
        this.userEmail = userEmail

        // 푸시 설정에 따라 fcm topic 추가 or 삭제
        if (push)
            FcmSetting.subscribeTopic()
        else
            FcmSetting.unSubscribeTopic()
    }

    fun logout() {
        PrintLog.d("user logout", "remove user login data")
        pref.edit().remove(TOKEN).apply()
        pref.edit().remove(USER_ID).apply()
        pref.edit().remove(SOCIAL_ID).apply()
        pref.edit().remove(USER_TYPE).apply()
        pref.edit().remove(USER_LEVEL).apply()
        pref.edit().remove(USER_EMAIL).apply()
        pref.edit().remove(RECENT_SEARCH).apply()

        // fcm topic 삭제
        FcmSetting.unSubscribeTopic()
    }

    var onBoard: Boolean
        get() = pref.getBoolean(ON_BOARD, false)
        set(onBoard) {
            PrintLog.d("New onBoard", onBoard.toString())
            pref.edit().putBoolean(ON_BOARD, onBoard).apply()
        }

    var fcmToken: String
        get() = pref.getString(FCM_TOKEN, "")!!
        set(fcmToken)  {
            PrintLog.d("New fcmToken", fcmToken)
            pref.edit().putString(FCM_TOKEN, fcmToken).apply()
        }

    var accessToken: String
        get() = pref.getString(TOKEN, "")!!
        set(accessToken)  {
            PrintLog.d("New accessToken", accessToken)
            pref.edit().putString(TOKEN, accessToken).apply()
        }

    var userId: String
        get() = pref.getString(USER_ID, "")!!
        set(userId)  {
            PrintLog.d("New userId", userId)
            pref.edit().putString(USER_ID, userId).apply()
        }

    var socialId: String
        get() = pref.getString(SOCIAL_ID, "")!!
        set(socialId)  {
            PrintLog.d("New socialId", socialId)
            pref.edit().putString(SOCIAL_ID, socialId).apply()
        }

    var userType: Int
        get() = pref.getInt(USER_TYPE, LOCAL_LOGIN)
        set(userType)  {
            PrintLog.d("New userType", userType.toString())
            pref.edit().putInt(USER_TYPE, userType).apply()
        }

    var userLevel: Int
        get() = pref.getInt(USER_LEVEL, NORMAL_USER_LEVEL)
        set(userLevel)  {
            PrintLog.d("New userLevel", userLevel.toString())
            pref.edit().putInt(USER_LEVEL, userLevel).apply()
        }

    var userEmail: String
        get() = pref.getString(USER_EMAIL, "")!!
        set(userEmail)  {
            PrintLog.d("New userEmail", userEmail)
            pref.edit().putString(USER_EMAIL, userEmail).apply()
        }

    var recentSearch: LinkedHashSet<String>
        @Throws(JSONException::class)
        get() {
            val searchList = LinkedHashSet<String>()

            val ps = pref.getString(RECENT_SEARCH, "")
            if (ps!!.isNotEmpty()) {
                val jList = JSONArray(ps)
                (0 until jList.length()).forEach { index ->
                    searchList.add(jList.getString(index))
                }
            }

            return searchList
        }
        set(searchList) {
            val jList = JSONArray()

            searchList.forEach { text ->
                jList.put(text)
            }

            pref.edit().putString(RECENT_SEARCH, jList.toString()).apply()
        }

    companion object {
        private const val SP_NAME = "xlab"
        private const val ON_BOARD = "onBoard"
        private const val FCM_TOKEN = "fcmToken"
        private const val TOKEN = "accessToken"
        private const val USER_ID = "userID"
        private const val SOCIAL_ID = "socialID"
        private const val USER_TYPE = "userType"
        private const val USER_LEVEL = "userLevel"
        private const val USER_EMAIL = "userMail"
        private const val RECENT_SEARCH = "recentSearch"
    }
}