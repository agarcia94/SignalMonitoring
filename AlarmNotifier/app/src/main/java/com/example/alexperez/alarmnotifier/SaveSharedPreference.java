package com.example.alexperez.alarmnotifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yao on 1/20/2017.
 */
public class SaveSharedPreference {

    static final String PREF_USER_NAME= "username";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setNameAppendSubs(Context ctx, String userName){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        String userLocation = "";
        try{
            userLocation = new JSONObject(userName).getString("location");
        }catch(Exception e) {

        }
        editor.putString(PREF_USER_NAME, userName.substring(0, userName.length()-1) +
                ",'subs':['" + userLocation +"'], 'limit':'10'}");
        Log.d("SSP", "user = " + PREF_USER_NAME);
        editor.commit();
    }

    public static void setUserName(Context ctx, String userName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.commit();
    }

    public static String getUserName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
    }

    public static String getName(Context ctx){
        try {
            JSONObject userInfo = new JSONObject(getUserName(ctx));
            String username = userInfo.getString("username");
            return username;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getLoc(Context ctx){
        try {
            JSONObject userInfo = new JSONObject(getUserName(ctx));
            String location = userInfo.getString("location");
            return location;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void clearUserName(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        unsubscribeAll(ctx);
        editor.clear(); //clear all stored data
        editor.commit();
    }

    private static void unsubscribeAll(Context ctx)
    {
        JSONArray subs;
        try {
            JSONObject userInfo = new JSONObject(getUserName(ctx));
            subs = userInfo.getJSONArray("subs");
            for(int i = 0; i < subs.length(); i++){
                FirebaseMessaging.getInstance().unsubscribeFromTopic(subs.getString(i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void subscribingTo(Context ctx, String location){
        //
        JSONObject userInfo = new JSONObject();
        try {
            userInfo = new JSONObject(getUserName(ctx));
            JSONArray subs = userInfo.getJSONArray("subs");
            subs.put(location);
            userInfo.remove("subs");
            userInfo.put("subs", subs);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        setUserName(ctx, userInfo.toString());
        Log.d("SSP", "user = " + getUserName(ctx));
    }

    public static void unsubscribingTo(Context ctx, String location){
        JSONArray subs;
        JSONObject userInfo = null;
        try {
            userInfo = new JSONObject(getUserName(ctx));
            subs = userInfo.getJSONArray("subs");
            for(int i = 0; i < subs.length(); i++){
                if(subs.getString(i).equals(location)) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(subs.getString(i));
                    subs.remove(i);
                }
            }
            userInfo.remove("subs");
            userInfo.put("subs", subs);
            setUserName(ctx, userInfo.toString());
            Log.d("SSP", "unsub username = " + getUserName(ctx));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static String[] getSubLocations(Context ctx){
        String[] locations;
        JSONArray subs;
        JSONObject userInfo = null;
        try {
            userInfo = new JSONObject(getUserName(ctx));
            subs = userInfo.getJSONArray("subs");
            List<String> list = new ArrayList<String>();
            for (int i=0; i<subs.length(); i++) {
                list.add( subs.getString(i) );
            }
            String[] stringArray = list.toArray(new String[list.size()]);
            return stringArray;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String [] empty = new String[0];
        return empty;
    }

    public static int getLimitPosition(Context ctx){
        try {
            JSONObject userInfo = new JSONObject(getUserName(ctx));
            int lim = userInfo.getInt("limit");
            if (lim == 1){
                return 0;
            }
            else{
                return lim / 5;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setLimitPosition(Context ctx, int position){
        try {
            int val;
            if (position == 0){
                val = 1;
            }else{
                val = position * 5;
            }
            JSONObject userInfo = new JSONObject(getUserName(ctx));
            userInfo.remove("limit");
            userInfo.put("limit", val);
            setUserName(ctx, userInfo.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static int getLimit(Context ctx){
        try{
            JSONObject userInfo = new JSONObject(getUserName(ctx));
            int lim = userInfo.getInt("limit");
            return lim;
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return 5;
    }
}
