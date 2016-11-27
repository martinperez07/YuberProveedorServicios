package yuber.yuberProveedorServicios.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import yuber.yuberProveedorServicios.R;

public class FragmentDialogYuberRetirarFondos extends DialogFragment {

    private static final String TAG = FragmentDialogYuberRetirarFondos.class.getSimpleName();
    private String Ip = "54.203.12.195";
    private String Puerto = "8080";
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    SharedPreferences sharedpreferences;

    public FragmentDialogYuberRetirarFondos() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    public AlertDialog createLoginDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialogo_retirar_fondo, null);
        builder.setView(v);

        //LOGICA DE RETIRAR FONDO

        TextView texto = (TextView) v.findViewById(R.id.text_dialog_msg);
        texto.setText("¿Deseas confirmar el retiro?");

        Button botonAceptar = (Button) v.findViewById(R.id.boton_aceptar_yuber);
        botonAceptar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        retirarFondo();
                        dismiss();
                    }
                }
        );

        Button botonCancelar = (Button) v.findViewById(R.id.boton_cancelar_yuber);
        botonCancelar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                }
        );

        return builder.create();

    }

    public void retirarFondo(){
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String email = sharedpreferences.getString(EmailKey, "");
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/Cobrar/" + email;
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

