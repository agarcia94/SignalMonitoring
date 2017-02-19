package com.example.alexperez.alarmnotifier;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.json.JSONObject;

/**
 * Created by andrewgarcia on 2/11/17.
 */
public class PastReportsLoader extends AsyncTaskLoader<JSONObject> {

    public PastReportsLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
    }

    @Override
    public JSONObject loadInBackground(){
        return null;
    }

    @Override
    public void deliverResult(JSONObject data) {
        super.deliverResult(data);
    }
}
