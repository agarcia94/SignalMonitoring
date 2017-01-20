package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ReportsPastData extends AppCompatActivity {
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;

    String userProfile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_past_data);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        userProfile = getIntent().getStringExtra("profile");

        mDrawerList = (ListView)findViewById(R.id.navList);

        addDrawerItems();
    }

    private void addDrawerItems() {
        String[] array = { "Home","Reports","Logout" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter.getItem(position).equals("Home")) {
                    Intent i = new Intent(getApplicationContext(), Anomaly.class);
                    userProfile = getIntent().getStringExtra("profile");
                    i.putExtra("profile", userProfile);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("Reports")) {
                    Intent i = new Intent(getApplicationContext(), Reports.class);
                    userProfile = getIntent().getStringExtra("profile");
                    i.putExtra("profile", userProfile);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("Logout")) {
                    Toast.makeText(ReportsPastData.this, "Logged out", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), Login.class);
                    SaveSharedPreference.clearUserName(ReportsPastData.this);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else {
                    Toast.makeText(ReportsPastData.this, "Error On Calling", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
