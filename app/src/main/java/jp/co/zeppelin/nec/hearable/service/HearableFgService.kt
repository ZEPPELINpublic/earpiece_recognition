package jp.co.zeppelin.nec.hearable.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import jp.co.zeppelin.nec.hearable.MainActivity
import jp.co.zeppelin.nec.hearable.R

/**
 * Deal with restrictions introduced in API 26 ("Oreo") regarding background apps doing things in the
 * background
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class HearableFgService : Service() {
    private val TAG = HearableFgService::class.java.simpleName

    private var mNotificationManager: NotificationManager? = null
    private val handler: Handler? = null
    private var count = 0
    private var stateService = ConnectState.NotConnected.ordinal

    override fun onCreate() {
        super.onCreate()
        mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        stateService = ConnectState.NotConnected.ordinal
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        intent.action?.apply {
            when (this) {
                ACTION_START_FG_SERVICE -> {
                    Log.d(
                        TAG,
                        "Received user starts foreground intent"
                    )
                    startForeground(
                        NOTIFICATION_ID_FOREGROUND_SERVICE,
                        prepareNotification()
                    )

                    // Start the locker receiver
                    // Start the locker receiver
                    val screenactionreceiver = HearableFgSvcBcReceiver()
                    registerReceiver(screenactionreceiver, screenactionreceiver.getFilter())

                    connect()

                }
                else -> {
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        // Ref: https://stackoverflow.com/questions/47110489/background-service-for-android-oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true); //true will remove notification
        }
        super.onDestroy()
    }

    // its connected, so change the notification text
    private fun connect() { // after 10 seconds its connected
        Handler().postDelayed(
            {
                Log.d(
                    TAG,
                    "Bluetooth Low Energy device is connected!!"
                )
                Toast.makeText(applicationContext, "Connected!", Toast.LENGTH_SHORT).show()
                stateService =
                    ConnectState.Connected.ordinal
                startForeground(
                    NOTIFICATION_ID_FOREGROUND_SERVICE,
                    prepareNotification()
                )
            }, 10000
        )
    }


    private fun prepareNotification(): Notification? { // handle build version above android oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            mNotificationManager?.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null
        ) {
            val name: CharSequence = getString(R.string.notification_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                name,
                importance
            )
            channel.enableVibration(false)
            mNotificationManager?.createNotificationChannel(channel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = ACTION_MAIN
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // if min sdk goes below honeycomb
/*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }*/
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // make a stop intent
        val stopIntent =
            Intent(this, HearableFgService::class.java)
        stopIntent.action = ACTION_STOP_FG_SERVICE
        val pendingStopIntent =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val remoteViews = RemoteViews(packageName, R.layout.fg_service_notification)
        remoteViews.setOnClickPendingIntent(R.id.buttonFgServiceStopSendingSensorData, pendingStopIntent)
        when (stateService) {
            ConnectState.NotConnected.ordinal -> remoteViews.setTextViewText(
                R.id.textViewFgServiceDescr,
                applicationContext.resources.getString(R.string.home_sensor_status_prep_ok)
            )
            ConnectState.Connected.ordinal -> remoteViews.setTextViewText(
                R.id.textViewFgServiceDescr,
                applicationContext.resources.getString(R.string.home_sensor_status_send_ok)
            )
        }
        // notification builder
        val notificationBuilder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = Builder(
                this,
                FOREGROUND_CHANNEL_ID
            )
        } else {
            notificationBuilder = Builder(this)
        }
        notificationBuilder
            .setContent(remoteViews)
            .setSmallIcon(R.drawable.ic_fg_service_notification)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        return notificationBuilder.build()
    }

    companion object {
        val NOTIFICATION_ID_FOREGROUND_SERVICE = 8466577
        private const val FOREGROUND_CHANNEL_ID = "foreground_channel_id"

        enum class ConnectState {
            Connected,
            NotConnected
        }

        val ACTION_MAIN = "ACTION_MAIN"
        val ACTION_START_FG_SERVICE = "ACTION_START_FG_SERVICE"
        val ACTION_STOP_FG_SERVICE = "ACTION_STOP_FG_SERVICE"
    }
}
