package com.example.alexperez.alarmnotifier;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity {

    private Spinner limitSpinner;
    private Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle("Settings");
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // ----------------------- Set up limit spinner ------------------------------//
        limitSpinner = (Spinner) findViewById(R.id.limit);
        //final Spinner historySpinner = (Spinner)findViewById(R.id.history_spinner);
        ArrayAdapter<CharSequence> limAdapter = ArrayAdapter.createFromResource(this, R.array.limit, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        limAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        limitSpinner.setAdapter(limAdapter);
        limitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SaveSharedPreference.setLimitPosition(ctx, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // do nothing
            }

        });
        limitSpinner.setSelection(SaveSharedPreference.getLimitPosition(this));
        // ------------------------- limit Spinner ----------------------------------//

        /*String[] history_Days = new String[]{
                "Select One",
                "Today",
                "Past 2 Days",
                "Past 3 Days",
                "Past 4 Days",
                "Past 5 Days",
                "Past 6 Days",
                "Past 7 Days"
        };

        final List<String> historyList = new ArrayList<>(Arrays.asList(history_Days));

        final ArrayAdapter<String> historyArrayAdapter = new ArrayAdapter<String>(this,R.layout.spinner_item,historyList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray, The first item is the placeholder
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    //Differentiate the two
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        historyArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        assert historySpinner != null; //Base Case?? Provided by Android
        historySpinner.setAdapter(historyArrayAdapter);

        historySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text
                    Toast.makeText
                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do Nothing, as nothing should happen if nothing is selected yet
            }
        });*/
    }

}
