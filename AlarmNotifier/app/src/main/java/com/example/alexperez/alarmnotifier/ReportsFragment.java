package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class ReportsFragment extends Fragment {
    private String userProfile = "";
    private String base_location = "";
    private String search_year = "";

    private ListView surveysList;
    private ArrayAdapter<String> surveysAdapter;
    private ArrayList<String> surveyIntros = new ArrayList<>();

    private JSONArray surveys = null;

    final String IP_ADDRESS = "10.85.47.144";
    //final String IP_ADDRESS = "192.168.1.67";
    //final String IP_ADDRESS = "192.168.1.8";

    public ReportsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_reports, container, false);

        surveysList = (ListView)rootView.findViewById(R.id.surveysView);

        surveysList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String surveyInfo = surveysAdapter.getItem(i);
                String[] surveyInfoPieces = surveyInfo.split(" - ");
//                Log.d("piece1", surveyInfoPieces[0]);
//                Log.d("piece2", surveyInfoPieces[1]);
//                Log.d("piece3", surveyInfoPieces[2]);

                Intent intent = new Intent(getActivity(), ReportsPastData.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile", userProfile);

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
                Intent intent = new Intent(getActivity(), Anomaly.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile", userProfile);
                startActivity(intent);
            }
        });

        Spinner baseLocation = (Spinner)rootView.findViewById(R.id.base_Location);
        baseLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                base_location = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner yearSelection = (Spinner)rootView.findViewById(R.id.year);
        yearSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                search_year = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ImageButton searchButton = (ImageButton)rootView.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText typeOfProb = (EditText)rootView.findViewById(R.id.type_Of_Problem);
                String probString = typeOfProb.getText().toString().trim();

                String[] searchCriteria = new String[3];
                searchCriteria[0] = base_location;
                searchCriteria[1] = search_year;
                searchCriteria[2] = probString;

                FetchSurveys surveyTask = new FetchSurveys();
                surveyTask.execute(searchCriteria);
            }
        });
        return rootView;
    }

    class FetchSurveys extends AsyncTask<String,Void,JSONObject>{
        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection client = null;

            String location = params[0];
            String year = params[1];
            String probType = params[2];

            try{
                URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/surveyReport");

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                JSONObject surveyCriteria = new JSONObject();
                surveyCriteria.put("base_Location", location);
                surveyCriteria.put("year", year);

                if(!probType.isEmpty())
                    surveyCriteria.put("type_Of_Problem", probType);

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
                    surveyIntros.clear();
                    surveysAdapter = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, surveyIntros);
                    surveysList.setAdapter(surveysAdapter);
                    Toast.makeText(getContext(), "No surveys found", Toast.LENGTH_SHORT).show();
                }
                else{
//                    Intent intent = new Intent(getActivity(), ReportsPastData.class);
//                    userProfile = getActivity().getIntent().getStringExtra("profile");
//                    intent.putExtra("profile",userProfile);
//                    intent.putExtra("surveyData", surveys.toString());
//                    startActivity(intent);

                    surveyIntros.clear();
                    for(int i =0; i < surveys.length(); i++){
                        JSONObject survey = surveys.getJSONObject(i);
                        String affectEquipment = survey.getString("equipAffected");
                        String errorType = affectEquipment.split("-")[0];
                        String anomalyDate = survey.getString("dateOfAnomaly");
                        String alarm = survey.getString("alarm");

                        String intro = errorType + " - " + alarm + " - " + anomalyDate;
                        surveyIntros.add(intro);
                    }

                    surveysAdapter = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, surveyIntros);
                    surveysList.setAdapter(surveysAdapter);
                }
            }catch(Exception o){
                Toast.makeText(getContext(),
                        "JSON data not transferred successfully",
                        Toast.LENGTH_SHORT).show();
            }



        }
    }


}