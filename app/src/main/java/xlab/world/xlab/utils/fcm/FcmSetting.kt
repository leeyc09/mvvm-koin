package xlab.world.xlab.utils.fcm

import com.google.firebase.messaging.FirebaseMessaging

object FcmSetting {
    private const val XLAB_FCM_TOPIC = "XLAB_TOPIC"

    const val DEFAULT_NOTIFICATION = "default"
    const val SOCIAL_NOTIFICATION = "social"
    const val SHOP_NOTIFICATION = "shop"

    fun subscribeTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(XLAB_FCM_TOPIC)
    }
    fun unSubscribeTopic() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(XLAB_FCM_TOPIC)
    }
}