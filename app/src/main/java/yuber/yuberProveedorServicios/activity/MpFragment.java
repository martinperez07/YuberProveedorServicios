package yuber.yuberProveedorServicios.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import yuber.yuberProveedorServicios.R;

public class MpFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener {

    MapView mMapView;
    private GoogleMap googleMap;
    private static int REQUEST_LOCATION;

    LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LatLng mOrigenLatLng;
    private Marker mOrigenMarker;
    private String mDistanciaViaje = "1";
    private ArrayList<LatLng> markerPoints;

    private static final String TAG = "MAPA";

    //NOTIFICACIONES
    public static final String ACTION_INTENT_ACEPTAR_RECHAZAR = "MpFragment.action.ACEPTAR_RECHAZAR";
    public static final String ACTION_INTENT_CANCELARON_VIAJE = "MpFragment.action.VIAJE_CANCELADO";
    public static final String ACTION_INTENT_DESTINO_ELEGIDO = "MpFragment.action.DESTINO_ELEGIDO";

    //BOTONES
    public static final String ACTION_PRENDE_INICIAR = "MpFragment.action.PRENDE_INICIAR";
    public static final String ACTION_APAGA_INICIAR = "MpFragment.action.APAGA_INICIAR";
    public static final String ACTION_MARCAR_ORIGEN = "MpFragment.action.MARCAR_ORIGEN";

    ProgressDialog prgDialog;
    Fragment topFragment = null;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String ClienteInstanciaServicioKey = "clienteInstanciaServicioKey";
    public static final String EstadoDelViaje = "estadoDelViaje";
    public static final String EmailKey = "emailKey";
    public static final String EnViaje = "enViaje";
    private SharedPreferences sharedpreferences;
    private String Ip = "54.203.12.195";
    private String Puerto = "8080";

    private Button IniciarServicio;
    private Button CancelarServicio;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflat and return the layout
        View v = inflater.inflate(R.layout.fragment_mp, container,
                false);

        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        prgDialog = new ProgressDialog(getActivity());
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);

        topFragment = new MapJornadaActivaFragment();
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.FlashBarLayout, topFragment);
        fragmentTransaction.commit();

        IntentFilter filter = new IntentFilter(ACTION_INTENT_ACEPTAR_RECHAZAR);
        filter.addAction(ACTION_INTENT_CANCELARON_VIAJE);
        filter.addAction(ACTION_INTENT_DESTINO_ELEGIDO);
        filter.addAction(ACTION_PRENDE_INICIAR);
        filter.addAction(ACTION_APAGA_INICIAR);
        filter.addAction(ACTION_MARCAR_ORIGEN);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(ActivityDataReceiver, filter);

        IniciarServicio = (Button) v.findViewById(R.id.IniciarViaje);
        IniciarServicio.setOnClickListener(crearBotonIniciar());

        CancelarServicio = (Button) v.findViewById(R.id.CancelarViaje);
        CancelarServicio.setOnClickListener(crearBotonCancelar());

        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String enviaje = sharedpreferences.getString(EnViaje, "");
        if (enviaje.contains("true")){
            String estadoDelViaje = sharedpreferences.getString(EstadoDelViaje, "");
            if(estadoDelViaje.contains("inicio")){
                mostrarBotones();
            }
        }

        markerPoints = new ArrayList<LatLng>();
        return v;
    }

    public void ocultarBotones(){
        IniciarServicio.setVisibility(View.GONE);
        CancelarServicio.setVisibility(View.GONE);
    }

    public void mostrarBotones(){
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(EstadoDelViaje, "inicio");
        editor.commit();
        IniciarServicio.setVisibility(View.VISIBLE);
        CancelarServicio.setVisibility(View.VISIBLE);
    }

    private View.OnClickListener crearBotonIniciar(){
        View.OnClickListener clickListtener = new View.OnClickListener() {
            public void onClick(View v) {
                //Cambio de botones
                ocultarBotones();
                borrarRutaYMarcadores();
                comenzarServicio();
                //Se llama a comenzar viaje
                MainActivity m = (MainActivity)getActivity();
                m.displayView(3);
            }
        };
        return clickListtener;
    }

    private View.OnClickListener crearBotonCancelar(){
        View.OnClickListener clickListtener = new View.OnClickListener() {
            public void onClick(View v) {
                //Cambio de botones
                ocultarBotones();
            }
        };
        return clickListtener;
    }

    private void borrarRutaYMarcadores() {
        // Removes all the points from Google Map
        googleMap.clear();
        // Removes all the points in the ArrayList
        markerPoints.clear();
    }

    private void comenzarServicio() {
        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String instanciaServicioId = sharedpreferences.getString(ClienteInstanciaServicioKey, "");
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/IniciarServicio/" + instanciaServicioId ;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                System.out.println("---->" + response);
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                System.out.println("Fail");
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMapParam) {
        googleMap = googleMapParam;
        LatLng myLocatLatLng;
        LatLng mdeoLatLng = new LatLng(-34, -56);
        Location myLocation = null;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        if (myLocation == null){
            myLocatLatLng = mdeoLatLng;
        } else{
            myLocatLatLng = new LatLng( myLocation.getLatitude(), myLocation.getLongitude());
        }
        initListeners();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initListeners() {
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnMapClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            Location myLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        initCamera(mCurrentLocation);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //handle play services disconnecting if location is being constantly used
    }

    private void initCamera(Location location) {
        LatLng myActualLatLng;
        //si no esta el GPS prendido va a la ubicacion (-34.9, -56.16)
        if(location!= null)
            myActualLatLng = new LatLng( location.getLatitude(),location.getLongitude() );
        else
            myActualLatLng = new LatLng(-34.9, -56.16);

        CameraPosition position = CameraPosition.builder()
                .target(myActualLatLng)
                .zoom(16f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled( true );
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Create a default location if the Google API Client fails. Placing location at Googleplex
        mCurrentLocation = new Location( "" );
        mCurrentLocation.setLatitude( -34.9 );
        mCurrentLocation.setLongitude( -56.16 );
        initCamera(mCurrentLocation);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText( getActivity(), "Clicked on marker", Toast.LENGTH_SHORT ).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    protected BroadcastReceiver ActivityDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ACTION_INTENT_ACEPTAR_RECHAZAR.equals(intent.getAction()) ){
                String jsonUsuario = intent.getStringExtra("DATOS_USUARIOS");
                double latitud = -34.9133764;
                double longitud = -56.1690546;
                try {
                    JSONObject datosUsuario = new JSONObject(jsonUsuario);
                    JSONObject datosOrigenUsuario = new JSONObject(datosUsuario.getString("ubicacion"));
                    latitud = datosOrigenUsuario.getDouble("latitud");
                    longitud = datosOrigenUsuario.getDouble("longitud");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mOrigenLatLng = new LatLng(latitud,longitud);
                mostrarDialAceptarRechazar(jsonUsuario);
            }else if(ACTION_INTENT_CANCELARON_VIAJE.equals(intent.getAction()) ){
                //muestro el dialogo
                mostrarDialCancelaronViaje();
            }else if(ACTION_PRENDE_INICIAR.equals(intent.getAction())) {
                mostrarBotones();
                ponerMarcadorOrigen();
            }else if(ACTION_APAGA_INICIAR.equals(intent.getAction())) {
                ocultarBotones();
            }else if(ACTION_MARCAR_ORIGEN.equals(intent.getAction())) {// TODO ELIMINAR, SE PASO A ACTION_APAGA_FIN
                ponerMarcadorOrigen();
            }
        }
    };

    private void ponerMarcadorDestino(String jsonDestino) {
        Log.d(TAG, "Adentro de ponerMarcadorDestino" + jsonDestino);
        double latitud = -34.9133764;
        double longitud = -56.1690546;
        try {
            JSONObject dataUbicacion = new JSONObject(jsonDestino);
            latitud = Double.parseDouble(dataUbicacion.getString("latitud"));
            longitud = Double.parseDouble(dataUbicacion.getString("longitud"));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        LatLng destinoLatLng = new LatLng(latitud,longitud);

        // Adding new item to the ArrayList
        markerPoints.add(destinoLatLng);

        markerPoints.add(destinoLatLng);
        MarkerOptions options = new MarkerOptions().position(destinoLatLng);
        options.icon(BitmapDescriptorFactory.defaultMarker());
        Marker mDestinationMarker = googleMap.addMarker(options);
        mDestinationMarker.setTitle("Destino cliente");



        // Checks, whether start and end locations are captured
        if(markerPoints.size() >= 2){
            LatLng origin = markerPoints.get(0);
            LatLng dest = markerPoints.get(1);

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }

    }

    private void eliminarMarcadorOrigen(){
        if (mOrigenMarker != null)
            mOrigenMarker.remove();
    }

    private void ponerMarcadorOrigen() {
        eliminarMarcadorOrigen();
        MarkerOptions options = new MarkerOptions().position(mOrigenLatLng);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mOrigenMarker = googleMap.addMarker(options);
        mOrigenMarker.setTitle("Ubicacion cliente");
        // Adding new item to the ArrayList
        markerPoints.add(mOrigenLatLng);
    }

    private void mostrarDialAceptarRechazar(String JUser){
        Bundle args = new Bundle();
        args.putString("DatosUsuario", JUser);
        FragmentDialogYuberAceptarRechazar newFragmentDialog = new FragmentDialogYuberAceptarRechazar();
        newFragmentDialog.setArguments(args);
        newFragmentDialog.setCancelable(false);
        newFragmentDialog.show(getActivity().getSupportFragmentManager(), "TAG");
    }

    private void mostrarDialCancelaronViaje(){
        ocultarBotones();
        Bundle args = new Bundle();
        FragmentDialogYuberCancelaronViaje newFragmentDialog = new FragmentDialogYuberCancelaronViaje();
        newFragmentDialog.setArguments(args);
        newFragmentDialog.show(getActivity().getSupportFragmentManager(), "TAG");
    }

    private void mostrarDialCalificacion(){
        Bundle args = new Bundle();
        FragmentDialogYuberCalificar newFragmentDialog = new FragmentDialogYuberCalificar();
        newFragmentDialog.setArguments(args);
        newFragmentDialog.setCancelable(false);
        newFragmentDialog.show(getActivity().getSupportFragmentManager(), "TAG");
    }

    public void actualizarCoordenadas(String email) {
        if (mCurrentLocation != null){
            String enViaje = sharedpreferences.getString(EnViaje, "");
            String latitud = String.valueOf(mCurrentLocation.getLatitude());
            String longitud = String.valueOf(mCurrentLocation.getLongitude());
            String instanciaID = "-1";
            if (enViaje.contains("true")) {
                instanciaID = sharedpreferences.getString(ClienteInstanciaServicioKey, "");
                enViaje = "True";
            }else{
                enViaje = "False";
            }
            //OBTENGO MIS COORDENADAS Y LAS PASO A STRING
            //REGISTRO EN LA BD
            String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/ActualizarCoordenadas/" + email + "," + latitud + "," + longitud + "," + enViaje + "," + instanciaID;
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(null, url, new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(String response) {
                    System.out.println("Actualice las coordenadas!");
                }
                @Override
                public void onFailure(int statusCode, Throwable error, String content){
                }
            });
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mCurrentLocation = location;
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("MAAAAAAL", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";



            if(result.size()<1){
                Toast.makeText(getActivity().getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }


            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){	// Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);

            }

            String[] splited = distance.split("\\s+");
            mDistanciaViaje = splited[0];

            String[] splitDist = distance.split(" ");
            String cantkm = splitDist[0];
            int distancia = (int) (Float.parseFloat(cantkm) * 1000);

            mDistanciaViaje = Integer.toString(distancia);

            // Drawing polyline in the Google Map for the i-th route
            googleMap.addPolyline(lineOptions);
        }
    }




}