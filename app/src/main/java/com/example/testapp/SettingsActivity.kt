package com.example.testapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            saveConnectionDetails()
        }
    }

    private fun saveConnectionDetails() {
        val controllerDomain = findViewById<EditText>(R.id.etControllerDomain).text.toString()
        val authDomain = findViewById<EditText>(R.id.etAuthDomain).text.toString()
        val site = findViewById<EditText>(R.id.etSite).text.toString()
        val apiPassword = findViewById<EditText>(R.id.etApiPassword).text.toString()

        val sharedPreferences = getSharedPreferences("ConnectionSettings", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("CONTROLLER_DOMAIN", controllerDomain)
        editor.putString("AUTH_DOMAIN", authDomain)
        editor.putString("SITE", site)
        editor.putString("API_PASSWORD", apiPassword)
        editor.apply()
    }
}