package com.example.insecurebank;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TransferActivity extends AppCompatActivity {
    private static final String TAG = "TransferActivity";
    private EditText etTargetAccount, etAmount;
    private Button btnTransfer;
    private TextView tvLogs;
    private LocalDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        etTargetAccount = findViewById(R.id.etTargetAccount);
        etAmount = findViewById(R.id.etAmount);
        btnTransfer = findViewById(R.id.btnTransfer);
        tvLogs = findViewById(R.id.tvLogs);
        dbHelper = new LocalDatabaseHelper(this);

        // Check if session token exists
        String sessionToken = getIntent().getStringExtra("session_token");
        
        // MASVS-PLATFORM-1: Insecure Intent Handling.
        // Because the activity is exported in the Manifest, third-party apps can start it via intent.
        // If the intent contains extras, we process it without validating the caller identity or authenticity of token parameters.
        if (getIntent().hasExtra("recipient") && getIntent().hasExtra("amount")) {
            String directRecipient = getIntent().getStringExtra("recipient");
            String directAmount = getIntent().getStringExtra("amount");
            
            Log.w(TAG, "WARNING: Transfer triggered externally via exported IPC intent! Recipient: " 
                    + directRecipient + " | Amount: " + directAmount);
            
            executeFundTransfer(directRecipient, directAmount);
        }

        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipient = etTargetAccount.getText().toString().trim();
                String amount = etAmount.getText().toString().trim();
                executeFundTransfer(recipient, amount);
            }
        });

        refreshTransactionLogs(null);
    }

    private void executeFundTransfer(String recipient, String amountStr) {
        if (recipient.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter recipient and amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            dbHelper.recordTransfer(recipient, amount);
            Toast.makeText(this, "Transfer of $" + amount + " successful!", Toast.LENGTH_LONG).show();
            
            // MASVS-CODE-2: Leak transaction details to public logcat
            Log.d(TAG, "Transaction Confirmed: Sent $" + amount + " to account: " + recipient);
            
            // Check for potential SQL injection syntax in the recipient field to display in the UI logs
            refreshTransactionLogs(recipient);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Transaction Parse Error: ", e);
        }
    }

    private void refreshTransactionLogs(String filterRecipient) {
        StringBuilder logBuilder = new StringBuilder();
        try {
            // Trigger raw SQL Query to populate transaction list
            Cursor cursor = dbHelper.queryLogsRaw(filterRecipient);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String rec = cursor.getString(1);
                    double amt = cursor.getDouble(2);
                    logBuilder.append("ID: ").append(id)
                              .append(" | To: ").append(rec)
                              .append(" | Amt: $").append(amt)
                              .append("\n");
                } while (cursor.moveToNext());
                cursor.close();
            } else {
                logBuilder.append("No matching records found.");
            }
        } catch (Exception e) {
            // If raw injection fails or prints stack trace, display on screen
            logBuilder.append("SQL Error: ").append(e.getMessage());
            Log.e(TAG, "Database Query Error: ", e);
        }
        tvLogs.setText(logBuilder.toString());
    }
}
