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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
    private boolean ackAlarmList = false;
    private JSONObject testAlarmJSON = null;


    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //TEST PURPOSE FOR LISTVIEW ON DETAILS
//        pastAnomListView = (ListView)rootView.findViewById(R.id.pastAnomListView);
//        results.add(0,"Hello");
//        results.add(1,"World");
//        simPastAnomaliesAdap = new ArrayAdapter(getActivity(), R.layout.past_anomalies_custom, R.id.textView, results);
//        pastAnomListView.setAdapter(simPastAnomaliesAdap);

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

                //ackAlarmList = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Button accept = (Button)rootView.findViewById(R.id.accept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FetchAlarmTask verifyAlarm = new FetchAlarmTask();
                verifyAlarm.execute();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (ackAlarmList) { // if matching record in database needs acknowledgement
                            Intent intent = new Intent(getActivity(), Survey.class);

                            System.out.println("AlarmID: " + alarmID);
                            String[] ackID = {alarmID};

                            SendACK data = new SendACK();
                            data.execute(ackID);

                            //Start The Survey Page
                            intent.putExtra("profile",getActivity().getIntent().getStringExtra("profile"));
                            intent.putExtra("alarm",getActivity().getIntent().getStringExtra("alarm"));
                            startActivity(intent);
                        } else { // if matching record in database has already been acknowledged
                            builder.setTitle(detailString);
                            builder.setIcon(R.drawable.exit).show();
                            builder.setMessage("Alarm Has Been Acknowledged");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(getActivity(), Anomaly.class);
                                    userProfile = getActivity().getIntent().getStringExtra("profile");
                                    intent.putExtra("profile", userProfile);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    //intent.putExtra("alarm", getActivity().getIntent().getStringExtra("alarm"));
                                    //intent.putExtra("details", getActivity().getIntent().getStringExtra("details"));
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                }, 1000);


                //If Alarm does not exist within Acknowledged List
                //Then it has not yet been accepted, redirect user to Survey
//                if(!ackAlarmList){
//                    Intent intent = new Intent(getActivity(), Survey.class);
//
//                    System.out.println("AlarmID: " + alarmID);
//                    String[] ackID = {alarmID};
//
//                    SendACK data = new SendACK();
//                    data.execute(ackID);
//
//                    //Start The Survey Page
//                    intent.putExtra("profile",getActivity().getIntent().getStringExtra("profile"));
//                    intent.putExtra("alarm",getActivity().getIntent().getStringExtra("alarm"));
//                    startActivity(intent);
//                }
//                //If It exists in Acknowledged alram list, than tell user
//                //it has been accepted and redirect him/her to same page
//                else{
//                    builder.setTitle(detailString);
//                    builder.setIcon(R.drawable.exit).show();
//                    builder.setMessage("Alarm Has Been Acknowledged");
//                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            Intent intent = new Intent(getActivity(), Details.class);
//                            userProfile = getActivity().getIntent().getStringExtra("profile");
//                            intent.putExtra("profile", userProfile);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            intent.putExtra("alarm", getActivity().getIntent().getStringExtra("alarm"));
//                            intent.putExtra("details", getActivity().getIntent().getStringExtra("details"));
//                            startActivity(intent);
//                        }
//                    });
//                }
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
                //if AckAlarmList is False, it means we are in Current Alarms Listview
                //Meaning we need to use pops up to see if user wasnts to decline
                if(!ackAlarmList){
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
                            Intent intent = new Intent(getActivity(), Details.class);
                            userProfile = getActivity().getIntent().getStringExtra("profile");
                            intent.putExtra("profile", userProfile);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("alarm", getActivity().getIntent().getStringExtra("alarm"));
                            //intent.putExtra("profile",getActivity().getIntent().getStringExtra("profile"));
                            intent.putExtra("details", getActivity().getIntent().getStringExtra("details"));
                            startActivity(intent);
                        }
                    });
                    builder.show();
                }
                //We are in AckAlarms, were the user is just viewing the alarms that has been Ack
                //Popup is not needed in this case
                else{
                    Intent intent = new Intent(getActivity(), Anomaly.class);
                    userProfile = getActivity().getIntent().getStringExtra("profile");
                    intent.putExtra("profile", userProfile);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
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
                URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/report");
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

                URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/ack");
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

    class FetchAlarmTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void...voids){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String alarmJSONStr = null;

            try{

                URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/alarms");

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

                alarmJSONStr = buffer.toString();

                try {
                    testAlarmJSON = new JSONObject(alarmJSONStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("alarm", alarmJSONStr);

            }catch(IOException o){
                o.printStackTrace();
            }finally {
                if(urlConnection != null) // Make sure the connection is not null.
                    urlConnection.disconnect();
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void voidItem) {
            super.onPostExecute(voidItem);

            try{
                JSONArray alarmArray = testAlarmJSON.getJSONArray("alarms");

                for(int i = 0; i < alarmArray.length(); i++){
                    JSONObject alarm = alarmArray.getJSONObject(i);
                    String alarmIDNumber = alarm.getJSONObject("_id").getString("$oid");

                    if(alarmIDNumber.equals(alarmID)){
                        Log.d("alarmMatch", "true");
                        Boolean ackAlarms = alarm.getBoolean("requiresAcknowledgment");
                        if(ackAlarms){
                            ackAlarmList = true;
                            Log.d("ackAlarm", "true");
                        }else{
                            ackAlarmList = false;
                            Log.d("ackAlarm","false");
                        }
                    }
                }


            }catch(JSONException o){
                o.printStackTrace();
            }
            finally{

            }


        }
    }


}