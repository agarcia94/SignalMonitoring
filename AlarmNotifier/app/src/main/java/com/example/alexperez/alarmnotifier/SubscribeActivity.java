package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SubscribeActivity extends AppCompatActivity {
    private Switch sw_testTopic = null;
    private Switch sw_location1 = null;
    private Switch sw_location2 = null;
    private FirebaseMessaging fcm = null;
    private Button btn_home = null;
    private String userProfile = "";
    private HashMap<String, Integer> switches = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);
        userProfile = SaveSharedPreference.getUserName(SubscribeActivity.this);
        String location = "";
        try {
            JSONObject userInfo = new JSONObject(userProfile);
            location = userInfo.getString("location");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        switches.put("testTopic", R.id.sw_testTopic);
        switches.put("location1", R.id.sw_location1);
        switches.put("LABC", R.id.LABC);

        btn_home = (Button) findViewById(R.id.btn_home);

        sw_testTopic = (Switch) findViewById(R.id.sw_testTopic);
        sw_location1 = (Switch)findViewById(R.id.sw_location1);
        sw_location2 = (Switch) findViewById(R.id.LABC);
        fcm = FirebaseMessaging.getInstance();

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Anomaly.class);
                startActivity(intent);
            }
        });



        sw_testTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_testTopic.isChecked()) {
                    fcm.subscribeToTopic("testTopic");
                    SaveSharedPreference.subscribingTo(SubscribeActivity.this, "testTopic");
                } else {
                    Log.d("ssp", "am i ever here");
                    fcm.unsubscribeFromTopic("testTopic");
                    SaveSharedPreference.unsubscribingTo(SubscribeActivity.this, "testTopic");
                }
            }
        });

        sw_location1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_location1.isChecked()) {
                    fcm.subscribeToTopic("location1");
                    SaveSharedPreference.subscribingTo(SubscribeActivity.this, "location1");
                } else {
                    fcm.unsubscribeFromTopic("location1");
                    SaveSharedPreference.unsubscribingTo(SubscribeActivity.this, "location1");
                }
            }
        });

        sw_location2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_location2.isChecked()) {
                    fcm.subscribeToTopic("LABC");
                    SaveSharedPreference.subscribingTo(SubscribeActivity.this, "LABC");
                } else {
                    fcm.unsubscribeFromTopic("LABC");
                    SaveSharedPreference.unsubscribingTo(SubscribeActivity.this, "LABC");
                }
            }
        });

        JSONArray subs;
        try {
            JSONObject userInfo = new JSONObject(userProfile);
            subs = userInfo.getJSONArray("subs");
            Switch responsibleSwitch = (Switch) findViewById(switches.get(location));
            responsibleSwitch.setOnClickListener(null);
            responsibleSwitch.setClickable(false);
            for(int i = 0; i < subs.length(); i++){
                Switch currentSwitch = (Switch) findViewById(switches.get(subs.getString(i)));
                currentSwitch.setChecked(true);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        userProfile = SaveSharedPreference.getUserName(SubscribeActivity.this);
        String location = "";
        try {
            JSONObject userInfo = new JSONObject(userProfile);
            location = userInfo.getString("location");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray subs;
        try {
            JSONObject userInfo = new JSONObject(userProfile);
            subs = userInfo.getJSONArray("subs");
            Switch currentSwitch = (Switch) findViewById(switches.get(location));
            currentSwitch.setClickable(false);
            currentSwitch.setOnClickListener(null);
            for(int i = 0; i < subs.length(); i++){
                currentSwitch = (Switch) findViewById(switches.get(subs.getString(i)));
                currentSwitch.setChecked(true);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
