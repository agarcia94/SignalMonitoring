package com.example.alexperez.alarmnotifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * A placeholder fragment containing a simple view.
 */
public class SurveyFragment extends Fragment {
    private String userProfile = "";
    //final String IP_ADDRESS = "10.85.43.2";
    //final String IP_ADDRESS = "192.168.1.67";
    final String IP_ADDRESS = "192.168.1.8";

    private String resolutionTime = "";
    private String resolutionType = "";

    private boolean isOtherResolutionType = false;

    private String moratoriumCheck = "";
    private String outageCheck = "";
    private String outageDuration = "";

    private JSONObject surveyProfile = new JSONObject();

    public SurveyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("surveyAlarm", getActivity().getIntent().getStringExtra("alarm"));

        final View rootView = inflater.inflate(R.layout.fragment_survey, container, false);

        final Button submit = (Button)rootView.findViewById(R.id.submitSurvey);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getActivity(), Anomaly.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile", userProfile);

                TextView date = (TextView)rootView.findViewById(R.id.Date);
                String[] dateInfo = date.getText().toString().split(": ");
                String timestamp = dateInfo[1].trim();
//                String timestamp = dateInfo[1] + ":" + dateInfo[2] +
//                                   ":" + dateInfo[3] + ":" + dateInfo[4];

                Log.d("superTime",timestamp);

                TextView name = (TextView)rootView.findViewById(R.id.name);
                String[] usernameArray = name.getText().toString().split(": ");
                String userName = usernameArray[1].trim();

                TextView site = (TextView)rootView.findViewById(R.id.site);
                String[] siteInfoArray = site.getText().toString().split(": ");
                String siteInfo = siteInfoArray[1].trim();

                TextView antenna = (TextView)rootView.findViewById(R.id.antenna);
                String[] antennaInfoArray = antenna.getText().toString().split(": ");
                String antennaInfo = antennaInfoArray[1].trim();

                TextView alarm = (TextView)rootView.findViewById(R.id.alarmFault);
                String[] alarmInfoArray = alarm.getText().toString().split(": ");
                String alarmInfo = alarmInfoArray[1].trim();

                TextView equipmentAffected = (TextView)rootView.findViewById(R.id.equipAffec);
                String[] equipAffectArray = equipmentAffected.getText().toString().split(": ");
                String affecEquip = equipAffectArray[1].trim();

                TextView vendor = (TextView)rootView.findViewById(R.id.equipVendor);
                String[] vendorInfoArray = vendor.getText().toString().split(": ");
                String vendorInfo = vendorInfoArray[1].trim();

                TextView severity = (TextView)rootView.findViewById(R.id.severity);
                String[] severityInfoArray = severity.getText().toString().split(": ");
                String severityInfo = severityInfoArray[1].trim();

                String IAanswerInfo = "";

                EditText answerOfIA = (EditText)rootView.findViewById(R.id.answerOfIA);
                if(!answerOfIA.getText().toString().isEmpty())
                    IAanswerInfo = answerOfIA.getText().toString();


                if(isOtherResolutionType){
                    EditText other = (EditText)rootView.findViewById(R.id.other);
                    if(other.getText().toString().isEmpty())
                        resolutionType = "";
                    else
                        resolutionType = other.getText().toString();
                }


                //Log.d("arrayYeah",answerOfIA.getText().toString());

                try{
                    Log.d("newTime", timestamp);
                    surveyProfile.put("dateOfAnomaly",timestamp);
                    surveyProfile.put("username",userName);
                    surveyProfile.put("site",siteInfo);
                    surveyProfile.put("antenna",antennaInfo);
                    surveyProfile.put("alarm",alarmInfo);
                    surveyProfile.put("equipAffected",affecEquip);
                    surveyProfile.put("vendor",vendorInfo);
                    surveyProfile.put("severity",severityInfo);
                    surveyProfile.put("issueDescription",IAanswerInfo);
                    surveyProfile.put("resolveTime",resolutionTime);
                    surveyProfile.put("resolution",resolutionType);
                    surveyProfile.put("moratorium",moratoriumCheck);
                    surveyProfile.put("outage",outageCheck);

                    if(outageCheck.equals("No"))
                        surveyProfile.put("outageDuration","");
                    else
                        surveyProfile.put("outageDuration",outageDuration);

                    String[] surveyInfo = {surveyProfile.toString()};
                    Log.d("surveyInfo", surveyProfile.toString());

                    SendSurveyData sendSurvey = new SendSurveyData();
                    sendSurvey.execute(surveyInfo);

                }catch(JSONException o){
                    o.printStackTrace();
                }


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

            TextView name = (TextView)rootView.findViewById(R.id.name);
            userProfile = getActivity().getIntent().getStringExtra("profile");

            JSONObject userInfo = new JSONObject(userProfile);
            String username = userInfo.getString("username");
            name.setText(name.getText() + " " + username);

            Log.d("surveyUserProfile", userProfile);

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

        Spinner resolveIA = (Spinner)rootView.findViewById(R.id.spinner1);
        resolveIA.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                resolutionTime = parent.getItemAtPosition(position).toString();
//                Toast.makeText(parent.getContext(),
//                        "OnItemSelectedListener : " + resolutionTime,
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final Spinner resolution = (Spinner)rootView.findViewById(R.id.spinner2);
        final EditText other = (EditText)rootView.findViewById(R.id.other);
        other.setVisibility(View.GONE);

        resolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("other")) {
                    other.setVisibility(View.VISIBLE);
                    isOtherResolutionType = true;
                } else {
                    other.setVisibility(View.GONE);
                    resolutionType = parent.getItemAtPosition(position).toString();
                    isOtherResolutionType = false;
                }

                Toast.makeText(parent.getContext(),
                        "OnItemSelectedListener : " + resolutionType,
                        Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner moratorium = (Spinner)rootView.findViewById(R.id.spinner3);
        moratorium.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                moratoriumCheck = parent.getItemAtPosition(position).toString();
//                Toast.makeText(parent.getContext(),
//                        "OnItemSelectedListener : " + moratoriumCheck,
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

                outageCheck = parent.getItemAtPosition(position).toString();
//                Toast.makeText(parent.getContext(),
//                        "OnItemSelectedListener : " + outageCheck,
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        outage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                outageDuration = parent.getItemAtPosition(position).toString();
//                Toast.makeText(parent.getContext(),
//                        "OnItemSelectedListener : " + outageDuration,
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Log.d("surveyProfile", getActivity().getIntent().getStringExtra("profile"));

        return rootView;
    }

    class SendSurveyData extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... args){
            Log.d("enter", "entered function");
            HttpURLConnection client = null;

            String survey = args[0];

            try{


                //URL url = new URL("http://192.168.43.253:8080/UserManagement/MongoService/login");
                URL url = new URL("http://" + IP_ADDRESS + ":8080/UserManagement/MongoService/survey");

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                Log.d("profile", survey);
                wr.write(survey);
                wr.flush();
                wr.close();

                Log.d("output", "output stream");

                StringBuilder sb = new StringBuilder();
                int HttpResult = client.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(client.getInputStream()));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    Log.d("goodbye", sb.toString());

                    System.out.println("" + sb.toString());


                } else {
                    Log.d("hello", client.getResponseMessage());
                    System.out.println("Server response: " + client.getResponseMessage());
                }


            }catch(Exception o) {
                o.printStackTrace();
            }finally {
                if(client != null) // Make sure the connection is not null.
                    client.disconnect();
            }

            return null;
        }
    }


}
