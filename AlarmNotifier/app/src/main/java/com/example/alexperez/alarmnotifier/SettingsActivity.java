package com.example.alexperez.alarmnotifier;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
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
        mDrawerList = (ListView)findViewById(R.id.navList);

        addDrawerItems();

    }

    private void addDrawerItems() {
        final TypedArray typedArray = getResources().obtainTypedArray(R.array.sections_icons_settings);
        String[] array = { "          Home", "          Reports", "          Comparison",
                "          Subscription", "          Logout" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                array
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                int resourceId = typedArray.getResourceId(position, 0);
                Drawable drawable = getResources().getDrawable(resourceId);
                ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                return v;
            }
        });


        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter.getItem(position).equals("          Home")) {
                    Intent i = new Intent(getApplicationContext(), Anomaly.class);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("          Comparison")) {
                    Intent i = new Intent(getApplicationContext(), ComparisonsReports.class);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("          Reports")) {
                    Intent i = new Intent(getApplicationContext(), Reports.class);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("          Logout")) {
                    Toast.makeText(SettingsActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                    SaveSharedPreference.clearUserName(getApplicationContext());
                    Intent i = new Intent(getApplicationContext(), Login.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (mAdapter.getItem(position).equals("          Subscription")) {
                    Intent i = new Intent(getApplicationContext(), SubscribeActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(SettingsActivity.this, "Error On Calling", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
