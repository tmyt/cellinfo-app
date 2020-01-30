package net.refy.android.cellinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import net.refy.android.cellinfo.services.MccMncService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val isEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enabled", false)
        if (isEnabled) {
            context?.startForegroundService(Intent(context, MccMncService::class.java))
        }
    }
}