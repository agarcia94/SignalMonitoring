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
        super.onStartLoading();
    }

    @Override
    public JSONObject loadInBackground() {
        HttpURLConnection client = null;
        BufferedReader reader = null;
        JSONObject jsonResponse = null;
        String alarmJSONStr = null;
        JSONObject locInfo = new JSONObject();
        JSONArray subs = new JSONArray();
        List<String> list = new ArrayList<String>();

        try {
            locInfo = new JSONObject(SaveSharedPreference.getUserName(ctx));
            subs = locInfo.getJSONArray("subs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try{
            URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/anomalies");
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Content-Type", "application/json");
            client.setRequestProperty("Accept", "application/json");
            client.setDoOutput(true);

            OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
            JSONObject userInfo = new JSONObject();
            userInfo.put("locationArray", subs);
            userInfo.put("limit", SaveSharedPreference.getLimit(getContext()));
            System.out.println("To server: "+ userInfo.toString());
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
                if(sb.toString().length() > 0)
                    jsonResponse = new JSONObject(sb.toString());
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
        return jsonResponse;
    }

    @Override
    public void deliverResult(JSONObject data) {
        super.deliverResult(data);
    }

}