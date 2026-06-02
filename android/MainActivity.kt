package com.demolab

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val API_KEY = "AIzaSyA1_Vulnerable_Secret_Key_Example"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val usernameInput = findViewById<EditText>(R.id.username)
        val passwordInput = findViewById<EditText>(R.id.password)
        val saveButton = findViewById<Button>(R.id.save_button)

        saveButton.setOnClickListener {
            val user = usernameInput.text.toString()
            val pass = passwordInput.text.toString()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Triggering the vulnerable implementations
            saveToSharedPreferences(user, pass)
            saveToDatabase(user, pass)
            saveToExternalStorage(user, pass)

            // Triggering the secure implementation for comparison
            saveDataSecurely(this, pass)

            Toast.makeText(this, "Data Saved! Ready for testing.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToSharedPreferences(user: String, pass: String) {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("username", user)
            putString("password", pass)
            putString("api_key", API_KEY)
            apply()
        }
    }

    private fun saveToDatabase(user: String, pass: String) {
        val db: SQLiteDatabase = openOrCreateDatabase("UserDB", Context.MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS Users(Username VARCHAR, Password VARCHAR);")
        db.execSQL("INSERT INTO Users VALUES('$user', '$pass');")
        db.close()
    }

    private fun saveToExternalStorage(user: String, pass: String) {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val externalFile = File(getExternalFilesDir(null), "config_backup.txt")
            FileOutputStream(externalFile).use { fos ->
                fos.write("User: $user | Pass: $pass".toByteArray())
            }
        }
    }

    private fun saveDataSecurely(context: Context, pass: String) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val secureSharedPreferences = EncryptedSharedPreferences.create(
            "SecureSecretTokens",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        secureSharedPreferences.edit().apply {
            putString("secure_password", pass)
            apply()
        }
    }
}
