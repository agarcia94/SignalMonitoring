package com.example.alexperez.alarmnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
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

import java.util.ArrayList;
import java.util.regex.Pattern;

public class AnomalyFragment extends Fragment implements LoaderManager.LoaderCallbacks<JSONObject>{

    private ListView ackAlarms,currentAlarms;
    private ArrayAdapter<String> adapter,currentAdapter;
    private String userProfile = "";
    private JSONObject alarmJSON;
    private ArrayList<String> data;
    private ArrayList<String> ackData;
    private TextView load1;
    private TextView load2;
    private LoaderManager.LoaderCallbacks<JSONObject> loadercb = this;

    public AnomalyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter inF = new IntentFilter("data_changed");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(dataChangeReceiver, inF);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Initialize Views
        View rootView = inflater.inflate(R.layout.fragment_anomaly, container, false);
        currentAlarms = (ListView)rootView.findViewById(R.id.cAlarmList);
        ackAlarms = (ListView)rootView.findViewById(R.id.ackAlarmsList);
        data = new ArrayList<>();
        ackData = new ArrayList<>();
        load1 = (TextView) rootView.findViewById(R.id.loading1);
        load2 = (TextView) rootView.findViewById(R.id.loading2);
        load1.setVisibility(View.VISIBLE);
        load2.setVisibility(View.VISIBLE);

        // Get user information from application context
        userProfile = SaveSharedPreference.getUserName(getActivity());

        // Check if there is any existing user logged in
        String username = "";
        String location = "";
        try {
            JSONObject userInfo = new JSONObject(userProfile);
            username = userInfo.getString("username");
            location = userInfo.getString("location");

            if (location.length() > 0){
                // If there is existing user, start loading data from server
                FirebaseMessaging.getInstance().subscribeToTopic(location);

                if(getLoaderManager().getLoader(1) == null){
                    getLoaderManager().initLoader(1, null, this).forceLoad();
                }else{
                    data = new ArrayList<>();
                    ackData = new ArrayList<>();
                    alarmJSON = new JSONObject();
                    getLoaderManager().restartLoader(1, null, this).forceLoad();
                }
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
                        boolean isReq = currentAlarm.getBoolean("requiresAcknowledgment");
                        String[] itemFields = itemInAdapter.split(" ", 2);
                        String anomalyName = itemFields[1];

                        String[] antennaAbbreviationArray = itemFields[0].split("-");
                        String antenna = antennaAbbreviationArray[0];
                        String abbreviation = antennaAbbreviationArray[1];
                        if(parameter.contains(anomalyName)
                                && parameter.contains(antenna)
                                && parameter.contains(abbreviation) && isReq){

                            intent.putExtra("currAlarm", currentAlarm.toString());
                            Log.d("currAlarmAno", currentAlarm.toString());
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

                    for (int j = 0; j < alarmArray.length(); j++) {
                        JSONObject currentAlarm = alarmArray.getJSONObject(j);
                        String parameter = currentAlarm.getString("parameter");
                        boolean isReq = currentAlarm.getBoolean("requiresAcknowledgment");

                        String[] itemFields = itemInAdapter.split(" ", 2);
                        String anomalyName = itemFields[1];

                        String[] antennaAbbreviationArray = itemFields[0].split("-");
                        String antenna = antennaAbbreviationArray[0];
                        String abbreviation = antennaAbbreviationArray[1];

                        if (parameter.contains(anomalyName)
                                && parameter.contains(antenna)
                                && parameter.contains(abbreviation) && !isReq) {

                            intent.putExtra("currAlarm", currentAlarm.toString());
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
        if(jdata != null){
            alarmJSON = jdata;
            try{
                JSONArray alarmArray = jdata.getJSONArray("alarms");
                Log.d("alarmJson", jdata.toString());
                for(int i = 0; i < alarmArray.length(); i++){
                    JSONObject alarm = alarmArray.getJSONObject(i);
                    String parameterItems = alarm.getString("parameter");
                    Boolean ackAlarms = alarm.getBoolean("requiresAcknowledgment");
                    if(ackAlarms == false) {
                        String[] parameterFields = parameterItems.split("-");
                        String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
                        String alarmInfo = parameterFields[2] + "-" + parameterFields[0] + " " + anomalyNameArray[1];
                        Log.d("alarmInfoAno", alarmInfo);
                        ackData.add(alarmInfo);
                    }

                    if(ackAlarms == true){
                        String[] parameterFields = parameterItems.split("-");
                        String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
                        String alarmInfo = parameterFields[2] + "-" + parameterFields[0] + " " + anomalyNameArray[1];
                        data.add(alarmInfo);
                    }
                }
            }catch(JSONException o){
                o.printStackTrace();
            }

            // Temporary remove the loading labels when data is ready
            load1.setVisibility(View.GONE);
            load2.setVisibility(View.GONE);

            // Update the adapter and load data into according list views
            adapter = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, ackData); //acknowledged alarms
            currentAdapter = new ArrayAdapter(getActivity(), R.layout.crow, R.id.textView, data); //current alarms

            currentAlarms.setAdapter(currentAdapter);
            ackAlarms.setAdapter(adapter);
        }


    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
        alarmJSON = null;
    }

    private BroadcastReceiver dataChangeReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(getLoaderManager().getLoader(1) == null){
                getLoaderManager().initLoader(1, null, loadercb).forceLoad();
            }else{
                data = new ArrayList<>();
                ackData = new ArrayList<>();
                alarmJSON = new JSONObject();
                getLoaderManager().restartLoader(1, null, loadercb).forceLoad();
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(dataChangeReceiver);
    }
}
