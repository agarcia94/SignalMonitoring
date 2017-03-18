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
import android.widget.Toast;

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

    class DeclineTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... args){
            HttpURLConnection client = null;
            Integer[] progress = new Integer[1];
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
                progress[0] = 0;
                publishProgress(progress);
                StringBuilder sb = new StringBuilder();
                int HttpResult = client.getResponseCode();
                if (HttpResult == 204) {
                    Log.d("decline", sb.toString());

                    System.out.println("" + sb.toString());
                } else {
                    Log.d("decline", HttpResult+ "");
                    progress[0] = 1;
                    publishProgress(progress);
                    System.out.println("Server response: " + client.getResponseMessage());
                }
            }catch(Exception o) {
                o.printStackTrace();
                progress[0] = 3;
                publishProgress(progress);
            }finally {
                if(client != null) // Make sure the connection is not null.
                    client.disconnect();
            }
            progress[0] = 2;
            publishProgress(progress);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(values[0] == 0){
                Toast.makeText(Survey.this, "Declined! Notifying other technicians.", Toast.LENGTH_SHORT).show();
            }else if(values[0] == 1){
                Toast.makeText(Survey.this, "HTTP response error.", Toast.LENGTH_SHORT).show();
            }else if(values[0] == 2){
                Toast.makeText(Survey.this, "Alarm declined.", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(Survey.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
            }
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
