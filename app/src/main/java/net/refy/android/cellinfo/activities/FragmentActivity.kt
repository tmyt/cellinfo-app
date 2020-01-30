package net.refy.android.cellinfo.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.refy.android.cellinfo.R
import net.refy.android.cellinfo.fragments.SettingsFragment

class FragmentActivity : AppCompatActivity(){

    companion object {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root, SettingsFragment())
            .commit()

        if(!checkRequiredPermissions()){
            requestPermissions(requiredPermissions, 1000)
        }
    }

    private fun checkRequiredPermissions(): Boolean{
        return requiredPermissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }
}