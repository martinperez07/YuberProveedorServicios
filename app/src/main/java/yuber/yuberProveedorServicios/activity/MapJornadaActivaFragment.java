package yuber.yuberProveedorServicios.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import yuber.yuberProveedorServicios.R;


public class MapJornadaActivaFragment extends Fragment {

    private Switch switchJornada;
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String EstoyTrabajando = "EstoyTrabajando";
    public static final String EnViaje = "enViaje";
    SharedPreferences sharedpreferences;

    private String Ip = "54.203.12.195";
    private String Puerto = "8080";

    public MapJornadaActivaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map_jornada, container, false);
        switchJornada = (Switch) rootView.findViewById(R.id.JornadaActiva);

        /* CONSULTO AL SERVIDOR SI ESTA TRABAJANDO */

        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String email = sharedpreferences.getString(EmailKey, "");
        estoyTrabajando(email);
        switchJornada.setChecked(false);

        /* CUANDO TOCAN LA PERILLA CAMBIO DE ESTADO EN EL SERVER */
        switchJornada.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on) {
                SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                String email = sharedpreferences.getString(EmailKey, "");
                String enViaje = sharedpreferences.getString(EnViaje, "");
                if (on) {
                    //VOY A COMENZAR A TRABAJAR
                    sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(EstoyTrabajando, "true");
                    editor.commit();
                    comenzarATrabajar(email);
                } else {
                    //DEJA DE TRABAJAR enViaje
                    if (enViaje.contains("false")) {
                        dejarDeTrabajar(email);
                    }else{
                        Toast.makeText(getActivity().getApplicationContext(), "No puede dejar de trabajar mientras estas brindando un servicio", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        return rootView;
    }

    public void estoyTrabajando(String email){
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/EstoyTrabajando/" + email;
        AsyncHttpClient client = new AsyncHttpClient();
        RequestHandle rc = client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                guardarDato(response);
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    public void guardarDato(String dato){
        if (dato.contains("true")){
            switchJornada.setChecked(true);
            SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(EstoyTrabajando, dato);
            editor.commit();
        }else{
            switchJornada.setChecked(false);
            SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(EstoyTrabajando, dato);
            editor.commit();
        }
    }

    public void comenzarATrabajar(String email){
        //voy a el servidor a cambiar de estado
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/IniciarJornada/" + email + ",0";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                if (response.contains("ERROR") ){
                    Toast.makeText(getActivity().getApplicationContext(), "No se pudo cambiar de estado, vuelva a intentar.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void dejarDeTrabajar(String email){
        //voy a el servidor a cambiar de estado
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/FinalizarJornada/" + email + ",0";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                if (response.contains("ERROR") ){
                    Toast.makeText(getActivity().getApplicationContext(), "No se pudo cambiar de estado, vuelva a intentar.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



}