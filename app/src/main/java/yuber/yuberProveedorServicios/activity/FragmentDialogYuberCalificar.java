package yuber.yuberProveedorServicios.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import yuber.yuberProveedorServicios.R;

public class FragmentDialogYuberCalificar extends DialogFragment {

    private String Ip = "54.203.12.195";
    private String Puerto = "8080";
    private String idInstance;
    private String punta;
    MainActivity mainActivity;

    private static final String TAG = FragmentDialogYuberCancelaronViaje.class.getSimpleName();
    private RatingBar ratingBarPuntaje;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String ClienteInstanciaServicioKey = "clienteInstanciaServicioKey";
    public static final String ClienteNombreKey = "clienteNombreKey";
    public static final String ClienteApellidoKey = "clienteApellidoKey";
    public static final String ClienteUbicacionOrigenKey = "ubicacionOrigenKey";
    public static final String ClienteTelefonoKey = "clienteTelefonoKey";
    public static final String ClienteUbicacionDestinoKey = "ubicacionDestinoKey";
    public static final String EnViaje = "enViaje";

    SharedPreferences sharedpreferences;

    public FragmentDialogYuberCalificar() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    public AlertDialog createLoginDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialogo_calificar, null);
        builder.setView(v);

        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String Nombre = sharedpreferences.getString(ClienteNombreKey, "");
        String Apellido = sharedpreferences.getString(ClienteApellidoKey, "");

        TextView texto = (TextView) v.findViewById(R.id.text_titulo_calificacion);
        texto.setText("Califica a " + Nombre + " " + Apellido);

        ratingBarPuntaje = (RatingBar) v.findViewById(R.id.ratingBarDialogHistorial);
        double puntaje = 0;
        ratingBarPuntaje.setRating(((float) puntaje));

        Button botonConfirmar = (Button) v.findViewById(R.id.boton_confirmar);
        botonConfirmar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Limpio las variable en sesion relacionadaas al viaje
                        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.remove(ClienteNombreKey);
                        editor.remove(ClienteApellidoKey);
                        editor.remove(ClienteUbicacionOrigenKey);
                        editor.remove(ClienteTelefonoKey);
                        editor.remove(ClienteUbicacionDestinoKey);
                        editor.putString(EnViaje, "false");
                        editor.commit();
                        //Envio el puntaje al servidor
                        enviarPuntaje();
                        dismiss();
                    }
                }
        );
        return builder.create();
    }

    public void agregoALista(){
        //String instanciaID = sharedpreferences.getString(ClienteInstanciaServicioKey, "");
        System.out.print("--->Entre a  agregar a lista" + idInstance);
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Servicios/ObtenerInstanciaServicio/" + idInstance;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject ubicacionDestino = new JSONObject(json.getString("ubicacionDestino"));
                    JSONObject ubicacion = new JSONObject(json.getString("ubicacion"));
                    String costo = json.getString("instanciaServicioCosto");
                    String puntaje = punta;

                    String fecha  = json.getString("instanciaServicioFechaInicio");
                    Long longFecha = Long.parseLong(fecha);
                    final Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(longFecha);
                    final SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    fecha = f.format(cal.getTime());

                    String dist = json.getString("instanciaServicioDistancia");

                    Double latO = ubicacion.getDouble("latitud");
                    Double lonO = ubicacion.getDouble("longitud");
                    Double latD = ubicacion.getDouble("latitud");
                    Double lonD = ubicacion.getDouble("longitud");

                    String dirO = getAddressFromLatLng(latO, lonO);
                    String dirD = getAddressFromLatLng(latD, lonD);

                    Historial hst = new Historial("Sin comentario", puntaje, costo, dist, dirO, dirD, fecha);

                    mainActivity.agregarEnHistorial(hst);
                } catch (JSONException e) {
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
            }
        });
    }

    private String getAddressFromLatLng(double lat, double lon) {
        Geocoder geocoder = new Geocoder( mainActivity );
        String address = "";
        try {
            address =geocoder
                    .getFromLocation( lat, lon, 1 )
                    .get( 0 ).getAddressLine( 0 ) ;
        } catch (IOException e ) {
            // this is the line of code that sends a real error message to the  log
            Log.e("ERROR", "ERROR IN CODE: " + e.toString());
            // this is the line that prints out the location in the code where the error occurred.
            e.printStackTrace();
            return "ERROR_IN_CODE";
        }
        return address;
    }

    public void enviarPuntaje(){
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String instanciaID = sharedpreferences.getString(ClienteInstanciaServicioKey, "");
        idInstance = instanciaID;
        float number = ratingBarPuntaje.getRating();
        int punt = new Float(number).intValue();
        String puntaje = String.valueOf(number);
        punta = puntaje;

        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/PuntuarCliente/" + punt + ",Sin comentario," + instanciaID;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                agregoALista();
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
            }
        });
    }

}


