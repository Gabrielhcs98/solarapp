package com.example.solarapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.transition.Slide
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.solarapp.util.NetworkChecker
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
                showAlertDialog(getString(R.string.attention), getString(R.string.campo_invalido))
            } else {
                navigateToResultsActivity(location)
            }
        }

        buttonHere.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
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

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showLocationServiceDialog()
        } else {
            obtainLocation()
        }
    }

    private fun showLocationServiceDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.attention))
        builder.setMessage(getString(R.string.location_services_disabled))
        builder.setPositiveButton(getString(R.string.go_to_settings)) { dialog, _ ->
            // Intent para abrir as configurações de localização do dispositivo
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.drawable, null)
        dialog.window?.setBackgroundDrawable(drawable)
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.WHITE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(Color.WHITE)
        }
        dialog.show()
    }


    private fun obtainLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Caso a permissão não esteja concedida, solicita ao usuário
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestLocationPermission
            )
            return
        }

        // Se a permissão foi concedida, então obtém a localização
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    // Aqui você pode chamar uma função para geocodificar a localização
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
            val addresses = geocoder.getFromLocationName("latitude, longitude", 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val cityName = addresses[0].locality // Obtém o nome da cidade
                    if (cityName != null) {
                        showToast("Pesquisando cidade: $cityName")
                        sendCityToAPI(cityName)
                    } else {
                        showToast("Cidade não encontrada")
                    }
                } else {
                    showToast("Endereço não encontrado")
                }
            }
        } catch (e: IOException) {
            showToast("Erro na geocodificação: ${e.message}")
        }
    }



    private fun sendCityToAPI(cityName: String) {
        // Implemente o código para enviar o nome da cidade para a API
        // Aqui você deve construir sua requisição HTTP para enviar cityName para sua API
        // Exemplo simples:
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

    private fun showAlertDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.drawable, null)
        dialog.window?.setBackgroundDrawable(drawable)
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.WHITE)
        }
        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
                showToast("Permissão de localização negada")
            }
        }
    }
}
