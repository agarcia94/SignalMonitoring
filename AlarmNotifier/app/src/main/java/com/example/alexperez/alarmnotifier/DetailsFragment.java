package com.example.alexperez.alarmnotifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
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


    //IP ADDRESS
    //final String IP_ADDRESS = "10.85.41.232";
    final String IP_ADDRESS = "192.168.1.67";
    //final String IP_ADDRESS = "192.168.1.8";


    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        //TEST PURPOSE FOR LISTVIEW ON DETAILS
//        pastAnomListView = (ListView)rootView.findViewById(R.id.pastAnomListView);
//        results.add(0,"Hello");
//        results.add(1,"World");
//        simPastAnomaliesAdap = new ArrayAdapter(getActivity(), R.layout.past_anomalies_custom, R.id.textView, results);
//        pastAnomListView.setAdapter(simPastAnomaliesAdap);
//
//        pastAnomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = new Intent(getActivity(), Reports.class);
//                userProfile = getActivity().getIntent().getStringExtra("profile");
//                intent.putExtra("profile", userProfile);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//            }
//        });

        final Intent intent = getActivity().getIntent();
        final String detailString = intent.getStringExtra("details");
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
        tv.setText(detailString);

        Log.d("detailsProfile", getActivity().getIntent().getStringExtra("profile"));

        try {
            JSONObject alarmObject = new JSONObject(alarmJSON);
            boolean requiresAcknowledgement = alarmObject.getBoolean("requiresAcknowledgment");
            if(!requiresAcknowledgement) {
                Button accept = (Button)rootView.findViewById(R.id.accept);
                accept.setEnabled(false);
                accept.setBackgroundColor(Color.GRAY);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Button accept = (Button)rootView.findViewById(R.id.accept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Survey.class);

                System.out.println("AlarmID: " + alarmID);
                String[] ackID = {alarmID};

                SendACK data = new SendACK();
                data.execute(ackID);

                //Start The Survey Page
                intent.putExtra("profile",getActivity().getIntent().getStringExtra("profile"));
                intent.putExtra("alarm",getActivity().getIntent().getStringExtra("alarm"));
                startActivity(intent);
            }
        });

        ImageButton home = (ImageButton)rootView.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity(), Anomaly.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile",userProfile);
                startActivity(intent);
            }
        });

        ImageButton back = (ImageButton)rootView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                builder.setTitle(detailString);
                builder.setIcon(R.drawable.exit).show();
                builder.setMessage("Would you Like To Decline Responsibility? ");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getActivity(), Anomaly.class);
                        userProfile = getActivity().getIntent().getStringExtra("profile");
                        intent.putExtra("profile", userProfile);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
                builder.setNeutralButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getActivity(), Anomaly.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        //dialog.cancel();
                        //dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        ImageButton report = (ImageButton)rootView.findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Reports.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile", userProfile);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


        return rootView;
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
                URL url = new URL("http://" + IP_ADDRESS + ":8080/UserManagement/MongoService/report");
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                JSONObject locationInfo = new JSONObject();
                locationInfo.put("location",location);

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

                URL url = new URL("http://" + IP_ADDRESS + ":8080/UserManagement/MongoService/ack");
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