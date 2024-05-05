package com.example.solarapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextLocation = findViewById<EditText>(R.id.editTextLocation)
        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)

        buttonSubmit.setOnClickListener {
            val location = editTextLocation.text.toString()
            val intent = Intent(this, ResultsActivity::class.java)
            intent.putExtra("city", location)
            startActivity(intent)
        }
    }
}
