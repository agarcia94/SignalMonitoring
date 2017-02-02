package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Reports extends AppCompatActivity {
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private String reportType = "";
    private String IP_ADDRESS = "192.168.43.253";

    private String userProfile = "";

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
        final Spinner spinner = (Spinner)findViewById(R.id.typeOfField);

        // Initializing a String Array
        String[] type = new String[]{
                "Select an item...",
                "Year",
                "Vendor",
                "Base Location",
        };

        final List<String> typeList = new ArrayList<>(Arrays.asList(type));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,R.layout.spinner_item,typeList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
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

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reportType = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text

                    Toast.makeText
                            (getApplicationContext(), "Field Selected : " + reportType, Toast.LENGTH_SHORT)
                            .show();


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ImageButton searchButton = (ImageButton)findViewById(R.id.searchButton);
        if (searchButton != null) {
            searchButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    EditText answerOfIA = (EditText)findViewById(R.id.answerOfIA);
                    if(!answerOfIA.getText().toString().isEmpty()){
                        String answer = answerOfIA.getText().toString();

                        String[] reportFields = {reportType, answer};
                        SendReportType sendReport = new SendReportType();
                        sendReport.execute(reportFields);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "No field selected", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

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

    class SendReportType extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... args){
            Log.d("enter", "entered function");
            HttpURLConnection client = null;

            String reportTypeSelection = args[0];
            String reportTypeValue = args[1];

            try{
                JSONObject reportObject = new JSONObject();
                if(reportTypeSelection.equals("Base Location")){
                    reportObject.put("location", reportTypeValue);
                }
                else if(reportTypeSelection.equals("Vendor")){
                    reportObject.put("vendor",reportTypeValue);
                }

                Log.d("reportObject", reportObject.toString());

                URL url = new URL("http://" + IP_ADDRESS + ":8080/UserManagement/MongoService/report");

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                Log.d("profile", reportObject.toString());
                wr.write(reportObject.toString());
                wr.flush();
                wr.close();

                Log.d("output", "output stream");

                StringBuilder sb = new StringBuilder();
                int HttpResult = client.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(client.getInputStream()));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    Log.d("reportResponse", sb.toString());

                    System.out.println("" + sb.toString());


                } else {
                    Log.d("hello", client.getResponseMessage());
                    System.out.println("Server response: " + client.getResponseMessage());
                }


            }catch(Exception o) {
                o.printStackTrace();
            }finally {
                if(client != null) // Make sure the connection is not null.
                    client.disconnect();
            }

            return null;
        }
    }

}
