package com.example.currentplacedetailsonmap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;


public class loading extends AppCompatActivity {

//    private int loading=3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new Handler().postDelayed(() -> {
            // This method will be executed once the timer is over
            Intent i = new Intent(loading.this, MapsActivity.class);
            startActivity(i);
            finish();
        }, 3000);


    }
}
