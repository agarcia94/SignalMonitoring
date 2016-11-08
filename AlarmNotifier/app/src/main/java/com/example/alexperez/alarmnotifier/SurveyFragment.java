package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class SurveyFragment extends Fragment {

    public SurveyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_survey, container, false);
        //rootView.findViewById(R.id.)
        Spinner resolution = (Spinner)rootView.findViewById(R.id.spinner2);
        final EditText other = (EditText)rootView.findViewById(R.id.other);
        other.setVisibility(View.GONE);

        resolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(parent.getItemAtPosition(position).toString().equalsIgnoreCase("other")){
                    other.setVisibility(View.VISIBLE);
                }
                else{
                    other.setVisibility(View.GONE);
                }
//                Toast.makeText(parent.getContext(),
//                        "OnItemSelectedListener : " + parent.getItemAtPosition(position).toString(),
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Log.d("surveyProfile", getActivity().getIntent().getStringExtra("profile"));

        return rootView;
    }
}
