package mhs.unisbank.prayertime

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class SettingActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvLocation: TextView
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var etDetailAddress: EditText
    private lateinit var btnSave: Button
    private lateinit var btnGetLocation: Button

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)
        // Inisialisasi UI
        tvLocation = findViewById(R.id.tvLocation)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        etDetailAddress = findViewById(R.id.etDetailAddress)
        btnSave = findViewById(R.id.btnSave)
        btnGetLocation = findViewById(R.id.btnGetLocation)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Tombol Lokasi Otomatis
        btnGetLocation.setOnClickListener {
            checkLocationPermissionAndRequest()
        }

        // Tombol Gunakan Input Manual
        btnSave.setOnClickListener {
            val lat = etLatitude.text.toString().toDoubleOrNull()
            val lon = etLongitude.text.toString().toDoubleOrNull()
            val address = etDetailAddress.text.toString()
            val resultIntent = Intent().apply {
                putExtra("latitude", lat)
                putExtra("longitude", lon)
                putExtra("address", address)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun checkLocationPermissionAndRequest() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            requestLocation()
        }
    }

    private fun requestLocation() {
        // Pengecekan eksplisit izin lokasi
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            tvLocation.text = "Izin belum diberikan untuk lokasi"
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).apply {
            setMinUpdateIntervalMillis(500)
            setMaxUpdateDelayMillis(2000)
            setGranularity(Granularity.GRANULARITY_FINE)
            setMaxUpdates(1)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    etLatitude.setText(latitude.toString())
                    etLongitude.setText(longitude.toString())
                    tvLocation.text = "Otomatis:\nLatitude: $latitude\nLongitude: $longitude"
                } else {
                    tvLocation.text = "Lokasi tidak tersedia"
                }
            }
        }

        try {
            // Pengecekan izin ulang (defensive)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    mainLooper
                )
            } else {
                tvLocation.text = "Izin belum diberikan (ulang)"
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            tvLocation.text = "Terjadi error: ${e.message}"
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocation()
        } else {
            tvLocation.text = "Izin lokasi ditolak"
        }
    }
}