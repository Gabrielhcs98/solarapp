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
import androidx.lifecycle.lifecycleScope
import com.example.solarapp.util.DialogUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

/**
 * MainActivity é a atividade principal do aplicativo SolarApp.
 * Esta atividade lida com a obtenção da localização do usuário,
 * validação de entrada e navegação para outra atividade para exibir resultados.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var editTextLocation: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonHere: Button
    private val requestLocationPermission = 1
    private val requestLocationSettings = 2
    private lateinit var networkChecker: NetworkChecker
    private val debounceDelay = 1000L // 1 segundo de debounce
    private var lastClickTime = 0L

    /**
     * Chamado quando a atividade é criada.
     * Inicializa os componentes da interface do usuário e configura os ouvintes de clique dos botões.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextLocation = findViewById(R.id.editTextLocation)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        buttonHere = findViewById(R.id.buttonHere)
        networkChecker = NetworkChecker(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        buttonSubmit.setOnClickListener {
            if (System.currentTimeMillis() - lastClickTime > debounceDelay) {
                lastClickTime = System.currentTimeMillis()
                handleSubmitClick()
            }
        }

        buttonHere.setOnClickListener {
            if (System.currentTimeMillis() - lastClickTime > debounceDelay) {
                lastClickTime = System.currentTimeMillis()
                handleHereClick()
            }
        }
    }

    /**
     * Trata o clique no botão Submit.
     * Valida a entrada do usuário e navega para a atividade de resultados se a entrada for válida.
     */
    private fun handleSubmitClick() {
        val location = editTextLocation.text.toString().trim()
        val regex = Regex("^[A-Za-zÁÉÍÓÚÂÊÎÔÛÃÕÇáéíóúâêîôûãõç\\s]+$")

        if (networkChecker.isNetworkQualityPoor()) {
            networkChecker.checkNetworkQuality()
            return
        }

        if (location.isEmpty() || !regex.matches(location)) {
            showAlertDialog(getString(R.string.campo_invalido))
        } else {
            navigateToResultsActivity(location)
        }
    }

    /**
     * Trata o clique no botão Here.
     * Verifica a permissão de localização e obtém a localização atual do usuário.
     */
    private fun handleHereClick() {
        checkLocationPermission()
    }

    /**
     * Exibe um diálogo informando que os serviços de localização estão desativados.
     */
    private fun showLocationServiceDialog() {
        DialogUtils.showCustomAlertDialog(
            context = this,
            title = getString(R.string.attention),
            message = getString(R.string.location_services_disabled),
            positiveButtonText = getString(R.string.go_to_settings),
            negativeButtonText = getString(R.string.cancel),
            onPositiveClick = {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent, requestLocationSettings)
            },
            onNegativeClick = {
                // Fecha o diálogo
            }
        )
    }

    /**
     * Obtém a última localização conhecida do usuário e realiza a geocodificação para obter o endereço.
     */
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

    /**
     * Realiza a geocodificação das coordenadas fornecidas e obtém o endereço correspondente.
     *
     * @param latitude A latitude da localização.
     * @param longitude A longitude da localização.
     */
    private fun geocodeLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val neighborhoodName = address.subLocality ?: address.locality ?: address.subAdminArea ?: address.adminArea
                val cityName = address.locality ?: address.subAdminArea ?: address.adminArea

                if (neighborhoodName != null) {
                    showToast("Pesquisando bairro: $neighborhoodName")
                    sendNeighborhoodOrCityToAPI(neighborhoodName, cityName)
                } else {
                    showToast("Bairro não encontrado")
                    sendNeighborhoodOrCityToAPI(null, cityName)
                }
            } else {
                showToast("Endereço não encontrado")
            }
        } catch (e: IOException) {
            showToast("Erro na geocodificação: ${e.message}")
        }
    }

    /**
     * Envia o nome do bairro ou da cidade para uma API.
     *
     * @param neighborhoodName O nome do bairro (ou null se não disponível).
     * @param cityName O nome da cidade.
     */
    private fun sendNeighborhoodOrCityToAPI(neighborhoodName: String?, cityName: String?) {
        val location = neighborhoodName ?: cityName

        lifecycleScope.launch {
            try {
                // Simulação de envio para a API. Substitua pela sua lógica de envio real.
                val response = simulateSendLocationToAPI(location)
                if (response) {
                    navigateToResultsActivity(location!!)
                } else if (neighborhoodName != null) {
                    navigateToResultsActivity(cityName!!)
                }
            } catch (e: Exception) {
                showToast("Erro ao enviar dados: ${e.message}")
                if (neighborhoodName != null) {
                    navigateToResultsActivity(cityName!!)
                }
            }
        }
    }

    /**
     * Simula o envio da localização para uma API e retorna o resultado.
     *
     * @param location A localização a ser enviada.
     * @return true se o envio for bem-sucedido, false caso contrário.
     */
    private suspend fun simulateSendLocationToAPI(location: String?): Boolean {
        return withContext(Dispatchers.IO) {
            if (location.isNullOrEmpty()) {
                return@withContext false
            }

            try {
                val weatherService = WeatherServiceImpl()
                val apiKey = "a77b6a1742276dd6fb74dc969b5d4380"
                val weatherData = weatherService.getWeather(location, apiKey)
                return@withContext weatherData.name.isNotEmpty()
            } catch (e: Exception) {
                return@withContext false
            }
        }
    }

    /**
     * Navega para a atividade de resultados com a localização fornecida.
     *
     * @param location A localização a ser exibida na atividade de resultados.
     */
    private fun navigateToResultsActivity(location: String) {
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("city", location)
        window.exitTransition = Slide(Gravity.END)
        startActivity(intent)
    }

    /**
     * Exibe um diálogo de alerta com a mensagem fornecida.
     *
     * @param message A mensagem a ser exibida no diálogo.
     */
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

    private var lastToast: Toast? = null

    /**
     * Exibe um toast com a mensagem fornecida.
     *
     * @param message A mensagem a ser exibida no toast.
     */
    private fun showToast(message: String) {
        // Cancelar o último toast, se existir
        lastToast?.cancel()

        // Criar um novo toast e exibi-lo
        lastToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        val view = lastToast?.view
        view?.background = ResourcesCompat.getDrawable(resources, R.drawable.drawable, null)
        val textView = view?.findViewById<TextView>(android.R.id.message)
        textView?.setTextColor(Color.WHITE)
        lastToast?.show()
    }

    /**
     * Verifica a permissão de localização e solicita se necessário.
     */
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

    /**
     * Exibe um diálogo explicando a necessidade da permissão de localização.
     */
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
                startActivityForResult(intent, requestLocationSettings)
            },
            onNegativeClick = {
                showToast(getString(R.string.permission_denied))
            }
        )
    }

    /**
     * Manipula o resultado da atividade de configurações.
     *
     * @param requestCode O código da solicitação.
     * @param resultCode O código do resultado.
     * @param data Dados adicionais fornecidos pela atividade.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestLocationSettings) {
            // Verifica se a permissão foi concedida e o GPS está ativado
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            ) {
                obtainLocation()
            } else {
                showToast("Localização não disponível")
            }
        }
    }

    /**
     * Manipula o resultado das solicitações de permissão.
     *
     * @param requestCode O código da solicitação.
     * @param permissions As permissões solicitadas.
     * @param grantResults Os resultados das permissões solicitadas.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestLocationPermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtainLocation()
            } else {
                showPermissionExplanationDialog()
            }
        }
    }
}