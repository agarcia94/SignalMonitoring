package com.example.alexperez.alarmnotifier;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import java.net.URL;


public class AnomalyFragment extends Fragment {

    private ListView lv;
    private ArrayAdapter<String> adapter;

    public AnomalyFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_anomaly, container, false);

        lv = (ListView)rootView.findViewById(R.id.listview);

        ArrayList<String> data = new ArrayList<>();
        data.add("Antenna");
        data.add("Electrical Circuit");
        data.add("No Activity");

        adapter = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, data);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), Details.class);
                intent.putExtra("details", adapter.getItem(i));
                startActivity(intent);
            }
        });

        ImageButton home = (ImageButton)rootView.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        return rootView;

        /*
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_anomaly, container, false);

        Intent intent = getActivity().getIntent();
        String anomalyString = intent.getStringExtra("anomaly");

        TextView tv = (TextView)rootView.findViewById(R.id.anomalyText);
        tv.setText(anomalyString);

        return rootView;
        */

    }



}
