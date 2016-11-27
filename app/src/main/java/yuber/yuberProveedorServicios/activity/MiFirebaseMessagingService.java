package yuber.yuberProveedorServicios.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;


public class MiFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MESSAGES";
    public static final String EstoyTrabajando = "EstoyTrabajando";
    public static final String EnViaje = "enViaje";
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //ACA VAN LAS CADENAS DE NOTIFICACIONES QUE ME LLEGAN
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String tabajando = sharedpreferences.getString(EstoyTrabajando, "");
        String enViaje = sharedpreferences.getString(EnViaje, "");
        String tituloNotificacion = remoteMessage.getNotification().getTitle();
        if (tabajando.contains("true")) {
            if (enViaje.contains("false")) {
                if (tituloNotificacion.equals("Nueva solicitud"))
                    sendBodyToMapFragment(remoteMessage.getData());
            }
            else {
                if (tituloNotificacion.equals("Solicitud cancelada"))
                    sendBodyToMapFragmentCancelado();
            }
        }
    }

    private void sendBodyToMapFragment(Map<String, String> data) {
        String aux = data.toString();
        Intent intent = new Intent("MpFragment.action.ACEPTAR_RECHAZAR");
        intent.putExtra("DATOS_USUARIOS", aux);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendBodyToMapFragmentCancelado() {
        Intent intent = new Intent("MpFragment.action.VIAJE_CANCELADO");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendBodyToMapFragmentComenzarViaje(Map<String, String> data) {
        Gson gson = new Gson();
        String jsonData = gson.toJson(data);
        Intent intent = new Intent("MpFragment.action.DESTINO_ELEGIDO");
        intent.putExtra("DATOS_DESTINO", jsonData);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
