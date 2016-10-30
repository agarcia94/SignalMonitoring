package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ListView lv;
    private ArrayAdapter<String> adapter;
    private JSONObject userProfile = new JSONObject();

    String username = "";
    String stationSite = "";

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        lv = (ListView)rootView.findViewById(R.id.listview);

        ArrayList<String> data = new ArrayList<>();
        data.add("Ground Machine 1");
        data.add("Ground Machine 2");

        TextView welcome = (TextView)rootView.findViewById(R.id.welcome);
        TextView location = (TextView)rootView.findViewById(R.id.location);
        Intent intent = getActivity().getIntent();

        if(intent.getStringExtra("profile") != null){
            String profileString = intent.getStringExtra("profile");
            Log.d("mainprofile",profileString);

            try {
                JSONObject profile = new JSONObject(profileString);
                username = profile.getString("username");
                stationSite = profile.getString("location");

                Log.d("username", username);
                Log.d("location",stationSite);

                welcome.setText("Welcome " + username + "!");
                location.setText(stationSite);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        adapter = new ArrayAdapter(getActivity(), R.layout.row, R.id.textView, data);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    userProfile.put("username",username);
                    userProfile.put("location",stationSite);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getActivity(), Anomaly.class);
                intent.putExtra("anomaly", adapter.getItem(i));
                intent.putExtra("profile", userProfile.toString());
                startActivity(intent);
            }
        });



        return rootView;
    }
}
