package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(SaveSharedPreference.getUserName(getApplicationContext()).length() > 0)
        {
            // call Anomaly Activity
            Intent i = new Intent(getApplicationContext(), Anomaly.class);
            startActivity(i);
        }
    }


}
