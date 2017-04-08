package com.example.alexperez.alarmnotifier;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReportsFragment extends Fragment {
    private String base_location = "";
    private String search_year = "";
    private RangeBar rb;
    private ListView surveysList;
    private ArrayAdapter<String> surveysAdapter;
    private ArrayList<String> surveyIntros = new ArrayList<>();
    private Button btn_search;
    private TextView resultTxt;
    private TextView resultCount;
    private ScrollView sv;
    private JSONArray surveys = null;

    public ReportsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_reports, container, false);
        surveysList = (ListView)rootView.findViewById(R.id.surveysView);
        sv = (ScrollView) rootView.findViewById(R.id.scroll);
        rb = (RangeBar) rootView.findViewById(R.id.yearRange);
        rb.setTickEnd(Calendar.getInstance().get(Calendar.YEAR));
        rb.setRight(Calendar.getInstance().get(Calendar.YEAR));
        rb.setTickStart(2000);
        rb.setLeft(2000);
        final NumberPicker startRange = (NumberPicker) rootView.findViewById(R.id.editRangeStart);
        final NumberPicker endRange = (NumberPicker) rootView.findViewById(R.id.editRangeEnd);
        resultTxt = (TextView) rootView.findViewById(R.id.txt_result);
        resultCount = (TextView) rootView.findViewById(R.id.result_count);
        resultTxt.setVisibility(View.GONE);
        btn_search = (Button) rootView.findViewById(R.id.btn_search);
        final RelativeLayout criteria = (RelativeLayout) rootView.findViewById(R.id.criteria);

        startRange.setValue(Integer.parseInt(rb.getLeftPinValue()));
        //Set the minimum value of NumberPicker
        endRange.setMinValue(startRange.getValue());
        //Specify the maximum value/number of NumberPicker
        endRange.setMaxValue(Calendar.getInstance().get(Calendar.YEAR));

        endRange.setValue(rb.getRight());
        //Gets whether the selector wheel wraps when reaching the min/max value.
        endRange.setWrapSelectorWheel(true);
        //Set a value change listener for NumberPicker
        endRange.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //Display the newly selected number from picker
                //tv.setText("Selected Number : " + newVal);
                rb.setRangePinsByValue(startRange.getValue(), newVal);
                startRange.setMaxValue(newVal);
            }
        });

        //Set the minimum value of NumberPicker
        startRange.setMinValue(2000);
        endRange.setMinValue(startRange.getValue());
        //Specify the maximum value/number of NumberPicker
        startRange.setMaxValue(endRange.getValue());

        //Gets whether the selector wheel wraps when reaching the min/max value.
        startRange.setWrapSelectorWheel(true);

        //Set a value change listener for NumberPicker
        startRange.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //Display the newly selected number from picker
                //tv.setText("Selected Number : " + newVal);
                rb.setRangePinsByValue(newVal, endRange.getValue());
                endRange.setMinValue(newVal);
            }
        });

        surveysList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String surveyInfo = surveysAdapter.getItem(i);
                String[] surveyInfoPieces = surveyInfo.split(" - ");

                Intent intent = new Intent(getActivity(), ReportsPastData.class);

                String errorType = surveyInfoPieces[0];
                String alarmName = surveyInfoPieces[1];
                String anomalyDate = surveyInfoPieces[2];

                try {
                    for (int j = 0; j < surveys.length(); j++) {
                        JSONObject survey = surveys.getJSONObject(j);
                        String equipAffected = survey.getString("equipAffected");
                        String anomalyType = equipAffected.split("-")[0];

                        String alarm = survey.getString("alarm");
                        String dateOfAnomaly = survey.getString("dateOfAnomaly");

                        if (errorType.equals(anomalyType)
                                && alarmName.equals(alarm)
                                && anomalyDate.equals(dateOfAnomaly)) {
                            intent.putExtra("relevantSurvey", survey.toString());
                            break;
                        }
                    }
                } catch (Exception o) {
                }
                startActivity(intent);
            }
        });
        surveysList.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        ImageButton home = (ImageButton)rootView.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Anomaly.class);
                startActivity(intent);
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                // toggle hide criteria
                if(criteria.getVisibility()==View.GONE){
                    criteria.animate()
                            .translationY(0).alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    super.onAnimationStart(animation);
                                    criteria.setVisibility(View.VISIBLE);
                                    criteria.setAlpha(0.0f);
                                }
                            });
                    btn_search.setText("Search");

                }else{
                    criteria.animate()
                            .translationY(-0.3f*criteria.getHeight()).alpha(0.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    criteria.setVisibility(View.GONE);
                                }
                            });
                    sv.setEnabled(false);
                    btn_search.setText("New search");
                    EditText typeOfProb = (EditText)rootView.findViewById(R.id.type_Of_Problem);
                    String probString = typeOfProb.getText().toString().trim().toUpperCase();

                    String[] searchCriteria = new String[4];
                    searchCriteria[0] = base_location;
                    searchCriteria[1] = rb.getLeftPinValue();
                    searchCriteria[2] = rb.getRightPinValue();
                    searchCriteria[3] = probString;

                    FetchSurveys surveyTask = new FetchSurveys();
                    surveyTask.execute(searchCriteria);

                }
            }
        });

        ImageButton back = (ImageButton)rootView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
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

        //Spinner yearSelection = (Spinner)rootView.findViewById(R.id.year);
        /*yearSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                search_year = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        //ImageButton searchButton = (ImageButton)rootView.findViewById(R.id.searchButton);
        /*searchButton.setOnClickListener(new View.OnClickListener() {
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
        });*/

        rb.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex, String leftPinValue, String rightPinValue) {
                startRange.setValue(Integer.parseInt(leftPinValue));
                endRange.setValue(Integer.parseInt(rightPinValue));
                endRange.setMinValue(Integer.parseInt(leftPinValue));
                startRange.setMaxValue(Integer.parseInt(rightPinValue));
            }
        });
        return rootView;
    }

    class FetchSurveys extends AsyncTask<String,Void,JSONObject>{
        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection client = null;

            String location = params[0];
            String year_low = params[1];
            String year_high = params[2];
            String probType = params[3];

            try{
                URL url = new URL("http://cs3.calstatela.edu:8080/cs4961stu20/MongoService/surveyReport");

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                JSONObject surveyCriteria = new JSONObject();
                surveyCriteria.put("base_Location", location);
                surveyCriteria.put("year_low", year_low );
                surveyCriteria.put("year_high", year_high);

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
                resultTxt.setVisibility(View.VISIBLE);
                resultCount.setText("There are total of " + surveys.length() + " result(s) found in database.");
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