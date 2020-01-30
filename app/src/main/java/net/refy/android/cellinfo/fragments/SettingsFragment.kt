package net.refy.android.cellinfo.fragments

import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import net.refy.android.cellinfo.R
import net.refy.android.cellinfo.services.MccMncService

class SettingsFragment : PreferenceFragmentCompat(){
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        // handle events
        findPreference<SwitchPreferenceCompat>("enabled")?.setOnPreferenceClickListener {  preference ->
            when((preference as SwitchPreferenceCompat).isChecked){
                true -> start()
                else -> stop()
            }
            true
        }
    }

    private fun start(){
        requireContext().startForegroundService(Intent(requireContext(), MccMncService::class.java))
    }

    private fun stop(){
        requireContext().stopService(Intent(requireContext(), MccMncService::class.java))
    }
}