package com.example.alexperez.alarmnotifier;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andrewgarcia on 2/2/17.
 */
class FetchAlarmTaskLoader extends AsyncTaskLoader<JSONObject> {


    public FetchAlarmTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if(AnomalyFragment.alarmJSON == null){
            forceLoad();
        }else{
            super.deliverResult(AnomalyFragment.alarmJSON);
        }

    }

    @Override
    public JSONObject loadInBackground() {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String alarmJSONStr = null;

        try{
            Log.d("IP", AnomalyFragment.IP_ADDRESS);
            URL url = new URL("http://" + AnomalyFragment.IP_ADDRESS + ":8080/UserManagement/MongoService/alarms");
            Log.d("test", url.toString());
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

            alarmJSONStr = buffer.toString();

            try {
                AnomalyFragment.alarmJSON = new JSONObject(alarmJSONStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("alarm", alarmJSONStr);

        }catch(IOException o){
            o.printStackTrace();
        }

        return AnomalyFragment.alarmJSON;
    }

    @Override
    public void deliverResult(JSONObject data) {
        AnomalyFragment.alarmJSON = data;
        super.deliverResult(data);
    }
}