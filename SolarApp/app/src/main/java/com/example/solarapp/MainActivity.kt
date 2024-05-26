package com.example.solarapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextLocation = findViewById<EditText>(R.id.editTextLocation)
        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)

        buttonSubmit.setOnClickListener {
            val location = editTextLocation.text.toString()
            val regex = Regex("^[A-Za-zÁÉÍÓÚÂÊÎÔÛÃÕÇáéíóúâêîôûãõç]+$")

            val input = editTextLocation.text.toString()

            if (location.isEmpty() || !regex.matches(input)) {
                // Mostrar um dialog de erro
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.attention)
                builder.setMessage(R.string.campo_invalido)
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
                startActivity(intent)
            }
        }
    }
}
