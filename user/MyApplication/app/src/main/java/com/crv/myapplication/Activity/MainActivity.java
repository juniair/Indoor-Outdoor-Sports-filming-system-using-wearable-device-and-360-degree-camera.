package com.crv.myapplication.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crv.myapplication.R;
import com.crv.myapplication.app.AppConfig;
import com.crv.myapplication.app.AppController;
import com.crv.myapplication.app.BackPressCloseHandler;
import com.crv.myapplication.app.SQLiteHandler;
import com.crv.myapplication.app.SessionManager;
import com.crv.myapplication.app.Sglobal;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private BackPressCloseHandler backPressCloseHandler;

    EditText idT;
    EditText pwT;
    Button btnPost;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idT = (EditText) findViewById(R.id.input_id);
        pwT = (EditText) findViewById(R.id.input_pw);
        btnPost = (Button) findViewById(R.id.login_btn);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        btnPost.setOnClickListener(mClickListener);
        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        backPressCloseHandler = new BackPressCloseHandler(this);
    }

    // 버튼 클릭시 login()함수 호출
    Button.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            String userId = idT.getText().toString().trim();
            String userPw = pwT.getText().toString().trim();

            // check for empty data in the form
            if (userId.isEmpty() || userPw.isEmpty()) {   // its empty
                Toast.makeText(getApplicationContext(),
                        "빈 칸없이 적어주십시오", Toast.LENGTH_LONG).show();
            } else {   // isnt empty
                checkLogin(userId, userPw);
            }
        }
    };

    private void checkLogin(final String userID, final String userPW) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if(!error)
                    {
                        Log.d("No error","!!!!!");
                    }
                    else
                    {
                        Log.d("Error","!!!!!");
                    }

                    // check for error node in json
                    if (!error) {
                        session.setLogin(true);

                        JSONObject user = jObj.getJSONObject("user");
                        int idx = user.getInt("idx");
                        String name = user.getString("name");
                        db.addUser(idx, name);

                        HashMap<String, String> user1 = db.getUserDetails();

                        Sglobal.idx = idx;
                        Sglobal.name = user1.get("name");

                        AppController.getInstance().setIdx(String.valueOf(Sglobal.idx));

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Json error : " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("userId", userID);
                params.put("userPw", userPW);

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.crv.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.crv.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();
    }

}

