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

import yuber.yuberProveedorServicios.R;

public class FragmentDialogYuberCancelaronViaje extends DialogFragment {

    private static final String TAG = FragmentDialogYuberCancelaronViaje.class.getSimpleName();

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String ClienteNombreKey = "clienteNombreKey";
    public static final String ClienteApellidoKey = "clienteApellidoKey";
    public static final String ClienteTelefonoKey = "clienteTelefonoKey";
    public static final String EnViaje = "enViaje";
    SharedPreferences sharedpreferences;

    public FragmentDialogYuberCancelaronViaje() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    public AlertDialog createLoginDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialogo_cancelar_viaje, null);
        builder.setView(v);

        TextView texto = (TextView) v.findViewById(R.id.text_dialog_caneclar_viaje);

        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String Nombre = sharedpreferences.getString(ClienteNombreKey, "");
        String Apellido = sharedpreferences.getString(ClienteApellidoKey, "");
        String Telefono = sharedpreferences.getString(ClienteTelefonoKey, "");

        texto.setText(Nombre + " " + Apellido + " cancelo el viaje");

        //Seteo la variable global
        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(EnViaje, "false");
        editor.commit();
        //saco el boton
        Button FinViaje = (Button) getActivity().findViewById(R.id.FinViaje);
        FinViaje.setVisibility(View.GONE);



        Button botonAceptar = (Button) v.findViewById(R.id.boton_aceptar_yuber);
        botonAceptar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                }
        );

        return builder.create();

    }

}

