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


public class AnomalyFragment extends Fragment {

    private ListView ackAlarms,currentAlarms;
    private ArrayAdapter<String> adapter;

    private String userProfile = "";

    public AnomalyFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_anomaly, container, false);

        currentAlarms = (ListView)rootView.findViewById(R.id.ackAlarmsList);

        ArrayList<String> data = new ArrayList<>();
        data.add("Antenna");
        data.add("Electrical Circuit");
        data.add("No Activity");

        Log.d("anomalyProfile",getActivity().getIntent().getStringExtra("profile"));

        adapter = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, data);
        currentAlarms.setAdapter(adapter);

        currentAlarms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), Details.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("details", adapter.getItem(i));
                intent.putExtra("profile",userProfile);
                startActivity(intent);
            }
        });

        ImageButton back = (ImageButton)rootView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile",userProfile);
                startActivity(intent);
            }
        });

        return rootView;

    }
}
