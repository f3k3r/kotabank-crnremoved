package com.msf.kbank.mobile.Suggest;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.msf.kbank.mobile.Helper;

public class NetworkUtils {

    public interface PostRequestCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public static void postRequest(String urlString, JSONObject jsonData, PostRequestCallback callback) {
        new AsyncTask<String, Void, String>() {
            private String errorMessage = null;

            @Override
            protected String doInBackground(String... params) {
                String response = "";
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    // Write JSON data to the output stream
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(jsonData.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    // Check the response code
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read response
                        Scanner scanner = new Scanner(conn.getInputStream());
                        StringBuilder responseBuilder = new StringBuilder();
                        while (scanner.hasNextLine()) {
                            responseBuilder.append(scanner.nextLine());
                        }
                        scanner.close();
                        response = responseBuilder.toString();
                        Helper.debug(response);
                    } else {
                        errorMessage = "Response Code: " + responseCode;
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    errorMessage = "Response Error: " + e.getMessage();
                }
                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                if (errorMessage != null) {
                    callback.onError(errorMessage);
                } else {
                    callback.onResponse(result);
                }
            }
        }.execute();
    }
}
