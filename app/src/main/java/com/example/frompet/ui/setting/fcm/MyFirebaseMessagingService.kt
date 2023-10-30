package com.example.frompet.ui.setting.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.frompet.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

//알림메시지를 처리하는 클래스
class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val fcmTokenManager = FCMTokenManager()

    //알림 수신할때 호출 되는 함수
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message Received")

        //알림페이로드를 처리
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Title"
            val message = notification.body ?: "Message"
            Log.d("FCM", "Title: $title, Message: $message")
            showNotification(title, message)
        }?: run{
            Log.d("FCM","Notification payload failure")
        }
    }

    //새로운 FCM토큰을 받을 때 호출되는 함수
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            fcmTokenManager.fetchAndBaseFCMToken(it.uid)  // 새로운 FCM 토큰을 가져와서 저장 앱 삭제나 했을경우에
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "FromPetNotificationChannel"
        val channelName = "FROM_PET_NOTIFICATION"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "반려동물 소개팅 어플입니다"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.splash)            //아이콘
            .setContentTitle(title)           //알림데이터모델에 제목
            .setContentText(message)         //알림데이터모델에 메시지
            .setPriority(NotificationCompat.PRIORITY_HIGH)        //중요도
            .setAutoCancel(true)         //클릭했을때 자동사라짐

        val notificationId = System.currentTimeMillis().toInt() //알림 Id 생성 현재시간으로 사용해서 각 알림에 고유한 id생성,알림 동시에 오더라도 각각별도로 표시
        notificationManager.notify(notificationId, notificationBuilder.build()) //알람을 표시 알람id식별하고 builder를 사용하여 알림내용 구성
    }
}