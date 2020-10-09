package com.fauzan.ukmbudgetmanager.divuser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.dashboard.Dashboard2Activity;

public class UserType extends AppCompatActivity {
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type);

        final RadioGroup radio_usertype = findViewById(R.id.radio_usertype);
        Button btn_submit_user = findViewById(R.id.btn_submit_user);
        btn_submit_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = radio_usertype.getCheckedRadioButtonId();
                switch (id){
                    case R.id.radio_bendahara :
                        //tipe bendahara ke loginactivity

                        Intent intent = new Intent(UserType.this, LoginActivity.class );
                        intent.putExtra("user_bendahara",1);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.radio_anggota :
                        //tipe anggota ke dashboard2activity
                        Intent intent2 = new Intent(UserType.this, LoginActivity.class );
                        intent2.putExtra("user_ketua",2);
                        startActivity(intent2);
                        finish();
                        break;
                }
            }
        });

    }
}
