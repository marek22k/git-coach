package com.vishal2376.gitcoach.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vishal2376.gitcoach.R
import com.vishal2376.gitcoach.utils.Constants
import com.vishal2376.gitcoach.utils.LoadData

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.e("@@@", "Notification Receiver called")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(channel)
        }

        //get data
        val gitCommandList = LoadData.getGitCommandData(context)!!.gitCommands
        val totalSize = gitCommandList.size - 1

        //get one random git command
        val randomIndex = (0..totalSize).random()
        val gitTitle = gitCommandList[randomIndex].name
        val gitCommand = gitCommandList[randomIndex].command
        val gitDescription = gitCommandList[randomIndex].description
        val gitLongDesc = gitCommand + "\n\n" + gitDescription

        //build notification
        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo).setContentTitle(gitTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(gitLongDesc))
            .setContentText(gitCommand).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true).build()

        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
    }
}