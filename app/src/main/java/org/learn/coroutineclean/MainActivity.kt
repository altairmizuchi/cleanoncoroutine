package org.learn.coroutineclean

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import org.learn.data.repository.LocationRepositoryGoogleServiceImpl
import org.learn.domain.entity.Success
import org.learn.domain.interactor.ListenLocationInteractor
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    val case by lazy {
        ListenLocationInteractor(
            LocationRepositoryGoogleServiceImpl.getInstance(this)
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            hasPermission()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), 777)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 777) {
            val ind = permissions.indexOfFirst { it == ACCESS_FINE_LOCATION }
            if (ind != -1 && grantResults[ind] == PackageManager.PERMISSION_GRANTED) {
                hasPermission()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun hasPermission() {
        case.execute(
            null,
            onUpdate = {
                text.text = it.toString()
            },
            onError = {
                text.text = it.message
            }
        )
    }

    override fun onPause() {
        super.onPause()
        case.cancel()
    }
}