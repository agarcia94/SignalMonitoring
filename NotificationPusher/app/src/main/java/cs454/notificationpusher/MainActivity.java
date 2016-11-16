package cs454.notificationpusher;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Push push = new Push();
                push.execute();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class Push extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... args){
            HttpURLConnection connection = null;

            try{

                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "key=AIzaSyAJ2_Gb3Vt_MfZDIaV-BBdwbxrlWOSAs_8");
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                JSONObject jsonParent = new JSONObject();
                jsonParent.put("to", "/topics/location1");
                JSONObject jsonNotification = new JSONObject();
                jsonNotification.put("body", "<alarm details>");
                jsonNotification.put("title", "<app name>");
                jsonNotification.put("icon", "ic_logo");
                jsonNotification.put("sound", "softbells");
                jsonParent.put("notification", jsonNotification);
                out.write(jsonParent.toString());
                out.flush();
                out.close();

                StringBuilder sb = new StringBuilder();
                int HttpResult = connection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    Log.d("goodbye", sb.toString());
                    System.out.println("" + sb.toString());

                } else {
                    Log.d("hello", connection.getResponseMessage());
                    System.out.println("Server response: " + connection.getResponseMessage());
                }


            }catch(Exception o) {
                o.printStackTrace();
            }finally {
                if(connection != null) // Make sure the connection is not null.
                    connection.disconnect();
            }

            return null;
        }
    }
}
