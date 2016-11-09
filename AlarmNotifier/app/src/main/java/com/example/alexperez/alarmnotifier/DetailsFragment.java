package com.example.alexperez.alarmnotifier;

import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;


public class DetailsFragment extends Fragment {
    private ArrayList<String> results = new ArrayList<String>();
    private JSONObject alarmACK = new JSONObject();
    private JSONArray reportMatches;
    private String userProfile = "";
    final String IP_ADDRESS = "192.168.1.8";

    public DetailsFragment() {
        // Required empty public constructor
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        FetchSurveyTask task = new FetchSurveyTask();
//        task.execute();
// }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        final Intent intent = getActivity().getIntent();
        String detailString = intent.getStringExtra("details");
        String alarmJSON = intent.getStringExtra("alarm");
        Log.d("alarm", alarmJSON);

        try {
            JSONObject alarmObject = new JSONObject(alarmJSON);

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

        Log.d("detailsProfile",getActivity().getIntent().getStringExtra("profile"));

        final Button accept = (Button)rootView.findViewById(R.id.accept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Survey.class);

                //Send DB that it has been ACK
                Boolean accepted = accept.isPressed();
                System.out.println("accepted Boolean: " + accepted);

                Boolean[] ack = {accepted};

                SendACK data = new SendACK();
                data.execute(ack);

                //Start The Survey Page
                intent.putExtra("profile",getActivity().getIntent().getStringExtra("profile"));
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
                Intent intent = new Intent(getActivity(), Anomaly.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile",userProfile);
                startActivity(intent);
            }
        });

        ImageButton report = (ImageButton)rootView.findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userProfile = getActivity().getIntent().getStringExtra("profile");
                try {
                    JSONObject profile = new JSONObject(userProfile);
                    String location = profile.getString("location");

                    String[] locationInfo = {location};

                    SendLocationData data = new SendLocationData();
                    data.execute(locationInfo);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(rootView.getContext(), String.valueOf(reportMatches.length()), Toast.LENGTH_SHORT).show();
                        }
                    }, 1000);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(getContext(), results.get(0), Toast.LENGTH_LONG).show();
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

    class FetchSurveyTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(Void...voids){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String surveyJSONStr = null;
            String[] resultArray = null;

            try{
                URL url = new URL("http://" + IP_ADDRESS + ":8080/UserManagement/MongoService/alarms");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                surveyJSONStr = buffer.toString();
                resultArray = getSurveyDataFromJson(surveyJSONStr);

                if(results.isEmpty()){
                    results.addAll(Arrays.asList(resultArray));
                }
                else{
                    results.clear();
                    results.addAll(Arrays.asList(resultArray));
                }


                Log.d("lengthResults", String.valueOf(results.size()));
            }catch(IOException o){
                o.printStackTrace();
            }

            return resultArray;

        }

        private String[] getSurveyDataFromJson(String surveyJsonStr){
            String[] resultStr = new String[1];

            try{
                JSONObject jsonObject = new JSONObject(surveyJsonStr);
                String resolution = jsonObject.getString("resolution");

                for(int i=0; i < resultStr.length; i++){
                    resultStr[i] = resolution;
                }
            }catch(Exception o){
                o.printStackTrace();
            }

            return resultStr;
        }
    }

    class SendACK extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Boolean... args){
            Log.d("enter", "entered function");
            HttpURLConnection client = null;

            Boolean alarm = args[0];

            try{
                alarmACK.put("alarm",alarm);

                URL url = new URL("http://" + IP_ADDRESS + ":8080/UserManagement/MongoService/alarms");
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

//                    if(//sb.toString().contains("true"))
//                        match = true;
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
