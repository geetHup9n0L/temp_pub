package com.example.insecurebank;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "LocalDatabaseHelper";
    private static final String DATABASE_NAME = "BankTransactions.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TRANSFERS = "transfers";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_RECIPIENT = "recipient";
    public static final String COLUMN_AMOUNT = "amount";

    public LocalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_TRANSFERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_RECIPIENT + " TEXT, "
                + COLUMN_AMOUNT + " REAL)";
        db.execSQL(CREATE_TABLE);
        Log.d(TAG, "Database created: " + TABLE_TRANSFERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSFERS);
        onCreate(db);
    }

    public void recordTransfer(String recipient, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RECIPIENT, recipient);
        values.put(COLUMN_AMOUNT, amount);
        db.insert(TABLE_TRANSFERS, null, values);
        db.close();
    }

    // MASVS-STORAGE-2 / OWASP SQL Injection: Raw SQL query utilizing raw string concatenation instead of parameterized inputs.
    // An attacker sending SQL syntax in the "recipient" parameter can query/alter arbitrary database components.
    public Cursor queryLogsRaw(String filterRecipient) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_TRANSFERS;
        if (filterRecipient != null && !filterRecipient.isEmpty()) {
            query += " WHERE " + COLUMN_RECIPIENT + " = '" + filterRecipient + "'";
        }
        
        Log.d(TAG, "Executing raw query: " + query);
        return db.rawQuery(query, null);
    }
}
