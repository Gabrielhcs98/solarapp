package com.example.solarapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.transition.Slide
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.solarapp.util.DialogUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var editTextLocation: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonHere: Button
    private val requestLocationPermission = 1
    private lateinit var networkChecker: NetworkChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextLocation = findViewById(R.id.editTextLocation)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        buttonHere = findViewById(R.id.buttonHere)
        networkChecker = NetworkChecker(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        buttonSubmit.setOnClickListener {
            val location = editTextLocation.text.toString().trim()
            val regex = Regex("^[A-Za-zÁÉÍÓÚÂÊÎÔÛÃÕÇáéíóúâêîôûãõç\\s]+$")

            if (networkChecker.isNetworkQualityPoor()) {
                networkChecker.checkNetworkQuality()
                return@setOnClickListener
            }

            if (location.isEmpty() || !regex.matches(location)) {
                showAlertDialog(getString(R.string.campo_invalido))
            } else {
                navigateToResultsActivity(location)
            }
        }

        buttonHere.setOnClickListener {
            checkLocationPermission()
        }
    }


    private fun showLocationServiceDialog() {
        DialogUtils.showCustomAlertDialog(
            context = this,
            title = getString(R.string.attention),
            message = getString(R.string.location_services_disabled),
            positiveButtonText = getString(R.string.go_to_settings),
            negativeButtonText = getString(R.string.cancel),
            onPositiveClick = {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            },
            onNegativeClick = {
                // Fecha o diálogo
            }
        )
    }

    private fun obtainLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestLocationPermission
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    geocodeLocation(latitude, longitude)
                } else {
                    showToast("Localização não disponível")
                }
            }
            .addOnFailureListener { exception ->
                showToast("Erro ao obter localização: ${exception.message}")
            }
    }

    private fun geocodeLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val cityName = addresses[0].locality
                if (cityName != null) {
                    showToast("Pesquisando cidade: $cityName")
                    sendCityToAPI(cityName)
                } else {
                    showToast("Cidade não encontrada")
                }
            } else {
                showToast("Endereço não encontrado")
            }
        } catch (e: IOException) {
            showToast("Erro na geocodificação: ${e.message}")
        }
    }

    private fun sendCityToAPI(cityName: String) {
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("city", cityName)
        window.exitTransition = Slide(Gravity.END)
        startActivity(intent)
    }

    private fun navigateToResultsActivity(location: String) {
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("city", location)
        window.exitTransition = Slide(Gravity.END)
        startActivity(intent)
    }

    private fun showAlertDialog(message: String) {
        DialogUtils.showCustomAlertDialog(
            context = this,
            title = getString(R.string.attention),
            message = message,
            positiveButtonText = "OK",
            onPositiveClick = {
                // Fecha o diálogo
            }
        )
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        val view = toast.view

        view?.background = ResourcesCompat.getDrawable(resources, R.drawable.drawable, null)
        val textView = view?.findViewById<TextView>(android.R.id.message)
        textView?.setTextColor(Color.WHITE) // Altere a cor do texto aqui

        toast.show()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Se a permissão não foi concedida
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionExplanationDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestLocationPermission
                )
            }
            return
        }

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showLocationServiceDialog()
        } else {
            obtainLocation()
        }
    }

    private fun showPermissionExplanationDialog() {
        DialogUtils.showCustomAlertDialog(
            context = this,
            title = getString(R.string.permission_required),
            message = getString(R.string.begging_permission),
            positiveButtonText = "OK",
            negativeButtonText = "Cancelar",
            onPositiveClick = {
                // Redireciona o usuário para as configurações do aplicativo
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            },
            onNegativeClick = {
                showToast(getString(R.string.permission_denied))
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestLocationPermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtainLocation()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    // Permissão negada, mas o usuário pode solicitar novamente
                    showToast("Permissão de localização negada")
                } else {
                    // Permissão negada permanentemente
                    showPermissionExplanationDialog()
                }
            }
        }
    }
}

