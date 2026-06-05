package com.example.insecurebank;

import android.util.Base64;
import android.util.Log;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtility {
    private static final String TAG = "CryptoUtility";
    
    // MASVS-CRYPTO-1: Hardcoded static encryption key. Easily extracted via reverse-engineering (dex2jar/jadx).
    private static final String STATIC_KEY = "BankKey1"; // 8-byte key for DES

    // MASVS-CRYPTO-3: Use of outdated, weak encryption algorithm (DES) and insecure block mode (ECB).
    // ECB mode does not use an Initialization Vector (IV), meaning identical plaintext blocks produce identical ciphertext blocks.
    private static final String ALGORITHM = "DES/ECB/PKCS5Padding";

    public static String encrypt(String plaintext) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(STATIC_KEY.getBytes("UTF-8"), "DES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));
            
            String ciphertext = Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim();
            
            // MASVS-CODE-2: Leakage of sensitive cryptographic input/output to system logs.
            Log.d(TAG, "DEBUG: Encryption input: " + plaintext + " | Output: " + ciphertext);
            return ciphertext;
        } catch (Exception e) {
            Log.e(TAG, "Encryption error: ", e);
            return null;
        }
    }

    public static String decrypt(String ciphertext) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(STATIC_KEY.getBytes("UTF-8"), "DES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.decode(ciphertext, Base64.DEFAULT);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "Decryption error: ", e);
            return null;
        }
    }
}
