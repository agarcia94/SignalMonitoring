package com.example.alexperez.alarmnotifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class DetailsFragment extends Fragment {
    private ArrayList<String> results = new ArrayList<String>();
    private ListView pastAnomListView;
    private ArrayAdapter<String> simPastAnomaliesAdap;
    private JSONObject alarmACK = new JSONObject();
    private JSONArray reportMatches;
    private String userProfile = "";
    private String alarmID = "";
    private JSONObject testAlarmJSON = null;
    private AlertDialog.Builder builder = null;
    private String detailString = null;
    private Button accept;


    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        builder = new AlertDialog.Builder(getActivity());
        accept = (Button)rootView.findViewById(R.id.accept);
        //TEST PURPOSE FOR LISTVIEW ON DETAILS
//        pastAnomListView = (ListView)rootView.findViewById(R.id.pastAnomListView);
//        results.add(0,"Hello");
//        results.add(1,"World");
//        simPastAnomaliesAdap = new ArrayAdapter(getActivity(), R.layout.past_anomalies_custom, R.id.textView, results);
//        pastAnomListView.setAdapter(simPastAnomaliesAdap);

        Intent intent = getActivity().getIntent();
        String alarmJSON = intent.getStringExtra("alarm");
        Log.d("alarm", alarmJSON);

        try {
            JSONObject alarmObject = new JSONObject(alarmJSON);

            alarmID = alarmObject.getJSONObject("_id").getString("$oid");
            int severity = alarmObject.getInt("severity");
            String parameter = alarmObject.getString("parameter");

            String[] parameterFields = parameter.split("-");

            String site = parameterFields[1];
            String antenna = parameterFields[2];

            String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
            String fault = anomalyNameArray[1];
            int nameIndex = fault.indexOf("(");
            boolean requiresAcknowledgement = alarmObject.getBoolean("requiresAcknowledgment");
            if(!requiresAcknowledgement) {
                accept.setEnabled(false);
                accept.setOnClickListener(null);
                accept.setBackgroundColor(Color.GRAY);}

            if(nameIndex != -1){
                fault = fault.substring(0,nameIndex);
            }

            JSONObject deviceAttributes = alarmObject.getJSONObject("deviceAttributes");
            String affectEquipment = deviceAttributes.getString("DeviceId");
            String vendor = deviceAttributes.getString("DeviceType");
            vendor = vendor.substring(0, vendor.indexOf(" "));

            TextView siteView = (TextView)rootView.findViewById(R.id.site);
            siteView.setText(siteView.getText() + " " + site);

            TextView antennaView = (TextView)rootView.findViewById(R.id.antenna);
            antennaView.setText(antennaView.getText() + " " + antenna);

            TextView alarmView = (TextView)rootView.findViewById(R.id.alarmfault);
            alarmView.setText(alarmView.getText() + " " + fault);

            TextView equipmentAffected = (TextView)rootView.findViewById(R.id.equipmentAffected);
            equipmentAffected.setText(equipmentAffected.getText() + " " + affectEquipment);

            TextView vendorView = (TextView)rootView.findViewById(R.id.equipmentVendor);
            vendorView.setText(vendorView.getText() + " " + vendor);

            TextView severityView = (TextView)rootView.findViewById(R.id.severity);
            severityView.setText(severityView.getText() + " " + severity);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView tv = (TextView)rootView.findViewById(R.id.detailsText);
        detailString = intent.getStringExtra("details");
        tv.setText(detailString);

        Log.d("detailsProfile", getActivity().getIntent().getStringExtra("profile"));

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FetchAlarmTask verifyAlarm = new FetchAlarmTask();
                verifyAlarm.execute();
            }
        });

        ImageButton home = (ImageButton)rootView.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity(), Anomaly.class);
                startActivity(intent);
            }
        });

        ImageButton back = (ImageButton)rootView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Anomaly.class);
                startActivity(intent);
            }
        });

        ImageButton report = (ImageButton)rootView.findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Reports.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    class FetchAlarmTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void...voids){
            HttpURLConnection client = null;
            BufferedReader reader = null;
            String alarmJSONStr = null;

            try{
                URL url = new URL("http://192.168.0.12:8080/UserManagement/MongoService/isReqAckd");
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                JSONObject alarmJSON = new JSONObject(getActivity().getIntent().getStringExtra("alarm"));
                String oid = alarmJSON.getJSONObject("_id").getString("$oid");
                Log.d("oid", oid);
                JSONObject userInfo = new JSONObject();
                userInfo.put("_id", oid);

                wr.write(userInfo.toString());
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
                    if(sb.toString().length() > 0) {
                        JSONObject response = new JSONObject(sb.toString());
                        Log.d("isReqAckd", response.toString());
                        Boolean uh = response.getBoolean("isReqAckd");
                        Log.d("isReqAckd", uh.toString());
                        return uh;
                    }
                    else return null;
                } else {
                    System.out.println("Server response: " + client.getResponseMessage());
                }
            }catch(Exception o) {
                Log.d("hello", o.toString());
                o.printStackTrace();
            }finally {
                if (client != null) {// Make sure the connection is not null.
                    client.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean isReqAckd) {
            super.onPostExecute(isReqAckd);
            if(isReqAckd){
                Intent intent = new Intent(getActivity(), Survey.class);
                System.out.println("AlarmID: " + alarmID);
                String[] ackID = {alarmID};
                SendACK data = new SendACK();
                data.execute(ackID);

                //Start The Survey Page
                intent.putExtra("alarm", getActivity().getIntent().getStringExtra("alarm"));
                startActivity(intent);
            }else{
                builder.setTitle(detailString);
                builder.setIcon(R.drawable.exit).show();
                builder.setMessage("Alarm Has Been Acknowledged");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getActivity(), Anomaly.class);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    class SendLocationData extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... args){
            Log.d("enter", "entered function");
            HttpURLConnection client = null;

            String location = args[0];

            try{
                URL url = new URL("http://192.168.0.12:8080/UserManagement/MongoService/report");
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                JSONObject locationInfo = new JSONObject();
                locationInfo.put("location", location);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                Log.d("locationProfile", locationInfo.toString());
                wr.write(locationInfo.toString());
                wr.flush();
                wr.close();

                Log.d("locationOutput", "output stream");

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

                    JSONObject reportResponse = new JSONObject(sb.toString());
                    reportMatches = reportResponse.getJSONArray("alarms");

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

    class SendACK extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... args){
            Log.d("enter", "entered function");
            HttpURLConnection client = null;

            String alarmID = args[0];

            try{
                alarmACK.put("alarm",alarmID);

                URL url = new URL("http://192.168.0.12:8080/UserManagement/MongoService/ack");
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                Log.d("alarm", alarmACK.toString());
                wr.write(alarmACK.toString());
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
                    Log.d("goodbye", sb.toString());
                    alarmACK = new JSONObject(sb.toString());
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