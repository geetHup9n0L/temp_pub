package com.example.insecurebank;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Prepopulate a dummy credential on first run for validation/test ease
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        if (!sharedPref.contains("admin")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            // MASVS-STORAGE-1: Sensitive data (passwords) stored in plaintext SharedPreferences.
            editor.putString("admin", "P@ssword123");
            editor.apply();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // MASVS-CODE-2: Sensitive authentication events and raw passwords printed directly to Android logcat
                Log.d(TAG, "Login attempt by user: " + username + " with password: " + password);

                String storedPassword = sharedPref.getString(username, null);

                if (storedPassword != null && storedPassword.equals(password)) {
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    // Generate a pseudo-session token using weak crypto utility
                    String sessionToken = CryptoUtility.encrypt(username + ":" + System.currentTimeMillis());
                    
                    // MASVS-CODE-2: Leaking the session token to logs
                    Log.d(TAG, "Successfully authenticated. Generated Session Token: " + sessionToken);

                    // Navigate to transfer screen
                    Intent intent = new Intent(LoginActivity.this, TransferActivity.class);
                    intent.putExtra("session_token", sessionToken);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Failed login attempt for user: " + username);
                }
            }
        });
    }
}
