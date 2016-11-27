package yuber.yuberProveedorServicios.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TimePicker;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import yuber.yuberProveedorServicios.R;

public class FragmentCronometro extends Fragment {

    private TimePicker timePicker;
    private String Ip = "54.203.12.195";
    private String Puerto = "8080";
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EstadoDelViaje = "estadoDelViaje";
    public static final String ClienteInstanciaServicioKey = "clienteInstanciaServicioKey";


    private Chronometer tiempo;
    private Button pausarReanudar;
    private SharedPreferences sharedpreferences;
    long timeWhenStopped = 0;
    private String instanciaId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_cronometro, container, false);

        //visual de reloj
        timePicker = (TimePicker) v.findViewById(R.id.reloj);
        timePicker.setIs24HourView(true);
        timePicker.setClickable(false);
        timePicker.setEnabled(false);

        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        instanciaId = sharedpreferences.getString(ClienteInstanciaServicioKey, "");

        pausarReanudar = (Button) v.findViewById(R.id.button_pausar_reanudar);
        pausarReanudar.setOnClickListener(EventoPausarReanudar());

        Button finalizar = (Button) v.findViewById(R.id.button_finalizar_servicio);
        finalizar.setOnClickListener(EventoFinalizar());
        //cronometro
        timeWhenStopped = 0;
        tiempo = (Chronometer) v.findViewById(R.id.chronometer);
        tiempo.start();

        return v;
    }

    public View.OnClickListener EventoFinalizar(){
        View.OnClickListener clickListtener = new View.OnClickListener() {
            public void onClick(View v) {
                //Se llama a comenzar viaje
                SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(EstadoDelViaje, "sinCal");
                editor.commit();

                timeWhenStopped = tiempo.getBase() - SystemClock.elapsedRealtime();
                tiempo.stop();
                finalizarServicio(timeWhenStopped / 1000);
                MainActivity m = (MainActivity)getActivity();
                m.displayView(0);
            }
        };
        return clickListtener;
    }

    public View.OnClickListener EventoPausarReanudar(){
        View.OnClickListener clickListtener = new View.OnClickListener() {
            public void onClick(View v) {
                //Se llama a comenzar viaje
                String textoBtn = pausarReanudar.getText().toString();
                if (textoBtn.equals("Pausar")){
                    pausarReanudar.setText("Reanudar");
                    timeWhenStopped = tiempo.getBase() - SystemClock.elapsedRealtime();
                    tiempo.stop();
                }else{
                    pausarReanudar.setText("Pausar");
                    tiempo.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    tiempo.start();
                }
            }
        };
        return clickListtener;
    }

    public void finalizarServicio(float t){
        String tiempo = Integer.toString((int) t);
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/FinServicio/" + instanciaId + "," + tiempo;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
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

}
