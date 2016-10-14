package com.example.alexperez.alarmnotifier;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class AnomalyFragment extends Fragment {

    public AnomalyFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_anomaly, container, false);

        Intent intent = getActivity().getIntent();
        String anomalyString = intent.getStringExtra("anomaly");

        TextView tv = (TextView)rootView.findViewById(R.id.anomalyText);
        tv.setText(anomalyString);

        return rootView;
    }


}
