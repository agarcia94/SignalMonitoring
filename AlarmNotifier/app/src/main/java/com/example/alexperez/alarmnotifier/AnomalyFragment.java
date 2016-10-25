package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


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

        return rootView;

    }
}
