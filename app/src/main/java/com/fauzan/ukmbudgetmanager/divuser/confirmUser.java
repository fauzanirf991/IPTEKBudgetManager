package com.fauzan.ukmbudgetmanager.divuser;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.dashboard.Dashboard1Activity;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class confirmUser extends AppCompatActivity {
    ProgressDialog loading;
    BaseApiService mApiService;
    Intent intent;
    Context mContext;
    EditText txt_username;
    Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_user);
        mContext = this;
        mApiService = UtilsApi.getAPIService();
        initComponent();
    }

    private void initComponent(){
        txt_username = findViewById(R.id.txt_username);
        nextBtn = findViewById(R.id.next_btn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading = ProgressDialog.show(mContext, null, "Sedang Mencari Username...", true, false);
                checkUsername();
            }
        });
    }

    private void checkUsername(){
        mApiService.checkUsername(txt_username.getText().toString())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()){
                            loading.dismiss();
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if(jsonRESULTS.getString("error").equals("false")){
                                    Toast.makeText(mContext, "Username Ditemukan", Toast.LENGTH_SHORT).show();
                                    String username = jsonRESULTS.getJSONObject("user").getString("username");
                                    Intent intent = new Intent(mContext, changePassword.class);
                                    intent.putExtra("result_username", username);
                                    startActivity(intent);
                                } else{
                                    String error_message = jsonRESULTS.getString("error_msg");
                                    Toast.makeText(mContext, error_message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            loading.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                        loading.dismiss();
                    }
                });
    }
}
