# Android Java Integration Guide - Connecting Mobile Apps to Java REST API Backend

This guide explains step-by-step how to make HTTP GET and POST requests from an Android mobile application written in Java to your backend server.

---

## 🔑 Key Concepts for Android Networking in Java

### 1. The Main UI Thread Constraint (`NetworkOnMainThreadException`)
Android prohibits network operations on the main thread (UI thread) to keep the app responsive. If you attempt an HTTP request on the main thread, Android will throw `android.os.NetworkOnMainThreadException`.
- **Solution**: Execute all network requests inside a background thread (using `ExecutorService` or `AsyncTask`/`Coroutines`), then switch back to the main thread (using `Handler(Looper.getMainLooper())` or `runOnUiThread()`) to update text views or lists.

### 2. Android IP Address Rules for Local Backend Servers
When testing locally:
- **Android Emulator**: Use `http://10.0.2.2:8080/api/items`.
  > `10.0.2.2` is a special alias provided by the Android Emulator to access `localhost` (`127.0.0.1`) on your host development computer.
- **Physical Android Phone**: Use your computer's local Wi-Fi IP address (e.g., `http://192.168.1.150:8080/api/items`).
  > Both your computer and phone must be connected to the exact same Wi-Fi network.

---

## 🛠️ Step 1: Configure `AndroidManifest.xml`

Open your Android app's `app/src/main/AndroidManifest.xml` and make two additions:

1. Add the **INTERNET permission** outside the `<application>` tag:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```

2. Enable **Cleartext (HTTP) Traffic** inside the `<application>` tag (since local dev servers use `http://` instead of `https://`):
   ```xml
   <application
       android:allowBackup="true"
       android:icon="@mipmap/ic_launcher"
       android:label="@string/app_name"
       android:usesCleartextTraffic="true"
       android:theme="@style/Theme.MyApp">
       
       <activity android:name=".MainActivity" android:exported="true">
           <intent-filter>
               <action android:name="android.intent.action.MAIN" />
               <category android:name="android.intent.category.LAUNCHER" />
           </intent-filter>
       </activity>
   </application>
   ```

---

## 💻 Step 2: Android Java Code Examples

### Option A: Standard Native Java (`HttpURLConnection`) - No External Libraries Needed

This approach uses only standard Java & Android built-in classes (`HttpURLConnection`, `ExecutorService`, `Handler`, `org.json.JSONArray`, `org.json.JSONObject`).

#### 1. Fetching Data from Backend (`GET /api/items`)

```java
package com.example.myandroidapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    
    // Use 10.0.2.2 for Android Emulator, or your local Wi-Fi IP for physical devices
    private static final String BACKEND_URL = "http://10.0.2.2:8080/api/items";

    private TextView statusTextView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);

        // Fetch data from local backend server
        fetchItemsFromBackend();
    }

    private void fetchItemsFromBackend() {
        // Execute network request on a background thread
        executor.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(BACKEND_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);

                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "GET Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    String jsonResponse = response.toString();
                    Log.d(TAG, "JSON Response: " + jsonResponse);

                    // Parse JSON Array using Android org.json library
                    JSONArray jsonArray = new JSONArray(jsonResponse);
                    StringBuilder displayBuilder = new StringBuilder("Items from Backend:\n\n");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject itemObj = jsonArray.getJSONObject(i);
                        int id = itemObj.getInt("id");
                        String name = itemObj.getString("name");
                        double price = itemObj.getDouble("price");

                        displayBuilder.append("#").append(id).append(" - ")
                                      .append(name).append(" ($").append(price).append(")\n");
                    }

                    String finalResult = displayBuilder.toString();

                    // Switch to Main UI Thread to update screen
                    mainHandler.post(() -> statusTextView.setText(finalResult));

                } else {
                    mainHandler.post(() -> statusTextView.setText("Failed! Server Code: " + responseCode));
                }

            } catch (Exception e) {
                Log.e(TAG, "Network Error", e);
                mainHandler.post(() -> statusTextView.setText("Network Error: " + e.getMessage()));
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }
}
```

---

#### 2. Sending New Data to Backend (`POST /api/items`)

```java
private void createNewItem(String name, String category, double price, boolean inStock) {
    executor.execute(() -> {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(BACKEND_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);

            // Construct JSON request body
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("name", name);
            jsonParam.put("category", category);
            jsonParam.put("price", price);
            jsonParam.put("inStock", inStock);

            // Write JSON bytes to output stream
            byte[] input = jsonParam.toString().getBytes("utf-8");
            urlConnection.getOutputStream().write(input, 0, input.length);

            int responseCode = urlConnection.getResponseCode();
            Log.d(TAG, "POST Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = in.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String responseStr = response.toString();
                mainHandler.post(() -> {
                    statusTextView.setText("Success! Created Item:\n" + responseStr);
                });
            } else {
                mainHandler.post(() -> statusTextView.setText("POST Failed: " + responseCode));
            }

        } catch (Exception e) {
            Log.e(TAG, "POST Request Error", e);
            mainHandler.post(() -> statusTextView.setText("Error: " + e.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    });
}
```

---

### Option B: Modern Android Java with OkHttp Library (Popular Industry Standard)

If you add `implementation 'com.squareup.okhttp3:okhttp:4.12.0'` to your Android `build.gradle`, networking becomes significantly simpler:

#### 1. OkHttp GET Request
```java
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

OkHttpClient client = new OkHttpClient();

Request request = new Request.Builder()
        .url("http://10.0.2.2:8080/api/items")
        .build();

client.newCall(request).enqueue(new Callback() {
    @Override
    public void onFailure(Call call, IOException e) {
        Log.e("OkHttp", "Request failed", e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            String jsonResponse = response.body().string();
            Log.d("OkHttp", "Response: " + jsonResponse);
            
            // Update UI on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                // Update views
            });
        }
    }
});
```

#### 2. OkHttp POST Request
```java
import okhttp3.MediaType;
import okhttp3.RequestBody;

MediaType JSON = MediaType.get("application/json; charset=utf-8");
String jsonString = "{\"name\":\"Wireless Mouse\",\"category\":\"Electronics\",\"price\":29.99,\"inStock\":true}";

RequestBody body = RequestBody.create(jsonString, JSON);
Request request = new Request.Builder()
        .url("http://10.0.2.2:8080/api/items")
        .post(body)
        .build();

client.newCall(request).enqueue(new Callback() {
    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            String result = response.body().string();
            Log.d("OkHttp", "Created item: " + result);
        }
    }
});
```

---

## 🔍 Troubleshooting Android Connection Issues

1. **`java.net.ConnectException: Connection refused`**:
   - Make sure your Java backend server is running (`java -cp bin com.example.backend.Main.Main`).
   - Check if you used `http://10.0.2.2:8080` for Android Emulator (do NOT use `http://localhost:8080` inside Android code).

2. **`java.io.IOException: Cleartext HTTP traffic to 10.0.2.2 not permitted`**:
   - Ensure `android:usesCleartextTraffic="true"` is added to `AndroidManifest.xml`.

3. **`android.os.NetworkOnMainThreadException`**:
   - Ensure network calls run inside `executor.execute(...)` or OkHttp asynchronous `enqueue(...)`.
