package com.msf.kbank.mobile;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.last_layout);
        TextView queryNumber = findViewById(R.id.query_number);
        int number = getIntent().getIntExtra("id", -1);
        queryNumber.setText(String.valueOf(number));
    }

}
