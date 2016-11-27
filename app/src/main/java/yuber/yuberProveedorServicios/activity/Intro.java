package yuber.yuberProveedorServicios.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.json.JSONObject;

import yuber.yuberProveedorServicios.R;

public class Intro extends AppCompatActivity {

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String TokenKey = "tokenKey";
    public static final String EnViaje = "enViaje";
    SharedPreferences sharedpreferences;

    private String Ip = "54.203.12.195";
    private String Puerto = "8080";
    private static final String TAG = "INTRO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
            }
        }
        Log.d(TAG, "Antes de conseguir el token con Firebase");
        String token = FirebaseInstanceId.getInstance().getToken();
        //Guardo el token en session
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(TokenKey, token);
        editor.putString(EnViaje, "false");
        editor.commit();
        //Combruebo si ya tengo session.
        Log.d(TAG, "ANTES DE TENGOSESSION(token)");
        TengoSession(token);
    }

    public void TengoSession(String token){
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/ValidarSesion/" + token;
        Log.d(TAG, "URL get: " + url);
        JSONObject obj = new JSONObject();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestHandle Rq = client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                if (response.contains("ERROR")){
                    cambiarALogin();
                }else{
                    SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(EmailKey, response);
                    editor.commit();
                    //Veo si esta trabajando o no
                    cambiarAMain();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void cambiarAMain(){
        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void cambiarALogin(){
        Intent homeIntent = new Intent(getApplicationContext(), LoginActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

}
