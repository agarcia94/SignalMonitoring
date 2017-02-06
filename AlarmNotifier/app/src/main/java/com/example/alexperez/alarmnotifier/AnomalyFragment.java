package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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

import java.util.ArrayList;
import java.util.regex.Pattern;

public class AnomalyFragment extends Fragment implements LoaderManager.LoaderCallbacks<JSONObject>{

    private ListView ackAlarms,currentAlarms;
    private ArrayAdapter<String> adapter,currentAdapter;
    private String userProfile = "";
    static JSONObject alarmJSON;
    private ArrayList<String> data;
    private ArrayList<String> ackData;

    //final String IP_ADDRESS = "10.85.41.232";
    final static String IP_ADDRESS = "192.168.0.12";
    //final String IP_ADDRESS = "192.168.1.8";

    public AnomalyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().initLoader(1, null, this).forceLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_anomaly, container, false);

        currentAlarms = (ListView)rootView.findViewById(R.id.cAlarmList);
        ackAlarms = (ListView)rootView.findViewById(R.id.ackAlarmsList);
        data = new ArrayList<>();
        ackData = new ArrayList<>();
        userProfile = SaveSharedPreference.getUserName(getActivity());
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

        return rootView;

    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        return new FetchAlarmTaskLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject jdata) {
        alarmJSON = jdata;
        if(ackData.isEmpty() && data.isEmpty()){
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
            }catch(JSONException o){
                o.printStackTrace();
            }

        }

        adapter = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, ackData); //acknowledged alarms
        currentAdapter = new ArrayAdapter(getActivity(), R.layout.crow, R.id.textView, data); //current alarms

        currentAlarms.setAdapter(currentAdapter);
        ackAlarms.setAdapter(adapter);

    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
        alarmJSON = null;
    }

}
