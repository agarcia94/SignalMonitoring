package com.example.alexperez.alarmnotifier;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment {
    private boolean match = false;
    private JSONObject userProfile = new JSONObject();
    View rootView = null;

    final String IP_ADDRESS = "192.168.1.6";
    //final String IP_ADDRESS = "10.85.41.232";
    //final String IP_ADDRESS = "192.168.1.8";

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //final View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        rootView = inflater.inflate(R.layout.fragment_login, container, false);

        Button login = (Button)rootView.findViewById(R.id.login);

        final EditText username = (EditText)rootView.findViewById(R.id.userInfo);
        final EditText password = (EditText)rootView.findViewById(R.id.passwordInfo);
        password.setFocusableInTouchMode(true);
        username.requestFocus();

        password.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {


                    String userInfo = username.getText().toString();
                    String passwordInfo = password.getText().toString();

                    String[] userFacts = {userInfo, passwordInfo};

                    //SendUserData data = new SendUserData(getContext(),"");
                    SendUserData data = new SendUserData();
                    data.execute(userFacts);

                    return true;
                }
                return false;
            }
        });


        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                EditText username = (EditText)rootView.findViewById(R.id.userInfo);
                String userInfo = username.getText().toString();
                EditText password = (EditText)rootView.findViewById(R.id.passwordInfo);
                String passwordInfo = password.getText().toString();

                String[] userFacts = {userInfo, passwordInfo};

                SendUserData data = new SendUserData();
                data.execute(userFacts);
                
            }
        });

        Button register = (Button)rootView.findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://" + IP_ADDRESS +":8080/UserManagement/MongoService/register"));


                startActivity(browserIntent);
            }
        });
        return rootView;
    }


    class SendUserData extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... args){
            Log.d("enter", "entered function");
            HttpURLConnection client = null;

            String username = args[0];
            String password = args[1];

            try{
                userProfile.put("username",username);
                userProfile.put("password", password);

                //URL url = new URL("http://192.168.43.253:8080/UserManagement/MongoService/login");
                URL url = new URL("http://" + IP_ADDRESS + ":8080/UserManagement/MongoService/login");

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");
                client.setDoOutput(true);

                OutputStreamWriter wr= new OutputStreamWriter(client.getOutputStream());
                Log.d("profile", userProfile.toString());
                wr.write(userProfile.toString());
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
                    Log.d("goodbye", sb.toString());
                    userProfile = new JSONObject(sb.toString());
                    System.out.println("" + sb.toString());

                    if(sb.toString().contains("true"))
                        match = true;
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
        protected void onPostExecute(Void item){
            if(match){
                match = false; //Set match to false to reset the match for the next user
                Toast.makeText(rootView.getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                SaveSharedPreference.setNameAppendSubs(getActivity(), userProfile.toString());
                Log.d("SSP", "this is " + SaveSharedPreference.getUserName(getActivity()));
                Intent intent = new Intent(getActivity(), Anomaly.class);
                intent.putExtra("profile", userProfile.toString());
                startActivity(intent);
            }
            else{
                Toast.makeText(rootView.getContext(), "No record found", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
