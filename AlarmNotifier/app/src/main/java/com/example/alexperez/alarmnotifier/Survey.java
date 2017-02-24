package com.example.alexperez.alarmnotifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Survey extends AppCompatActivity {
    private String userProfile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /*
    Handler For back button on Survey, If Tech presses back it will demand for an answer if they
    really want to quit this ACK
     */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Name Of Alarm");
        builder.setMessage("Would you Like To Decline Responsibility? ");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Contact server to toggle the requiresAcknowledgment
                DeclineTask dt = new DeclineTask();
                dt.execute();
                // Server code here ^^^^^^^
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do nothing
            }
        });
        builder.show();

    }

    class DeclineTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args){
            HttpURLConnection client = null;
            String oid = getIntent().getStringExtra("alarmOid");

            try{
                //URL url = new URL("http://192.168.43.253:8080/UserManagement/MongoService/login");
                URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/decline");

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                String query = "{alarm:" + oid + "}";
                wr.write(query);
                wr.flush();
                wr.close();

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
                    Log.d("decline", sb.toString());

                    System.out.println("" + sb.toString());

                } else {
                    Log.d("decline", client.getResponseMessage());
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Put intent onPostExecute.
            Intent i = new Intent(getApplicationContext(), Anomaly.class);
            startActivity(i);
        }
    }



}
