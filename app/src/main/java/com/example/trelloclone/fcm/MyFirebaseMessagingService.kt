package com.example.trelloclone.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.trelloclone.R
import com.example.trelloclone.activities.MainActivity
import com.example.trelloclone.activities.SignInActivity
import com.example.trelloclone.firestore.FireStoreClass
import com.example.trelloclone.utils.Constant
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService() : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage : RemoteMessage) {
        //here our application is both SENDER and a RECEIVER
        //if user X add user Y to the board's members list then
        //user X is sending data to the firebase server and (SENDER SIDE)
        //user Y ( onMessageReceived() ) get the notification about it (RECEIVER SIDE)
        super.onMessageReceived(remoteMessage)

        println("Notification_Part:::${remoteMessage.from}")
        Log.d(TAG,"FROM::${remoteMessage.from}")
        //here i am using my own CUSTOM MADE TAG using companion object

        remoteMessage.data.isNotEmpty().let {
            println("Notification_Part:::MESSAGE_DATA_PAYLOAD::${remoteMessage.data}")

            val title = remoteMessage.data[Constant.FCM_KEY_TITLE]
            val message = remoteMessage.data[Constant.FCM_KEY_MESSAGE]

            sendNotification(title!!,message!!)
        }

        remoteMessage.notification?.let {
            println("Notification_Part:::Notification:::${it.body}")
        }
    }

    override fun onNewToken(token : String) {
        super.onNewToken(token)
        println("Token_part::Refreshed_Token:::$token")
    }

    private fun sendNotification(title : String,message : String){
        //by calling this function we can CREATE AND SEND the NOTIFICATION
        val intent = if(FireStoreClass().getCurrentUserID().isNotEmpty()){
            Intent(this,MainActivity::class.java)
        }else{
            Intent(this,SignInActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)

        val channelId = resources.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this,channelId).setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //for android oreo OR more then that,notification channel is required so that is why we are doing this code
            val channel = NotificationChannel(channelId,"Channel Trello Clone Title",NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notificationBuilder.build())
    }

    private fun sendRegistrationToServer(token : String){
        //TODO implement this function
    }
    companion object{
        private const val TAG = "Notification_Part"
    }
}