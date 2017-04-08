package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReportsPastDataFragment extends Fragment {
    private String userProfile = "";
    //final String IP_ADDRESS = "192.168.1.67";
    final String IP_ADDRESS = "10.85.47.144";

    public ReportsPastDataFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_reports_past_data, container, false);
        JSONObject currentSurvey = null;

        try{
            currentSurvey = new JSONObject(getActivity().getIntent().getStringExtra("relevantSurvey"));
            Log.d("relevantSurvey", currentSurvey.toString());

            TextView date  = (TextView)rootView.findViewById(R.id.Date);
            date.setText(date.getText() + " " + currentSurvey.getString("dateOfAnomaly"));

            TextView antenna = (TextView)rootView.findViewById(R.id.antenna);
            antenna.setText(antenna.getText() + " " + currentSurvey.getString("antenna"));

            TextView alarmFault = (TextView)rootView.findViewById(R.id.alarmFault);
            alarmFault.setText(alarmFault.getText() + " " + currentSurvey.getString("alarm"));

            TextView equipAffected = (TextView)rootView.findViewById(R.id.equipAffec);
            equipAffected.setText(equipAffected.getText() + " " + currentSurvey.getString("equipAffected"));

            TextView vendor = (TextView)rootView.findViewById(R.id.equipVendor);
            vendor.setText(vendor.getText() + " " + currentSurvey.getString("vendor"));

            TextView severity = (TextView)rootView.findViewById(R.id.severity);
            severity.setText(severity.getText() + " " + currentSurvey.getInt("severity"));

            TextView issueDescription = (TextView)rootView.findViewById(R.id.answerOfIA);
            issueDescription.setText(currentSurvey.getString("issueDescription"));

            TextView resolutionTime = (TextView)rootView.findViewById(R.id.responseToresolveIA);
            resolutionTime.setText(currentSurvey.getString("resolveTime"));

            TextView resolution = (TextView)rootView.findViewById(R.id.responseToresolution);
            resolution.setText(currentSurvey.getString("resolution"));

            TextView moratorium = (TextView)rootView.findViewById(R.id.responseToMoratorium);
            moratorium.setText(currentSurvey.getString("moratorium"));

            TextView outage = (TextView)rootView.findViewById(R.id.responseToOutage);
            outage.setText(currentSurvey.getString("outage"));

            TextView outageTime = (TextView)rootView.findViewById(R.id.responseToOutageTime);

            if(currentSurvey.getString("outage").equalsIgnoreCase("yes"))
                outageTime.setText(currentSurvey.getString("outageDuration"));
            else
                outageTime.setText("N/A");


        }catch(Exception o){
            System.out.println("Uh Oh");
        }



        ImageButton home = (ImageButton)rootView.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Anomaly.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile", userProfile);
                startActivity(intent);
            }
        });

        ImageButton back = (ImageButton)rootView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getActivity().onBackPressed();
            }
        });

        return rootView;
    }
}
