package com.example.alexperez.alarmnotifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

/**
 * A placeholder fragment containing a simple view.
 */
public class SurveyFragment extends Fragment {
    private String userProfile = "";
    final String IP_ADDRESS = "10.85.45.132";

    public SurveyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("surveyAlarm", getActivity().getIntent().getStringExtra("alarm"));

        View rootView = inflater.inflate(R.layout.fragment_survey, container, false);

        final Button submit = (Button)rootView.findViewById(R.id.submitSurvey);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getActivity(), Anomaly.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile", userProfile);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle("Survey Has Been Submitted");
                builder.setMessage("Will Be Re-Directed To Home Page");
                builder.setIcon(R.drawable.check).show();
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(intent);
                    }
                });
                builder.show();
            }
        });

        try {
            JSONObject alarmObject = new JSONObject(getActivity().getIntent().getStringExtra("alarm"));

            int severity = alarmObject.getInt("severity");
            String parameter = alarmObject.getString("parameter");

            String[] parameterFields = parameter.split("-");

            String site = parameterFields[1];
            String antenna = parameterFields[2];

            String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
            String fault = anomalyNameArray[1];
            int nameIndex = fault.indexOf("(");

            if(nameIndex != -1){
                fault = fault.substring(0,nameIndex);
            }

            JSONObject deviceAttributes = alarmObject.getJSONObject("deviceAttributes");
            String affectEquipment = deviceAttributes.getString("DeviceId");
            String vendor = deviceAttributes.getString("DeviceType");
            vendor = vendor.substring(0, vendor.indexOf(" "));

            TextView date = (TextView)rootView.findViewById(R.id.Date);
            date.setText(date.getText() + " " + alarmObject.getString("timestamp"));

            TextView siteView = (TextView)rootView.findViewById(R.id.site);
            siteView.setText(siteView.getText() + " " + site);

            TextView antennaView = (TextView)rootView.findViewById(R.id.antenna);
            antennaView.setText(antennaView.getText() + " " + antenna);

            TextView alarmView = (TextView)rootView.findViewById(R.id.alarmFault);
            alarmView.setText(alarmView.getText() + " " + fault);

            TextView equipmentAffected = (TextView)rootView.findViewById(R.id.equipAffec);
            equipmentAffected.setText(equipmentAffected.getText() + " " + affectEquipment);

            TextView vendorView = (TextView)rootView.findViewById(R.id.equipVendor);
            vendorView.setText(vendorView.getText() + " " + vendor);

            TextView severityView = (TextView)rootView.findViewById(R.id.severity);
            severityView.setText(severityView.getText() + " " + severity);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Spinner resolution = (Spinner)rootView.findViewById(R.id.spinner2);
        final EditText other = (EditText)rootView.findViewById(R.id.other);
        other.setVisibility(View.GONE);

        resolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("other")) {
                    other.setVisibility(View.VISIBLE);
                } else {
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

        Spinner wasThereAnOutage = (Spinner)rootView.findViewById(R.id.spinner4);
        final TextView outageIfYes = (TextView)rootView.findViewById(R.id.outageTime);
        final Spinner outage = (Spinner)rootView.findViewById(R.id.spinner5);
        outageIfYes.setVisibility(View.GONE);
        outage.setVisibility(View.GONE);

        wasThereAnOutage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("yes")) {
                    outageIfYes.setVisibility(View.VISIBLE);
                    outage.setVisibility(View.VISIBLE);
                } else {
                    outageIfYes.setVisibility(View.GONE);
                    outage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Log.d("surveyProfile", getActivity().getIntent().getStringExtra("profile"));

        return rootView;
    }
}
