package com.github.bassaer.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.realm.Realm
import java.text.SimpleDateFormat
import java.util.*

class AlarmNotification : BroadcastReceiver() {

    var mMessageDB: MessageDataBase? = null
    var mRealm: Realm = Realm.getDefaultInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        val requestCode = intent?.getIntExtra("AlarmCode", 0)
        val messageID = intent?.getIntExtra("MessageID", 0)
        val messageTitle = intent?.extras?.getString("MessageTitle", "")
        val messageContent = intent?.extras?.getString("MessageContent", "")

        val pendingIntent = messageID?.let {
            PendingIntent.getActivity(
                    context,
                    it,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
        }
        // Realm set up
        val realm: Realm = Realm.getDefaultInstance()

        realm.beginTransaction()

        mMessageDB = this.mRealm.where(MessageDataBase::class.java)
                .equalTo("id", messageID)
                .findAll()[0]

        mMessageDB!!.alarmFlag = false
        realm.copyToRealmOrUpdate(mMessageDB)
        realm.commitTransaction()
        realm.close()

        val channelId = "default"

        val title = messageTitle

        val currentTime: Long = System.currentTimeMillis()

        val dataFormat = SimpleDateFormat("HH:mm:ss", Locale.JAPAN)
        val message = messageContent

        val notificationManager: NotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val channel = NotificationChannel(
                channelId, title, NotificationManager.IMPORTANCE_DEFAULT
        )

        channel.description = message
        channel.enableVibration(true)
        channel.canShowBadge()
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        channel.setSound(defaultSoundUri, null)
        channel.setShowBadge(true)

        notificationManager.createNotificationChannel(channel)

        val notification = Notification.Builder(context, channelId)
                .setContentTitle(title)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .build()
        notificationManager.notify(R.string.app_name, notification)
    }
}
