package com.msf.kbank.mobile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class Loading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        ImageView loader = findViewById(R.id.loading);

        TextView loadTextView = findViewById(R.id.loadText);
        loadTextView.setText(getIntent().getStringExtra("loadText"));

        Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(loader);

        try {

            String nextActivityClassName = getPackageName()+"."+getIntent().getStringExtra("nextActivity");
            int id = getIntent().getIntExtra("id", -1);
            assert nextActivityClassName != null;
            Class<?> nextActivityClass = Class.forName(nextActivityClassName);

            Intent intent = new Intent(Loading.this, nextActivityClass);
            intent.putExtra("id", id);

            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                    startActivity(intent);
                }
            },5000);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}
