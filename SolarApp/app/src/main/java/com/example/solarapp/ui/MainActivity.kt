package com.example.solarapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import android.transition.Slide
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.solarapp.R
import com.example.solarapp.data.WeatherRepository
import com.example.solarapp.data.WeatherServiceImpl
import com.example.solarapp.util.DialogUtils
import com.example.solarapp.util.NetworkChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

/**
 * MainActivity é a atividade principal do aplicativo SolarApp.
 * Esta atividade lida com a obtenção da localização do usuário,
 * validação de entrada e navegação para outra atividade para exibir resultados.
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    val service = WeatherServiceImpl()
                    val repo = WeatherRepository(service, Dispatchers.IO)
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(repo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkFinalLocationStatus()
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var editTextLocation: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonHere: Button
    private val requestLocationPermission = 1
    private lateinit var networkChecker: NetworkChecker

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

        networkChecker.checkNetworkQuality()

        setupObservers()

        buttonSubmit.setOnClickListener {
            val location = editTextLocation.text.toString().trim()
            val apiKey = getString(R.string.api_key)

            viewModel.validateAndSubmit(location, apiKey)
        }

        buttonHere.setOnClickListener {
            handleHereClick()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is MainUiState.Loading -> {
                            buttonSubmit.isEnabled = false
                            showToast("Carregando...")
                        }
                        is MainUiState.Error -> {
                            buttonSubmit.isEnabled = true
                            showAlertDialog(state.message)
                        }
                        is MainUiState.Idle -> {
                            buttonSubmit.isEnabled = true
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { city ->
                    navigateToResultsActivity(city)
                }
            }
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
                locationSettingsLauncher.launch(intent)
            },
            onNegativeClick = {
            }
        )
    }

    private fun checkFinalLocationStatus() {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                handleGeocodeResult(addresses.toList())
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)?.toList() ?: emptyList()
                handleGeocodeResult(addresses)
            } catch (e: IOException) {
                showToast("Erro na geocodificação: ${e.message}")
            }
        }
    }

    private fun handleGeocodeResult(addresses: List<android.location.Address>?) {
        runOnUiThread {
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val neighborhoodName = address.subLocality
                val cityName = address.locality
                val apiKey = getString(R.string.api_key)

                viewModel.searchByCoordinates(neighborhoodName, cityName, apiKey)
            } else {
                showToast("Endereço não encontrado")
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
        lastToast?.cancel()

        lastToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
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
                locationSettingsLauncher.launch(intent)
            },
            onNegativeClick = {
                showToast(getString(R.string.permission_denied))
            }
        )
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
