package com.example.alexperez.alarmnotifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.google.firebase.messaging.FirebaseMessaging;

public class SubscribeActivity extends AppCompatActivity {
    private Switch sw_testTopic = null;
    private Switch sw_location1 = null;
    private Switch sw_location2 = null;
    private FirebaseMessaging fcm = null;
    private Button btn_home = null;
    private String userProfile = "";
    private Context context = this.context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_home = (Button) findViewById(R.id.btn_home);

        sw_testTopic = (Switch) findViewById(R.id.sw_testTopic);
        sw_location1 = (Switch)findViewById(R.id.sw_location1);
        sw_location2 = (Switch) findViewById(R.id.sw_location2);
        fcm = FirebaseMessaging.getInstance();

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent((Activity)context, Anomaly.class);
                intent.putExtra("profile", userProfile.toString());
                startActivity(intent);
            }
        });

        sw_testTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_testTopic.isChecked()){
                    fcm.unsubscribeFromTopic("testTopic");
                }else{
                    fcm.subscribeToTopic("testTopic");
                }
            }
        });

        sw_location1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_location1.isChecked()){
                    fcm.unsubscribeFromTopic("location1");
                }else{
                    fcm.subscribeToTopic("location1");
                }
            }
        });

        sw_location2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_location2.isChecked()){
                    fcm.unsubscribeFromTopic("location2");
                }else{
                    fcm.subscribeToTopic("location2");
                }
            }
        });
    }

}
