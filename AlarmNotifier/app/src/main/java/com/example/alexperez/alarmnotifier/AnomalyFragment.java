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
import android.widget.Spinner;
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
    private ArrayList<String> data;
    private ArrayList<String> ackData;
    JSONArray currentAlarmArray;
    JSONArray pastAlarmArray;
    private TextView load1;
    private TextView load2;
    private Spinner limitSpinner;
    private LoaderManager.LoaderCallbacks<JSONObject> loadercb = this;
    private String username = "";
    private String location = "";

    public AnomalyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        if(SaveSharedPreference.getLoc(getActivity()).length() > 0){
            FirebaseMessaging.getInstance().subscribeToTopic(SaveSharedPreference.getLoc(getActivity()));
            loadit();
        }
        IntentFilter inF = new IntentFilter("data_changed");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(dataChangeReceiver, inF);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("mango mango");
        // Initialize Views
        View rootView = inflater.inflate(R.layout.fragment_anomaly, container, false);
        currentAlarms = (ListView)rootView.findViewById(R.id.cAlarmList);
        ackAlarms = (ListView)rootView.findViewById(R.id.ackAlarmsList);
        limitSpinner = (Spinner) rootView.findViewById(R.id.limit);
        data = new ArrayList<>();
        ackData = new ArrayList<>();
        load1 = (TextView) rootView.findViewById(R.id.loading1);
        load2 = (TextView) rootView.findViewById(R.id.loading2);
        load1.setVisibility(View.VISIBLE);
        load2.setVisibility(View.VISIBLE);
        ArrayAdapter<CharSequence> limAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.limit, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        limAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        limitSpinner.setAdapter(limAdapter);

        TextView welcomeText = (TextView)rootView.findViewById(R.id.welcome);
        welcomeText.setText(welcomeText.getText() + " " + SaveSharedPreference.getName(getActivity()));

        TextView locationText = (TextView)rootView.findViewById(R.id.location);
        locationText.setText(locationText.getText() + " " + SaveSharedPreference.getLoc(getActivity()));

        //limitSpinner.setSelection(SaveSharedPreference.getLimitPosition(getActivity()));

        currentAlarms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), Details.class);
                //userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("details", currentAdapter.getItem(i));

                try {
                    String itemInAdapter = currentAdapter.getItem(i);

                    for(int j = 0; j < currentAlarmArray.length(); j++){
                        JSONObject currentAlarm = currentAlarmArray.getJSONObject(j);
                        String parameter = currentAlarm.getString("parameter");
                        String[] itemFields = itemInAdapter.split(" ", 2);
                        String anomalyName = itemFields[1];

                        String[] antennaAbbreviationArray = itemFields[0].split("-");
                        String antenna = antennaAbbreviationArray[0];
                        String abbreviation = antennaAbbreviationArray[1];
                        if(parameter.contains(anomalyName)
                                && parameter.contains(antenna)
                                && parameter.contains(abbreviation)){

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

                try {
                    String itemInAdapter = adapter.getItem(i);

                    for (int j = 0; j < pastAlarmArray.length(); j++) {
                        JSONObject currentAlarm = pastAlarmArray.getJSONObject(j);
                        String parameter = currentAlarm.getString("parameter");

                        String[] itemFields = itemInAdapter.split(" ", 2);
                        String anomalyName = itemFields[1];

                        String[] antennaAbbreviationArray = itemFields[0].split("-");
                        String antenna = antennaAbbreviationArray[0];
                        String abbreviation = antennaAbbreviationArray[1];

                        if (parameter.contains(anomalyName)
                                && parameter.contains(antenna)
                                && parameter.contains(abbreviation)) {

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

        limitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SaveSharedPreference.setLimitPosition(getActivity(), position);
                System.out.println("am i here" + position + SaveSharedPreference.getUserName(getActivity()));
                loadit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // do nothing
            }

        });
        limitSpinner.setSelection(SaveSharedPreference.getLimitPosition(getActivity()));
        return rootView;

    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        return new FetchAlarmTaskLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject jdata) {
        if(jdata != null){
            try{
                currentAlarmArray = jdata.getJSONArray("current");
                pastAlarmArray = jdata.getJSONArray("past");
                Log.d("alarmJson", jdata.toString());
                for(int i = 0; i < currentAlarmArray.length(); i++){
                    JSONObject alarm = currentAlarmArray.getJSONObject(i);
                    String parameterItems = alarm.getString("parameter");

                    String[] parameterFields = parameterItems.split("-");
                    String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
                    String alarmInfo = parameterFields[2] + "-" + parameterFields[0] + " " + anomalyNameArray[1];
                    data.add(alarmInfo);
                }

                for(int i = 0; i < pastAlarmArray.length(); i++){
                    JSONObject alarm = pastAlarmArray.getJSONObject(i);
                    String parameterItems = alarm.getString("parameter");
                    String[] parameterFields = parameterItems.split("-");
                    String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
                    String alarmInfo = parameterFields[2] + "-" + parameterFields[0] + " " + anomalyNameArray[1];
                    Log.d("alarmInfoAno", alarmInfo);
                    ackData.add(alarmInfo);
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
    }

    private BroadcastReceiver dataChangeReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadit();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(dataChangeReceiver);
    }


    private void loadit(){
        currentAlarmArray = new JSONArray();
        pastAlarmArray = new JSONArray();
        if(getLoaderManager().getLoader(1) == null){
            getLoaderManager().initLoader(1, null, loadercb).forceLoad();
        }else{
            System.out.println("loadit");
            data = new ArrayList<>();
            ackData = new ArrayList<>();
            getLoaderManager().restartLoader(1, null, loadercb).forceLoad();
        }
    }
}
