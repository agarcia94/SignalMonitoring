package com.example.alexperez.alarmnotifier;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrewgarcia on 2/2/17.
 */
class FetchAlarmTaskLoader extends AsyncTaskLoader<JSONObject> {

    private Context ctx;

    public FetchAlarmTaskLoader(Context context) {
        super(context);
        ctx = context;
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
        HttpURLConnection client = null;
        BufferedReader reader = null;
        JSONObject jsonResponse = null;
        String alarmJSONStr = null;
        JSONObject jsonParent = null;
        JSONObject locInfo = new JSONObject();
        JSONArray subs = new JSONArray();
        List<String> list = new ArrayList<String>();

        try {
            locInfo = new JSONObject(SaveSharedPreference.getUserName(ctx));
            subs = locInfo.getJSONArray("subs");
            jsonParent = new JSONObject().put("alarms", new JSONArray());
            for (int i=0; i<subs.length(); i++) {
                list.add( subs.getString(i) );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < list.size(); i++){
            try{
                URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/report");
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                JSONObject userInfo = new JSONObject();
                userInfo.put("location", list.get(i));

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
                    if(sb.toString().length() > 0){
                        jsonResponse = new JSONObject(sb.toString());
                        JSONArray currResponse = jsonResponse.getJSONArray("alarms");
                        JSONArray temp = new JSONArray();
                        temp = concatArray(jsonParent.getJSONArray("alarms"), currResponse);
                        jsonParent.put("alarms", temp);
                    }

                } else {
                    System.out.println("Server response: " + client.getResponseMessage());
                }


            }catch(Exception o) {
                Log.d("hello", o.toString());
                o.printStackTrace();
            }finally {
                if(client != null) // Make sure the connection is not null.
                    client.disconnect();
            }

        }

        AnomalyFragment.alarmJSON = jsonParent;
        return jsonParent;
    }

    @Override
    public void deliverResult(JSONObject data) {
        AnomalyFragment.alarmJSON = data;
        super.deliverResult(data);
    }

    private JSONArray concatArray(JSONArray... arrs)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrs) {
            for (int i = 0; i < arr.length(); i++) {
                result.put(arr.get(i));
            }
        }
        return result;
    }
}