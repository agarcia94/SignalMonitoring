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

        String profile = getActivity().getIntent().getStringExtra("profile");
        String username = "";
        String location = "";
        try {
            JSONObject userInfo = new JSONObject(profile);
            username = userInfo.getString("username");
            location = userInfo.getString("location");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView welcomeText = (TextView)rootView.findViewById(R.id.welcome);
        welcomeText.setText(welcomeText.getText() + " " + username);


        TextView locationText = (TextView)rootView.findViewById(R.id.location);
        locationText.setText(locationText.getText() + " " + location);

        Log.d("anomalyProfile",getActivity().getIntent().getStringExtra("profile"));



        adapter = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, data);

        currentAlarms.setAdapter(adapter);
        ackAlarms.setAdapter(adapter);

        currentAlarms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), Details.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("details", adapter.getItem(i));
                intent.putExtra("profile", userProfile);
                intent.putExtra("alarm", alarmJSON.toString());

                startActivity(intent);
            }
        });


        ackAlarms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), Details.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("details", adapter.getItem(i));
                intent.putExtra("profile", userProfile);
                startActivity(intent);
            }
        });


//        data.add("Antenna");
//        data.add("Electrical Circuit");
//        data.add("No Activity");


        return rootView;

    }

    class FetchAlarmTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void...voids){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String alarmJSONStr = null;

            try{
                URL url = new URL("http://192.168.1.8:8080/UserManagement/MongoService/alarms");

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
                String parameterItems = alarmJSON.getString("parameter");
                String[] parameterFields = parameterItems.split("-");

                String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
                for(int i = 0; i < anomalyNameArray.length; i++){
                    System.out.println("anomaly name: " + anomalyNameArray[i]);
                }

                String alarmInfo = parameterFields[2] + "-" + parameterFields[0] + " " + anomalyNameArray[1];
                data.add(alarmInfo);
            }catch(JSONException o){
                o.printStackTrace();
            }

            //adapter.addAll(data);

        }
    }
}
