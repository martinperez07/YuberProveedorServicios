package yuber.yuberProveedorServicios.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import yuber.yuberProveedorServicios.R;

public class FragmentDialogYuberAceptarRechazar extends DialogFragment {
    private static final String TAG = FragmentDialogYuberAceptarRechazar.class.getSimpleName();
    private JSONObject mProveedor;
    private JSONObject mCliente;
    private String Ip = "54.203.12.195";
    private String Puerto = "8080";

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String ClienteUbicacionOrigenKey = "ubicacionOrigenKey";
    public static final String ClienteNombreKey = "clienteNombreKey";
    public static final String ClienteApellidoKey = "clienteApellidoKey";
    public static final String ClienteTelefonoKey = "clienteTelefonoKey";
    public static final String ClienteInstanciaServicioKey = "clienteInstanciaServicioKey";
    public static final String EnViaje = "enViaje";
    SharedPreferences sharedpreferences;

    public FragmentDialogYuberAceptarRechazar() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    public AlertDialog createLoginDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String proveedorString = getArguments().getString("DatosUsuario");
        try {
            mProveedor = new JSONObject(proveedorString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialogo_yuber_aceptar_rechazar, null);
        builder.setView(v);

        TextView textoNombreProv = (TextView) v.findViewById(R.id.text_dialog_yub_disp_nombre);
        TextView textoAppellidoProv = (TextView) v.findViewById(R.id.text_dialog_yub_disp_apellido);
        TextView textoTelefonoProv = (TextView) v.findViewById(R.id.text_dialog_yub_disp_telefono);
        RatingBar ratingBarPuntajeProv = (RatingBar) v.findViewById(R.id.ratingBarYuberDispo);
        ratingBarPuntajeProv.setMax(5);

        double puntaje = 0;
        String instanciaId = "";
        try {

            String JSONubicacion = mProveedor.getString("ubicacion");
            String JSONcliente = mProveedor.getString("cliente");
            System.out.println("--el json pa:" + JSONcliente);
            mCliente = new JSONObject(JSONcliente);
            String nombre = mCliente.getString("usuarioNombre");
            String apellido = mCliente.getString("usuarioApellido");
            String telefono = mCliente.getString("usuarioTelefono");
            textoNombreProv.setText(nombre);
            textoAppellidoProv.setText(apellido);
            textoTelefonoProv.setText(telefono);
            puntaje = mCliente.getDouble("usuarioPromedioPuntaje");
            instanciaId = mProveedor.getString("instanciaServicioId");

            sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(ClienteUbicacionOrigenKey, JSONubicacion);
            editor.putString(ClienteNombreKey, nombre);
            editor.putString(ClienteApellidoKey, apellido);
            editor.putString(ClienteTelefonoKey, telefono);
            editor.putString(ClienteInstanciaServicioKey, instanciaId);
            editor.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ratingBarPuntajeProv.setRating(((float) puntaje));
        Button botonAceptar = (Button) v.findViewById(R.id.boton_aceptar_yuber);
        Button botonCancelar = (Button) v.findViewById(R.id.boton_cancelar_yuber);
        final String finalInstanciaId = instanciaId;
        botonAceptar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //seteo la variable global
                        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(EnViaje, "true");
                        editor.commit();
                        //envio al server que acepto el viaje
                        //aparece boton

                        //TODO CHEQUEAR QUE ONDA CON LOS INTENT ESTOS! NO SE PUEDE EVIAR VARIOS, creo...
                        Intent intent ;//= new Intent("MpFragment.action.APAGA_FIN");
                       // LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        intent = new Intent("MpFragment.action.PRENDE_INICIAR");
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        //intent = new Intent("MpFragment.action.MpFragment.action.MARCAR_ORIGEN");
                       // LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

                        AceptarServicio(finalInstanciaId);
                        dismiss();
                    }
                }
        );

        botonCancelar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                }
        );

        new Thread(new Runnable() {
            public void run() {
                temporizadorProv temp = new temporizadorProv();
                temp.esperarXsegundos(60);
                dismiss();
            }
        }).start();

        return builder.create();
    }

    public void AceptarServicio(String instanciaServicioId){
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String email = sharedpreferences.getString(EmailKey, "");
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/AceptarServicio/" + instanciaServicioId + "," + email;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
            }
        });
    }

}

