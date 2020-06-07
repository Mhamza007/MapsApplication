package com.mhamza007.mapsapplication.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

import com.mhamza007.mapsapplication.R
import com.mhamza007.mapsapplication.fragments.MainFragment
import java.lang.Exception

class MapsActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener,
    GoogleApiClient.ConnectionCallbacks, LocationListener {

    private lateinit var googleApiClient: GoogleApiClient
    private var mainFragment: MainFragment? = null

    companion object {
        const val LOCATION_PERMISSION = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addConnectionCallbacks(this)
            .addApi(LocationServices.API)
            .build()

        try {
            mainFragment =
                supportFragmentManager.findFragmentById(R.id.container_main) as MainFragment
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance()
            mainFragment?.let {
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.container_main, it)
                    .commit()
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onConnected(p0: Bundle?) {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION
            )
            Log.d("MAP", "Requesting Permission")
        } else {
            Log.d("MAP", "Starting Location Services from onConnected")
            startLocationService()
        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onLocationChanged(location: Location?) {
        Log.d("MAP", "Lat: ${location?.latitude}, Lon: ${location?.longitude}")

        location?.let {
            mainFragment?.setMarker(LatLng(it.latitude, it.longitude))
        }
    }

    override fun onStart() {
        super.onStart()

        googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()

        googleApiClient.disconnect()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MAP", "Starting Location Services from onRequestPermissionsResult")
                    startLocationService()
                } else
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLocationService() {
        Log.d("MAP", "Starting Location Services")

        val locationRequest =
            LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        LocationServices.FusedLocationApi.requestLocationUpdates(
            googleApiClient,
            locationRequest,
            this
        )
        Log.d("MAP", "Requesting Location Updates")
    }
}