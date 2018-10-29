package xlab.world.xlab.utils.fcm

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.view.preload.PreloadActivity


class MyFirebaseMessagingService: FirebaseMessagingService() {
    private val tag = "FCM"
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(newToken: String?) {
        newToken?.let{
            PrintLog.d("new fcm token", newToken)

            SPHelper(applicationContext).fcmToken = newToken
        }

    }
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // TODO(developer) : Handle FCM messages here.
        // Not gettingmessages here? See why this my be: https://goo.gl/39bRNJ
        remoteMessage?.let{ _ ->
            remoteMessage.from?.let {
                PrintLog.d("FCM From", it)
            }
            // Check if message contains a data payload.
            if (remoteMessage.data.isNotEmpty()) {
                PrintLog.d("Message data payload", remoteMessage.data.toString())
                sendNotification(remoteMessage.data)
            }

            // Check if message contains a notification payload.
            if (remoteMessage.notification != null) {
                remoteMessage.notification!!.body?.let { messageBody ->
                    PrintLog.d("Message Notification Body", messageBody)
                    sendNotification(messageBody)
                }
            }

            // Also if you intend on generating your own notifications as a result of a received FCM
            // message, here is where that should be initiated. See sendNotification method below.
        }
    }

    private val channelId = "xlab_channel"
    private val channelName = "xlab_notification"
    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(data: Map<String, String>) {
//        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        PrintLog.d("appIsInBackground", appIsInBackground().toString(), tag)
        PrintLog.d("data", data.toString(), tag)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder: NotificationCompat.Builder? = when (data["type"]) {
            FcmSetting.DEFAULT_NOTIFICATION -> {
                NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_notification)
                        .setContentText(data["message"])
                        .setAutoCancel(true)
            }
            else -> null
        }

        if (appIsInBackground()) {
            // notification touch intent
            val intent = Intent(this, PreloadActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(IntentPassName.NOTIFICATION_TYPE, data["notiType"])
            intent.putExtra(IntentPassName.NOTIFICATION_DATA, data["data"])

            val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT)
            notificationBuilder?.let {
                notificationBuilder.setContentIntent(pendingIntent)
            }
        }

        notificationBuilder?.let {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setContentTitle(resources.getString(R.string.app_name)) //-> 앱 이름 안나오는 버전 체크해서 타이틀에 앱 이름 붙이기
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        }
    }

    private fun sendNotification(messageBody: String) {
//        val intent = Intent(this, PreloadActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT)
//        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Xlab FCM")
                .setContentText(messageBody)
                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun appIsInBackground(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = am.runningAppProcesses

        runningProcesses.forEach { processInfo ->
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                 processInfo.pkgList.forEach { activeProcess ->
                     if (activeProcess == packageName)
                         return false
                 }
            }
        }

        return true
    }
}