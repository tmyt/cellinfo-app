package net.refy.android.cellinfo.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.os.IBinder
import android.telephony.*
import androidx.core.app.NotificationManagerCompat
import java.util.*
import kotlin.math.abs

class MccMncService : Service() {
    companion object {
        const val channelId = "cellinfo"
        const val notificationId = 1000
    }

    data class NetworkInfo(val mcc: String, val mnc: String)

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var timer: Timer

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) = updateNotification()
        override fun onServiceStateChanged(serviceState: ServiceState?) = updateNotification()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // init
        telephonyManager = getSystemService(TelephonyManager::class.java)
        // register
        registerListener()
        createChannel()
        startForeground(notificationId, createNotification(getRegisteredCellInfo()))
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterListener()
    }

    private fun registerListener() {
        if (::timer.isInitialized) {
            timer.cancel()
        }
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() = updateNotification()
            }, 300 * 1000, 300 * 1000)
        }
        updateListener(PhoneStateListener.LISTEN_SERVICE_STATE or PhoneStateListener.LISTEN_CELL_INFO)
    }

    private fun unregisterListener() {
        if(::timer.isInitialized){
            timer.cancel()
        }
        updateListener(0)
    }

    private fun updateListener(events: Int) {
        telephonyManager.listen(phoneStateListener, events)
    }

    private fun updateNotification() {
        NotificationManagerCompat.from(this)
            .notify(notificationId, createNotification(getRegisteredCellInfo()))
    }

    private fun createNotification(info: NetworkInfo): Notification {
        return createNotification(info.mcc, info.mnc)
    }

    private fun createNotification(mcc: String, mnc: String): Notification {
        val builder=  Notification.Builder(this, channelId)
            .setSmallIcon(createNotificationIcon(mcc, mnc))
            .setContentTitle("Current Registration")
            .setContentText("MCC: $mcc MNC: $mnc")
            .setOngoing(true)
        return builder.build()
    }

    private fun createChannel(): NotificationChannel {
        val channel = NotificationChannel(channelId, "CellInfo notification", NotificationManager.IMPORTANCE_DEFAULT)
        channel.enableVibration(false)
        channel.setShowBadge(false)
        channel.setSound(null, null)
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
        return channel
    }

    private fun createNotificationIcon(mcc: String, mnc: String): Icon {
        val density = resources.displayMetrics.density
        val dp: (Int) -> Float = { it * density }
        val round: (Float) -> Int = { (it + 0.5).toInt() }
        val paint = Paint().apply {
            isFakeBoldText = true
            isAntiAlias = true
            color = Color.WHITE
            textSize = dp(11)
        }
        val bitmap = Bitmap.createBitmap(round(dp(24)), round(dp(24)), Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        canvas.drawText(mcc, dp(2), abs(paint.fontMetrics.top), paint)
        canvas.drawText(mnc, dp(2), abs(paint.fontMetrics.top) * 2, paint)
        return Icon.createWithBitmap(bitmap)
    }

    private fun getRegisteredCellInfo(): NetworkInfo {
        if (!checkPermission()) return NetworkInfo("N/A", "N/A")
        telephonyManager.allCellInfo.filter { it.isRegistered }.forEach {
            return when (it) {
                is CellInfoGsm -> NetworkInfo(it.cellIdentity.mccString ?: "N/A", it.cellIdentity.mncString ?: "N/A")
                is CellInfoLte -> NetworkInfo(it.cellIdentity.mccString ?: "N/A", it.cellIdentity.mncString ?: "N/A")
                is CellInfoWcdma -> NetworkInfo(it.cellIdentity.mccString ?: "N/A", it.cellIdentity.mncString ?: "N/A")
                else -> NetworkInfo("???", "???")
            }
        }
        return NetworkInfo("N/A", "N/A")
    }

    private fun checkPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return false
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return false
        return true
    }
}