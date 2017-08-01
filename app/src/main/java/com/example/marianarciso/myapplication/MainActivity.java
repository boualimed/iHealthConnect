package com.example.marianarciso.myapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.content.DialogInterface;
import android.util.Log;
import android.util.Base64;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button btnGetData;
    TextView txtData;

    //Constantes:
    private static final String TAG = "MyActivity";
     // private static final String AUTH_URL ="https://account.withings.com/oauth2_device/authorize2?client_id=%s" /*"https://www.reddit.com/api/v1/authorize.compact?client_id=%s" */ +
        //      "&response_type=code&state=%s&redirect_uri=%s&" + /*"&response_type=code&state=%s&redirect_uri=%s&"*/
          //    "scope=identity"; //scope=device.info,device.environment"; "duration=permanent&scope=identity"*/



      private static final String AUTH_URL =
              "http://sandboxapi.ihealthlabs.com/api/OAuthv2/userauthorization.ashx?client_id=%s"+
               "&response_type=code&redirect_uri=%s&APIName=OpenApiBP";

    public String ACCESS_TOKEN = "";
    public String TOKEN_TYPE = "";
    public String REFRESH_TOKEN = "";
    public boolean tokenExpired = false;

    private static final String CLIENT_ID = "2b0527a8ffec42da8d45854f4e643026"; //"3pnfjKhFERD4JA"; //"71d055bb8b448d753e4b0d8eb00f164dc737237f319145bd84de71707c72694"
    private static final String SECRET = "51aa368a81d54906ada57fc828e45f4d";	//"21bde44de705439020d8cf44b3e342b66408335380d1e283565fe8128";

    private static final String STATE = "MY_RANDOM_STRING_1";
    private static final String ACCESS_TOKEN_URL = "https://api.ihealthlabs.com:8443/OpenApiV2/OAuthv2/userauthorization";

    private static final String REDIRECT_URI = "http://0d1197f7.ngrok.io/polls";
    private int main_menu;


    public static void putPref(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getPref(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }


    @Override
    protected void onStart() {
        super.onStart();

        //show saved token
        String token = getPref("accessToken",getApplicationContext());
        Toast.makeText(getApplicationContext(), token,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnGetData = (Button) findViewById(R.id.button3);
        txtData = (TextView) findViewById(R.id.textViewData);


        // If the user is not already logged in, go to the official login page
        String Logged = getPref("loggedIn", getApplicationContext());

        if( Logged.equals("false"))
        {
           final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);

            builder.setPositiveButton(
                          "OK",
                    new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                            startSignIn();


                        }
                    });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog -> back to the main page
                    dialog.dismiss();

                }
            });
            builder.setMessage("Tem de fazer Login na sua conta iHealth").create().show();
        }

        else
        {btnGetData.setVisibility(View.VISIBLE);}




        //Received Json
        btnGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                StringRequest stringRequest = new StringRequest(Request.Method.GET,"https://api.ihealthlabs.com:8443/openapiv2/user/{0}/weight.json",//"http://wbsapi.withings.net/measure?action=getmeas&userid=13672201",
                        //"https://oauth.reddit.com/api/v1/me",

                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                txtData.setText(response);

                                try {
                                    JSONObject responseArray = new JSONObject(response);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                requestQueue.stop();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(),Toast.LENGTH_LONG).show();
                        //tokenExpired = true;
                        //getAccessToken("");

                    }
                }){@Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    {
                        String ACCESS_TOKEN = getPref("accessToken",getApplicationContext());
                        String TOKEN_TYPE = getPref("tokenType",getApplicationContext());
                    headers.put("Authorization",TOKEN_TYPE +" " + ACCESS_TOKEN);

                    return headers;
                }}};

                requestQueue.add(stringRequest);

            }
        });}


    @Override
    protected void onResume() {
        super.onResume();

       /* wv1.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {

                String code = url.toString();
                Toast.makeText(getApplicationContext(),code,Toast.LENGTH_LONG).show();

            }
        });*/

        if(getIntent()!=null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();
            if(uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                Log.e(TAG, "An error has occurred : " + error);
            } else {
                String state = uri.getQueryParameter("state");
                if(state.equals(STATE)) {
                    final String code = uri.getQueryParameter("code");
                    Toast.makeText(getApplicationContext(),code,Toast.LENGTH_LONG).show();
                    getAccessToken(code);
                }
            }
        }

    }


    public void startSignIn() {

        String url = String.format(AUTH_URL, CLIENT_ID, STATE, REDIRECT_URI);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);


    }




    private void getAccessToken(final String code) {

    final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,ACCESS_TOKEN_URL,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject responseArray = new JSONObject(response);


                            String ACCESS_TOKEN = responseArray.getString("access_token");
                            putPref("accessToken",ACCESS_TOKEN, getApplicationContext());

                            String TOKEN_TYPE = responseArray.getString("token_type");
                            putPref("tokenType",TOKEN_TYPE, getApplicationContext());


                            String EXPIRES_IN = responseArray.getString("expires_in");
                            REFRESH_TOKEN = responseArray.getString("refresh_token");
                            String SCOPE = responseArray.getString("scope");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        requestQueue.stop();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(),Toast.LENGTH_LONG).show();

            }
        }){


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> params = new HashMap<String, String>();

                if (tokenExpired) //expired_token	Token is expired	4001
                {
                    params.put("grant_type",    "refresh_token");
                    params.put("code",          code);
                    params.put("redirect_uri",  REDIRECT_URI);
                    params.put("refresh_token", REFRESH_TOKEN);




                }

                else{

                params.put("grant_type",    "authorization_code");
                params.put("code",          code);
                params.put("redirect_uri",  REDIRECT_URI);}

                return params;
        }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String authString = CLIENT_ID + ":";
                String auth = "Basic "
                        + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
                headers.put("User-Agent", "Ximi");
                //headers.put("Content-Type", "");
                headers.put("Authorization", auth);
                return headers;
            }

        };

                requestQueue.add(stringRequest);

        btnGetData.setVisibility(View.VISIBLE);
        putPref("loggedIn", "true", getApplicationContext());

    }



    public class JSONTask extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null ){
                    buffer.append(line);
                }
                String finalJson = buffer.toString();
                return finalJson;

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try{
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

 return null;
        }

@Override
    protected  void onPostExecute(String result){
    super.onPostExecute(result);
    txtData.setText(result);
}


    }

    //In order to click on any link inside the webpage of the WebView and allow the new page to be loaded inside the WebView,
    // we need to extend the class from WebViewClient and override its method.
    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_exit:
                finish();
                break;
            case R.id.action_logout:
                 item.setVisible(false);
                 putPref("loggedIn", "false", getApplicationContext());

                //REVOKE: Revoking a refresh token will also revoke any related access tokens!
                final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://wbsapi.withings.net/v2/notify?action=revoke&callbackurl=http://example.com&appli=45/span>access_token=YOUR-ACCESS-TOKEN",
                        /*"https://www.reddit.com/api/v1/revoke_token"*/
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                try {
                                    JSONObject responseArray = new JSONObject(response);
                                    Toast.makeText(getApplicationContext(), response.toString(),Toast.LENGTH_LONG).show();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                requestQueue.stop();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(),Toast.LENGTH_LONG).show();

                    }
                }){

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();

                        String ACCESS_TOKEN = getPref("accessToken",getApplicationContext());
                        params.put("token",ACCESS_TOKEN);
                        params.put("token_type_hint","Token");

                        return params;
                    }
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        String authString = CLIENT_ID + ":";
                        String auth = "Basic "
                                + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
                        headers.put("User-Agent", "Ximi");
                        headers.put("Authorization", auth);
                        return headers;
                    }

                };

                requestQueue.add(stringRequest);

                putPref("accessToken","",getApplicationContext());
                putPref("tokenType","",getApplicationContext());

                break;
        }

        return super.onOptionsItemSelected(item);
    }

}


