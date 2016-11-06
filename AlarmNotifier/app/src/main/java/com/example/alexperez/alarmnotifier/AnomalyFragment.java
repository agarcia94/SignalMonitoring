package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class AnomalyFragment extends Fragment {

    private ListView ackAlarms,currentAlarms;
    private ArrayAdapter<String> adapter,currentAdapter;

    private String userProfile = "";

    public AnomalyFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_anomaly, container, false);

        currentAlarms = (ListView)rootView.findViewById(R.id.cAlarmList);
        ackAlarms = (ListView)rootView.findViewById(R.id.ackAlarmsList);

        ArrayList<String> data = new ArrayList<>();
        data.add("LKA35-ACU AZ Resolver Fault");
        data.add("LD2-HPA1 Helix Arc Fault");

        Log.d("anomalyProfile", getActivity().getIntent().getStringExtra("profile"));
        currentAdapter = new ArrayAdapter(getActivity(), R.layout.crow, R.id.textView, data);
        adapter = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, data);

        currentAlarms.setAdapter(currentAdapter);
        ackAlarms.setAdapter(adapter);

        currentAlarms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), Details.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("details", adapter.getItem(i));
                intent.putExtra("profile", userProfile);
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

        return rootView;

    }
}
