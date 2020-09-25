package com.fauzan.ukmbudgetmanager.divuser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.fauzan.ukmbudgetmanager.R;

public class splashScreen extends AppCompatActivity {
    private int waktu_loading=5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //setelah loading maka akan langsung berpindah ke home activity
                Intent home=new Intent(splashScreen.this, UserType.class);
                startActivity(home);
                finish();

            }
        },waktu_loading);

    }
}
