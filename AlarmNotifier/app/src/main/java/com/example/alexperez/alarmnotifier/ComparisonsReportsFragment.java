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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * A placeholder fragment containing a simple view.
 */
public class ComparisonsReportsFragment extends Fragment {
    private String userProfile = "";
    Spinner v1, v2, f1, f2, r1, r2;
    TextView u1, u2, a1, a2, unit1, unit2, rate1, rate2, mrf1, mrf2;
    public ComparisonsReportsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_comparisons_reports, container, false);

        v1 = (Spinner) rootView.findViewById(R.id.vendorSpinner);
        v2 = (Spinner) rootView.findViewById(R.id.vendorSpinner2);
        f1 = (Spinner) rootView.findViewById(R.id.freqSpinner);
        f2 = (Spinner) rootView.findViewById(R.id.freqSpinner2);
        r1 = (Spinner) rootView.findViewById(R.id.rangeSpinner);
        r2 = (Spinner) rootView.findViewById(R.id.rangeSpinner2);

        u1 = (TextView) rootView.findViewById(R.id.unit_In_Fleet);
        u2 = (TextView) rootView.findViewById(R.id.unit_In_Fleet2);
        a1 = (TextView) rootView.findViewById(R.id.anomalies_Values);
        a2 = (TextView) rootView.findViewById(R.id.anomalies_Values2);
        unit1 = (TextView) rootView.findViewById(R.id.units_Affected);
        unit2 = (TextView) rootView.findViewById(R.id.units_Affected2);
        rate1 = (TextView) rootView.findViewById(R.id.anomaly_Rate);
        rate2 = (TextView) rootView.findViewById(R.id.anomaly_Rate2);
        mrf1 = (TextView) rootView.findViewById(R.id.mrf);
        mrf2 = (TextView) rootView.findViewById(R.id.mrf2);


        ImageButton home = (ImageButton)rootView.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity(), Anomaly.class);
                userProfile = getActivity().getIntent().getStringExtra("profile");
                intent.putExtra("profile",userProfile);
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

        Button compareButton = (Button) rootView.findViewById(R.id.compareButton);
        compareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    // gather criteria
                    JSONObject mainObj = new JSONObject();

                    JSONObject left = new JSONObject();
                    left.put("v", v1.getSelectedItem().toString());
                    left.put("f", f1.getSelectedItem().toString());
                    left.put("r", r1.getSelectedItem().toString());

                    JSONObject right = new JSONObject();
                    right.put("v", v2.getSelectedItem().toString());
                    right.put("f", f2.getSelectedItem().toString());
                    right.put("r", r2.getSelectedItem().toString());

                    mainObj.put("left", left);
                    mainObj.put("right", right);

                    // do task
                    CompareTask ct = new CompareTask();
                    ct.execute(mainObj);
                }catch (Exception e){

                }

            }
        });
        return rootView;
    }

    class CompareTask extends AsyncTask<JSONObject,Void,JSONObject> {

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            JSONObject criteria = params[0];
            HttpURLConnection client = null;

            try{
                URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/comparison");

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                Log.d("compareSearch", criteria.toString());
                wr.write(criteria.toString());
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

                    JSONObject result = new JSONObject(sb.toString());
                    Log.d("pastSurveys", result.toString());
                    return result;

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
        protected void onPostExecute(JSONObject result){
           // got result, update view ( left and right )
            Log.d("comparison", result.toString());
            try{
                JSONObject left = result.getJSONObject("left");
                JSONObject right = result.getJSONObject("right");
                String l_affected = left.getString("affected");
                String r_affected = right.getString("affected");
                String l_count = left.getString("totalCount");
                String r_count = right.getString("totalCount");
                String l_mrf = left.getString("MRF");
                String r_mrf = right.getString("MRF");
                int l_uif = left.getInt("uif");
                int r_uif = right.getInt("uif");
                double l_rate = left.getDouble("rate") * 100;
                double r_rate = right.getDouble("rate") * 100;
                DecimalFormat df = new DecimalFormat("####0.00");

                a1.setText("Anomalies: " + l_count);
                a2.setText("Anomalies: " + r_count);
                unit1.setText("Units Affected: " + l_affected);
                unit2.setText("Units Affected: " + r_affected);
                mrf1.setText("Most Recurring Fault: \n\t" + l_mrf);
                mrf2.setText("Most Recurring Fault: \n\t" + r_mrf);
                u1.setText("Unit In Fleet: " + l_uif);
                u2.setText("Unit In Fleet: " + r_uif);
                rate1.setText("Anomaly Rate: " + df.format(l_rate));
                rate2.setText("Anomaly Rate: " + df.format(r_rate));

            }catch(Exception e){
                Log.d("compareOnpostEx", e.toString());
            }

        }
    }
}
