package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Reports extends AppCompatActivity {
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;

    String userProfile = "";
    Calendar calendar = Calendar.getInstance();
    int calender_year = calendar.get(Calendar.YEAR);
    //String current_year = Integer.toString(calender_year);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        userProfile = getIntent().getStringExtra("profile");

        mDrawerList = (ListView)findViewById(R.id.navList);

        addDrawerItems();

        // Get reference of widgets from XML layout
        final Spinner spinner = (Spinner)findViewById(R.id.base_Location);
        //final Spinner yearSpinner = (Spinner)findViewById(R.id.year);

        // Initializing a String Array
        String[] type = new String[]{
                "Base Location",
                "LABC",
                "LADF",
                "CBC",
                "CRBC",
                "CRDF",
                "ECUF",
                "MQUF",
                "MWDF",
                "NEUF",
                "NDUF",
                "NWUF",
                "NWDF",
                "SWUF",
                "SWDF"
        };

        final List<String> typeList = new ArrayList<>(Arrays.asList(type));
        //final List<String> yearList = new ArrayList<>(Arrays.asList(year));

        ArrayList<String> yearList = new ArrayList<String>();
        yearList.add("Year");
        for (int i = 1980; i <= calender_year; i++) {
            yearList.add(Integer.toString(i));
        }

        final ArrayAdapter<String> yearArrayAdapter = new ArrayAdapter<String>(this,R.layout.spinner_item,yearList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray, The first item is the placeholder
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    //Differentiate the two
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        yearArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        //assert yearSpinner != null; //Base Case?? Provided by Android
        //yearSpinner.setAdapter(yearArrayAdapter);

//        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selectedItemText = (String) parent.getItemAtPosition(position);
//                // If user change the default selection
//                // First item is disable and it is used for hint
//                if (position > 0) {
//                    // Notify the selected item text
//                    Toast.makeText
//                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
//                            .show();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                //Do Nothing, as nothing should happen if nothing is selected yet
//            }
//        });

        // Initializing the Base Location Adapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,R.layout.spinner_item,typeList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray, The first item is the placeholder
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    //Differentiate the two
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        assert spinner != null; //Base Case?? Provided by Android
        spinner.setAdapter(spinnerArrayAdapter);

//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selectedItemText = (String) parent.getItemAtPosition(position);
//                // If user change the default selection
//                // First item is disable and it is used for hint
//                if (position > 0) {
//                    // Notify the selected item text
//                    Toast.makeText
//                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
//                            .show();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

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
                    Toast.makeText(Reports.this, "Logged out", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), Login.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else {
                    Toast.makeText(Reports.this, "Error On Calling", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}