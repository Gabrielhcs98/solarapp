package com.example.solarapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.solarapp.util.NetworkChecker

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextLocation = findViewById<EditText>(R.id.editTextLocation)
        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)

        val networkChecker = NetworkChecker(this)

        buttonSubmit.setOnClickListener {
            val location = editTextLocation.text.toString()
            val regex = Regex("^[A-Za-zÁÉÍÓÚÂÊÎÔÛÃÕÇáéíóúâêîôûãõç\\s]+$")
            val input = editTextLocation.text.toString()

            if (networkChecker.isNetworkQualityPoor()) {
                networkChecker.checkNetworkQuality()
                return@setOnClickListener
            }

            if (location.isEmpty() || !regex.matches(input)) {
                // Mostrar um dialog de erro
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.attention))
                builder.setMessage(getString(R.string.campo_invalido))
                builder.setPositiveButton("OK", null)
                val dialog = builder.create()
                val drawable = ResourcesCompat.getDrawable(resources, R.drawable.drawable, null)
                dialog.window?.setBackgroundDrawable(drawable)
                dialog.setOnShowListener {
                    val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton.setTextColor(Color.WHITE)
                }
                dialog.show()
            } else {
                // Enviar local digitado pelo usuário
                val intent = Intent(this, ResultsActivity::class.java)
                intent.putExtra("city", location)

                // Set the animation
                window.exitTransition = Slide(Gravity.END)
                startActivity(intent)
            }
        }
    }
}
