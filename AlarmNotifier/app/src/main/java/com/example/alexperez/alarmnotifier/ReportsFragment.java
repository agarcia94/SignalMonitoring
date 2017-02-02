package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReportsFragment extends Fragment {
    private String userProfile = "";
    private String fieldSelection = "";
    //final String IP_ADDRESS = "192.168.1.67";
    final String IP_ADDRESS = "192.168.1.8";

    public ReportsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_reports, container, false);
        userProfile = getActivity().getIntent().getStringExtra("profile");

        ImageButton home = (ImageButton)rootView.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Anomaly.class);
                //userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile", userProfile);
                startActivity(intent);
            }
        });

        ImageButton back = (ImageButton)rootView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Anomaly.class);
                //userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile", userProfile);
                startActivity(intent);
            }
        });


        return rootView;
    }
}
