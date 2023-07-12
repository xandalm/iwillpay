package com.xandealm.iwillpay.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.xandealm.iwillpay.IwillpayApplication
import com.xandealm.iwillpay.MainActivity
import com.xandealm.iwillpay.R

class ExpenseReminderWorker(ctx: Context, params: WorkerParameters): Worker(ctx, params) {
    override fun doWork(): Result {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent
            .getActivity(applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        val expenseTitle = inputData.getString(titleKey)

        val builder = NotificationCompat.Builder(applicationContext, IwillpayApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_paid_24)
            .setContentTitle("Hey!")
            .setContentText("Pay $expenseTitle now!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(13, builder.build())
            }
        }

        return Result.success()
    }

    companion object {
        const val idKey = "ID"
        const val titleKey = "TITLE"
    }
}