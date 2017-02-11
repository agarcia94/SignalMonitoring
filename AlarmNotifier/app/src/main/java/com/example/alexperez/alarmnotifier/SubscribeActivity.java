package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
    private ArrayAdapter<String> mAdapter;
    private String userProfile = "";
    private HashMap<String, Integer> switches = new HashMap<String, Integer>();
    private ListView mDrawerList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);
        mDrawerList = (ListView)findViewById(R.id.navList);
        addDrawerItems();
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
        switches.put("ECUF", R.id.sw_ECUF);
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

    private void addDrawerItems() {
        final TypedArray typedArray = getResources().obtainTypedArray(R.array.sections_icons_detail);
        String[] array = { "          Home","          Reports",
                "          Subscription","          Comparison Reports",
                "          Logout" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array);
        mDrawerList.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                array
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                int resourceId = typedArray.getResourceId(position, 0);
                Drawable drawable = getResources().getDrawable(resourceId);
                ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                return v;
            }
        });


        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter.getItem(position).equals("          Home")) {
                    Intent i = new Intent(getApplicationContext(), Anomaly.class);
                    userProfile = getIntent().getStringExtra("profile");
                    i.putExtra("profile", userProfile);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("          Reports")) {
                    Intent i = new Intent(getApplicationContext(), Reports.class);
                    userProfile = getIntent().getStringExtra("profile");
                    i.putExtra("profile", userProfile);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("          Logout")) {
                    Toast.makeText(SubscribeActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                    SaveSharedPreference.clearUserName(getApplicationContext());
                    Intent i = new Intent(getApplicationContext(), Login.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("          Subscription")){
                    Intent i = new Intent(getApplicationContext(), Reports.class);
                    startActivity(i);
                }else if (mAdapter.getItem(position).equals("          Comparison Reports")){
                    Intent i = new Intent(getApplicationContext(), ComparisonsReports.class);
                    userProfile = getIntent().getStringExtra("profile");
                    i.putExtra("profile", userProfile);
                    startActivity(i);
                }
                else {
                    Toast.makeText(SubscribeActivity.this, "Error On Calling", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
