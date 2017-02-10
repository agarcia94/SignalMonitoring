package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * A placeholder fragment containing a simple view.
 */
public class ComparisonsReportsFragment extends Fragment {
    private String userProfile = "";

    public ComparisonsReportsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_comparisons_reports, container, false);

        ImageButton home = (ImageButton)rootView.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity(), Anomaly.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile",userProfile);
                startActivity(intent);
            }
        });

        ImageButton back = (ImageButton)rootView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Anomaly.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile", userProfile);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
