package com.example.alexperez.alarmnotifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class DetailsFragment extends Fragment {
    private ArrayList<String> results = new ArrayList<String>();
    private ListView pastAnomListView;
    private ArrayAdapter<String> simPastAnomaliesAdap;
    private JSONObject alarmACK = new JSONObject();
    private JSONArray reportMatches;
    private String userProfile = "";
    private String alarmID = "";
    private String detailString = null;
    private Button accept;

    private JSONArray surveys = null;


    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart(){
        super.onStart();

        try {
            JSONObject currentAlarm = new JSONObject(getActivity().getIntent().getStringExtra("currAlarm"));
            String baseLocation = currentAlarm.getString("parameter").split("-")[1];
            String errorType = currentAlarm.getString("parameter").split("-")[0];

            String[] timestampInfo = currentAlarm.getString("timestamp").split(" ");
            String year = timestampInfo[timestampInfo.length - 1];

            JSONObject surveyCriteria = new JSONObject();
            surveyCriteria.put("base_Location", baseLocation);
            surveyCriteria.put("year_low", year);
            surveyCriteria.put("year_high", year);
            surveyCriteria.put("type_Of_Problem", errorType);

            JSONObject[] surveyObjects = {surveyCriteria};

            FetchSimilarSurveys similarSurveys = new FetchSimilarSurveys();
            similarSurveys.execute(surveyObjects);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        accept = (Button)rootView.findViewById(R.id.accept);
        pastAnomListView = (ListView)rootView.findViewById(R.id.pastAnomListView);

        pastAnomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String surveyInfo = simPastAnomaliesAdap.getItem(i);
                String[] surveyInfoPieces = surveyInfo.split(" - ");

                Intent intent = new Intent(getActivity(), ReportsPastData.class);
                userProfile = SaveSharedPreference.getUserName(getActivity());

                String errorType = surveyInfoPieces[0];
                String alarmName = surveyInfoPieces[1];
                String anomalyDate = surveyInfoPieces[2];

                try{
                    for(int j = 0; j < surveys.length(); j++){
                        JSONObject survey = surveys.getJSONObject(j);
                        String equipAffected = survey.getString("equipAffected");
                        String anomalyType = equipAffected.split("-")[0];

                        String alarm = survey.getString("alarm");
                        String dateOfAnomaly = survey.getString("dateOfAnomaly");

                        if(errorType.equals(anomalyType)
                                && alarmName.equals(alarm)
                                && anomalyDate.equals(dateOfAnomaly)){
                            intent.putExtra("relevantSurvey", survey.toString());
                            break;
                        }

                    }
                }catch(Exception o){

                }

                startActivity(intent);
            }
        });

        Intent intent = getActivity().getIntent();
        String alarmJSON = intent.getStringExtra("currAlarm");
        Log.d("currAlarm", alarmJSON);

        try {
            JSONObject alarmObject = new JSONObject(alarmJSON);

            alarmID = alarmObject.getJSONObject("_id").getString("$oid");
            int severity = alarmObject.getInt("severity");
            String parameter = alarmObject.getString("parameter");

            String[] parameterFields = parameter.split("-");

            String site = parameterFields[1];
            String antenna = parameterFields[2];

            String[] anomalyNameArray = parameterFields[3].split(Pattern.quote("."));
            String fault = anomalyNameArray[1];
            int nameIndex = fault.indexOf("(");
            boolean requiresAcknowledgement = alarmObject.getBoolean("requiresAcknowledgment");

            JSONObject deviceAttributes = alarmObject.getJSONObject("deviceAttributes");
            String affectEquipment = deviceAttributes.getString("DeviceId");
            String vendor = deviceAttributes.getString("DeviceType");
            vendor = vendor.substring(0, vendor.indexOf(" "));

            TextView siteView = (TextView)rootView.findViewById(R.id.site);
            siteView.setText(siteView.getText() + " " + site);

            TextView antennaView = (TextView)rootView.findViewById(R.id.antenna);
            antennaView.setText(antennaView.getText() + " " + antenna);

            TextView alarmView = (TextView)rootView.findViewById(R.id.alarmfault);
            alarmView.setText(alarmView.getText() + " " + fault);

            TextView equipmentAffected = (TextView)rootView.findViewById(R.id.equipmentAffected);
            equipmentAffected.setText(equipmentAffected.getText() + " " + affectEquipment);

            TextView vendorView = (TextView)rootView.findViewById(R.id.equipmentVendor);
            vendorView.setText(vendorView.getText() + " " + vendor);

            TextView severityView = (TextView)rootView.findViewById(R.id.severity);
            severityView.setText(severityView.getText() + " " + severity);

            if(!requiresAcknowledgement) {
                accept.setEnabled(false);
                accept.setOnClickListener(null);
                accept.setBackgroundColor(Color.GRAY);}

            if(nameIndex != -1){
                fault = fault.substring(0,nameIndex);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView tv = (TextView)rootView.findViewById(R.id.detailsText);
        detailString = intent.getStringExtra("details");
        tv.setText(detailString);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FetchAlarmTask verifyAlarm = new FetchAlarmTask();
                verifyAlarm.execute();
            }
        });

        ImageButton home = (ImageButton)rootView.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity(), Anomaly.class);
                startActivity(intent);
            }
        });

        ImageButton back = (ImageButton)rootView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Anomaly.class);
                startActivity(intent);
            }
        });

        ImageButton report = (ImageButton)rootView.findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Reports.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    class FetchSimilarSurveys extends AsyncTask<JSONObject,Void,JSONObject>{

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            JSONObject surveyCriteria = params[0];
            HttpURLConnection client = null;

            try{
                URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/surveyReport");

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                Log.d("surveySearch", surveyCriteria.toString());
                wr.write(surveyCriteria.toString());
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

                    JSONObject pastSurveys = new JSONObject(sb.toString());
                    Log.d("pastSurveys", pastSurveys.toString());
                    return pastSurveys;

                    //System.out.println("" + sb.toString());

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

        @Override
        protected void onPostExecute(JSONObject param){
            try{
                surveys = param.getJSONArray("surveys");

                if(surveys.length() == 0){
                    results.clear();
                    simPastAnomaliesAdap = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, results);
                    pastAnomListView.setAdapter(simPastAnomaliesAdap);
                    Toast.makeText(getContext(), "No surveys found", Toast.LENGTH_SHORT).show();
                }
                else{
                    results.clear();
                    for(int i =0; i < surveys.length(); i++){
                        JSONObject survey = surveys.getJSONObject(i);
                        String affectEquipment = survey.getString("equipAffected");
                        String errorType = affectEquipment.split("-")[0];
                        String anomalyDate = survey.getString("dateOfAnomaly");
                        String alarm = survey.getString("alarm");

                        String intro = errorType + " - " + alarm + " - " + anomalyDate;
                        results.add(intro);
                    }

                    simPastAnomaliesAdap = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, results);
                    pastAnomListView.setAdapter(simPastAnomaliesAdap);
                }
            }catch(Exception o){
                Toast.makeText(getContext(),
                        "JSON data not transferred successfully",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    class FetchAlarmTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void...voids){
            HttpURLConnection client = null;
            BufferedReader reader = null;
            String alarmJSONStr = null;
            try{
                URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/isReqAckd");
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                JSONObject alarmJSON = new JSONObject(getActivity().getIntent().getStringExtra("currAlarm"));
                String oid = alarmJSON.getJSONObject("_id").getString("$oid");
                Log.d("oid", oid);
                JSONObject userInfo = new JSONObject();
                userInfo.put("_id", oid);

                wr.write(userInfo.toString());
                wr.flush();
                wr.close();

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
                    if(sb.toString().length() > 0) {
                        JSONObject response = new JSONObject(sb.toString());
                        Log.d("isReqAckd", response.toString());
                        Boolean uh = response.getBoolean("isReqAckd");
                        Log.d("isReqAckd", uh.toString());
                        return uh;
                    }
                    else return null;
                } else {
                    System.out.println("Server response: " + client.getResponseMessage());
                }
            }catch(Exception o) {
                Log.d("hello", o.toString());
                o.printStackTrace();
            }finally {
                if (client != null) {// Make sure the connection is not null.
                    client.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean isReqAckd) {
            super.onPostExecute(isReqAckd);
            if(isReqAckd){
                Intent intent = new Intent(getActivity(), Survey.class);
                System.out.println("AlarmID: " + alarmID);
                String[] ackID = {alarmID};
                SendACK data = new SendACK();
                data.execute(ackID);

                //Start The Survey Page
                intent.putExtra("alarmOid", alarmID);
                intent.putExtra("currAlarm", getActivity().getIntent().getStringExtra("currAlarm"));
                startActivity(intent);
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(detailString);
                builder.setIcon(R.drawable.exit).show();
                builder.setMessage("Alarm Has Been Acknowledged!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getActivity(), Anomaly.class);
                        startActivity(intent);
                    }
                });
                builder.show();
            }
        }
    }

    class SendACK extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... args){
            Log.d("enter", "entered function");
            HttpURLConnection client = null;

            String alarmID = args[0];

            try{
                alarmACK.put("alarm",alarmID);

                URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/ack");
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                Log.d("currAlarm", alarmACK.toString());
                wr.write(alarmACK.toString());
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
                    alarmACK = new JSONObject(sb.toString());
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