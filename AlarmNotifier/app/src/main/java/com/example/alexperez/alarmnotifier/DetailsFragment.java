package com.example.alexperez.alarmnotifier;

import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class DetailsFragment extends Fragment {
    private ArrayList<String> results = new ArrayList<String>();

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchSurveyTask task = new FetchSurveyTask();
        task.execute();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        Intent intent = getActivity().getIntent();
        String detailString = intent.getStringExtra("details");

        TextView tv = (TextView)rootView.findViewById(R.id.detailsText);
        tv.setText(detailString);

        Button report = (Button)rootView.findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), results.get(0), Toast.LENGTH_LONG).show();
            }
        });

        Button accept = (Button)rootView.findViewById(R.id.accept);
        accept.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity(), Survey.class);
                //intent.putExtra("details", adapter.getItem(i));
                startActivity(intent);
            }
        });

        Button home = (Button)rootView.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });


        return rootView;
    }

    class FetchSurveyTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(Void...voids){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String surveyJSONStr = null;
            String[] resultArray = null;

            try{
                URL url = new URL("http://192.168.1.67:8080/UserManagement/rest/MongoService/alarms");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                surveyJSONStr = buffer.toString();
                resultArray = getSurveyDataFromJson(surveyJSONStr);

                if(results.isEmpty()){
                    results.addAll(Arrays.asList(resultArray));
                }
                else{
                    results.clear();
                    results.addAll(Arrays.asList(resultArray));
                }


                Log.d("lengthResults", String.valueOf(results.size()));
            }catch(IOException o){
                o.printStackTrace();
            }



            return resultArray;

        }

        private String[] getSurveyDataFromJson(String surveyJsonStr){
            String[] resultStr = new String[1];

            try{
                JSONObject jsonObject = new JSONObject(surveyJsonStr);
                String resolution = jsonObject.getString("resolution");

                for(int i=0; i < resultStr.length; i++){
                    resultStr[i] = resolution;
                }
            }catch(Exception o){
                o.printStackTrace();
            }

            return resultStr;
        }


    }


}
