package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

        switches.put("LABC", R.id.sw_LABC);
        switches.put("LADF", R.id.sw_LADF);
        switches.put("CBC", R.id.sw_CBC);
        switches.put("CRBC", R.id.sw_CRBC);
        switches.put("CRDF", R.id.sw_CRDF);
        switches.put("MWUF", R.id.sw_MWUF);
        switches.put("MWDF", R.id.sw_MWDF);
        switches.put("NEUF", R.id.sw_NEUF);
        switches.put("NDUF", R.id.sw_NDUF);
        switches.put("NWUF", R.id.sw_NWUF);
        switches.put("NWDF", R.id.sw_NWDF);
        switches.put("SWUF", R.id.sw_SWUF);
        switches.put("SWDF", R.id.sw_SWDF);

        btn_home = (Button) findViewById(R.id.btn_home);

        fcm = FirebaseMessaging.getInstance();

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Anomaly.class);
                startActivity(intent);
            }
        });

        for(Map.Entry<String,Integer> entry: switches.entrySet()){
            final String key = entry.getKey();
            final Integer value = entry.getValue();
            final Switch currSwitch = (Switch) findViewById(value);
            currSwitch.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if (currSwitch.isChecked()){
                        fcm.subscribeToTopic(key);
                        SaveSharedPreference.subscribingTo(SubscribeActivity.this, key);
                    }else{
                        fcm.unsubscribeFromTopic(key);
                        SaveSharedPreference.unsubscribingTo(SubscribeActivity.this, key);
                    }
                }
            });
        }

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
