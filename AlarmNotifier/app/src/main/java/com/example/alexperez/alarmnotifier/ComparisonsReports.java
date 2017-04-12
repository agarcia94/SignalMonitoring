package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class ComparisonsReports extends AppCompatActivity {
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;

    String userProfile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparisons_reports);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mDrawerList = (ListView)findViewById(R.id.navList);

        addDrawerItems();
    }

    private void addDrawerItems() {
        final TypedArray typedArray = getResources().obtainTypedArray(R.array.sections_icons_comparison);
        String[] array = { "          Home","          Reports", "          Settings",
                "          Subscription", "          Logout" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array);
        mDrawerList.setAdapter(mAdapter);
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
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("          Reports")) {
                    Intent i = new Intent(getApplicationContext(), Reports.class);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("          Settings")) {
                    Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("          Logout")) {
                    Toast.makeText(ComparisonsReports.this, "Logged out", Toast.LENGTH_SHORT).show();
                    SaveSharedPreference.clearUserName(getApplicationContext());
                    Intent i = new Intent(getApplicationContext(), Login.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("          Subscription")) {
                    Intent i = new Intent(getApplicationContext(), SubscribeActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(ComparisonsReports.this, "Error On Calling", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
