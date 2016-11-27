package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;



public class AnomalyFragment extends Fragment {

    private ListView ackAlarms,currentAlarms;
    private ArrayAdapter<String> adapter,currentAdapter;

    private String userProfile = "";
    private JSONObject alarmJSON;

    private ArrayList<String> data = new ArrayList<>();
    private ArrayList<String> ackData = new ArrayList<>();

    //final String IP_ADDRESS = "10.85.41.232";
    //final String IP_ADDRESS = "192.168.1.67";
    final String IP_ADDRESS = "192.168.1.8";


    public AnomalyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchAlarmTask task = new FetchAlarmTask();
        task.execute();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_anomaly, container, false);

        currentAlarms = (ListView)rootView.findViewById(R.id.cAlarmList);
        ackAlarms = (ListView)rootView.findViewById(R.id.ackAlarmsList);

        //String profile = getActivity().getIntent().getStringExtra("profile");
        userProfile = getActivity().getIntent().getStringExtra("profile");
        String username = "";
        String location = "";
        try {
            JSONObject userInfo = new JSONObject(userProfile);
            username = userInfo.getString("username");
            location = userInfo.getString("location");

            if (location.length() > 0){
                FirebaseMessaging.getInstance().subscribeToTopic(location);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView welcomeText = (TextView)rootView.findViewById(R.id.welcome);
        welcomeText.setText(welcomeText.getText() + " " + username);


        TextView locationText = (TextView)rootView.findViewById(R.id.location);
        locationText.setText(locationText.getText() + " " + location);

        Log.d("anomalyProfile", getActivity().getIntent().getStringExtra("profile"));



        adapter = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, ackData);
        currentAdapter = new ArrayAdapter(getActivity(), R.layout.crow, R.id.textView, data);

        currentAlarms.setAdapter(currentAdapter);
        ackAlarms.setAdapter(adapter);

        currentAlarms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), Details.class);
                //userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("details", currentAdapter.getItem(i));
                intent.putExtra("profile", userProfile);

                try {
                    String itemInAdapter = currentAdapter.getItem(i);
                    JSONArray alarmArray = alarmJSON.getJSONArray("alarms");

                    for(int j = 0; j < alarmArray.length(); j++){
                        JSONObject currentAlarm = alarmArray.getJSONObject(j);
                        String parameter = currentAlarm.getString("parameter");

                        String[] itemFields = itemInAdapter.split(" ", 2);
                        String anomalyName = itemFields[1];

                        String[] antennaAbbreviationArray = itemFields[0].split("-");
                        String antenna = antennaAbbreviationArray[0];
                        String abbreviation = antennaAbbreviationArray[1];

                        if(parameter.contains(anomalyName)
                                && parameter.contains(antenna)
                                && parameter.contains(abbreviation)){

                            intent.putExtra("alarm", currentAlarm.toString());
                            break;
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                startActivity(intent);
            }
        });


        ackAlarms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), Details.class);
                //userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("details", adapter.getItem(i));
                intent.putExtra("profile", userProfile);

                try {
                    String itemInAdapter = adapter.getItem(i);
                    JSONArray alarmArray = alarmJSON.getJSONArray("alarms");

                    for(int j = 0; j < alarmArray.length(); j++){
                        JSONObject currentAlarm = alarmArray.getJSONObject(j);
                        String parameter = currentAlarm.getString("parameter");

                        String[] itemFields = itemInAdapter.split(" ", 2);
                        String anomalyName = itemFields[1];

                        String[] antennaAbbreviationArray = itemFields[0].split("-");
                        String antenna = antennaAbbreviationArray[0];
                        String abbreviation = antennaAbbreviationArray[1];

                        if(parameter.contains(anomalyName)
                                && parameter.contains(antenna)
                                && parameter.contains(abbreviation)){

                            intent.putExtra("alarm", currentAlarm.toString());
                            break;
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                startActivity(intent);
            }
        });


//        data.add("Antenna");
//        data.add("Electrical Circuit");
//        data.add("No Activity");

        rootView.postInvalidateDelayed(500);
        return rootView;

    }

    class FetchAlarmTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void...voids){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String alarmJSONStr = null;

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

                alarmJSONStr = buffer.toString();

                try {
                    alarmJSON = new JSONObject(alarmJSONStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("alarm", alarmJSONStr);

            }catch(IOException o){
                o.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void voidItem) {
            super.onPostExecute(voidItem);

            try{
//                String parameterItems = alarmJSON.getString("parameter");
//                String ackAlarms = alarmJSON.getString("requiresAcknowledgment");
                JSONArray alarmArray = alarmJSON.getJSONArray("alarms");

                for(int i = 0; i < alarmArray.length(); i++){
                    JSONObject alarm = alarmArray.getJSONObject(i);
                    String parameterItems = alarm.getString("parameter");
                    Boolean ackAlarms = alarm.getBoolean("requiresAcknowledgment");


                    if(ackAlarms == false) {
                        JSONObject userInfo = new JSONObject(userProfile);
                        String location = userInfo.getString("location");

                        if(parameterItems.contains(location)){
                            String[] parameterFields = parameterItems.split("-");

                            String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
                            for(int j = 0; j < anomalyNameArray.length; j++){
                                System.out.println("anomaly name: " + anomalyNameArray[j]);
                            }

                            String alarmInfo = parameterFields[2] + "-" + parameterFields[0] + " " + anomalyNameArray[1];
                            ackData.add(alarmInfo);
                        }
                    }


                    if(ackAlarms == true){
                        JSONObject userInfo = new JSONObject(userProfile);
                        String location = userInfo.getString("location");

                        if(parameterItems.contains(location)){
                            String[] parameterFields = parameterItems.split("-");

                            String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
                            for(int j = 0; j < anomalyNameArray.length; j++){
                                System.out.println("anomaly name: " + anomalyNameArray[j]);
                            }

                            String alarmInfo = parameterFields[2] + "-" + parameterFields[0] + " " + anomalyNameArray[1];
                            data.add(alarmInfo);
                        }
                    }
                }

//                if(ackAlarms.equalsIgnoreCase("false")) {
//                    String[] parameterFields = parameterItems.split("-");
//
//                    String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
//                    for (int j = 0; j < anomalyNameArray.length; j++) {
//                        System.out.println("anomaly name: " + anomalyNameArray[j]);
//                    }
//
//                    String alarmInfo = parameterFields[2] + "-" + parameterFields[0] + " " + anomalyNameArray[1];
//                    ackData.add(alarmInfo);
//                }
//
//
//                if(ackAlarms.equalsIgnoreCase("true")){
//                    String[] parameterFields = parameterItems.split("-");
//
//                    String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
//                    for(int j = 0; j < anomalyNameArray.length; j++){
//                        System.out.println("anomaly name: " + anomalyNameArray[j]);
//                    }
//
//                    String alarmInfo = parameterFields[2] + "-" + parameterFields[0] + " " + anomalyNameArray[1];
//                    data.add(alarmInfo);
//
//                }


            }catch(JSONException o){
                o.printStackTrace();
            }

//            adapter.addAll(data);
//            currentAdapter.addAll(ackData);

        }
    }
}
