package yuber.yuberProveedorServicios.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yuber.yuberProveedorServicios.R;


public class SignUpActivity extends AppCompatActivity {

    private String Ip = "54.203.12.195";
    private String Puerto = "8080";
    private EditText nameText;
    private EditText addressText;
    private EditText emailText;
    private EditText mobileText;
    private EditText passwordText;
    private EditText reEnterPasswordText;
    private EditText LastNameText;
    private EditText ciudadText;
    private EditText MarcaVehiculo;
    private EditText ModeloVehiculo;
    private Spinner combo;

    private Map<String, String> servicios;
    private String name;
    private String LastName;
    private String address;
    private String email;
    private String mobile;
    private String password;
    private String ciudad;
    private String servicioId;
    private String servicioKey;
    private String ClavePrivada;
    private String ClavePublica;
    private EditText clavePrivada;
    private EditText clavePublica;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String TokenKey = "tokenKey";
    public static final String EmailKey = "emailKey";
    public static final String ErrorRegistrar = "errorRegistrar";
    public static final String ErrorAsociar = "errorAsociar";
    public static final String ServiciosTransporte = "serviciosTransporte";
    private SharedPreferences sharedpreferences;

    //======STRIPE ==================
    private String cardNumber = "4242424242424242";
    private String cvc = "123";
    private int monthSpinner = 1;
    private int yearSpinner = 2018;
    private String currencySpinner = "usd";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button signupButton = (Button) findViewById(R.id.btn_signup);
        TextView loginLink = (TextView) findViewById(R.id.link_login);
        nameText = (EditText) findViewById(R.id.input_name);
        addressText = (EditText) findViewById(R.id.input_address);
        emailText = (EditText) findViewById(R.id.input_email);
        mobileText = (EditText) findViewById(R.id.input_mobile);
        passwordText = (EditText) findViewById(R.id.input_password);
        reEnterPasswordText = (EditText) findViewById(R.id.input_reEnterPassword);
        LastNameText = (EditText) findViewById(R.id.input_LastName);
        ciudadText = (EditText) findViewById(R.id.input_ciudad);
        //Stripe
        clavePrivada = (EditText) findViewById(R.id.input_clave_privada);
        clavePublica = (EditText) findViewById(R.id.input_clave_publica);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        cargarCombo();
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
    }

    public void cargarCombo(){
        //Completo el comboBox
        combo = (Spinner) findViewById(R.id.combo);
        List<String> categories = new ArrayList<String>();
        //Consulto los servicios
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Servicios/ObtenerServicios/On-Site";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(ServiciosTransporte, response);
                editor.commit();
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
            }
        });
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String Response = sharedpreferences.getString(ServiciosTransporte, "");
        //agrego al map y a la lista
        JSONObject rec; JSONObject datos; String ServicioNombre; String ServicioId;
        servicios = new HashMap<String, String>();
        try {
            JSONArray arr_strJson = new JSONArray(Response);
            for (int i = 0; i < arr_strJson.length(); ++i) {
                //rec todos los datos de una instancia servicio
                rec = arr_strJson.getJSONObject(i);
                //datos tiene los datos basicos
                datos = new JSONObject(rec.toString());
                ServicioNombre = (String) datos.getString("servicioNombre");
                ServicioId = (String) datos.getString("servicioId");
                //creo mapa
                servicios.put(ServicioNombre,ServicioId);
                //agrego a la lista
                categories.add(ServicioNombre);
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            combo.setAdapter(dataAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }
        Button signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creando su cuenta...");
        progressDialog.show();
        //Cargo los datos
        name = nameText.getText().toString();
        LastName = LastNameText.getText().toString();
        address = addressText.getText().toString();
        email = emailText.getText().toString();
        mobile = mobileText.getText().toString();
        ciudad = ciudadText.getText().toString();
        password = passwordText.getText().toString();
        servicioKey = (String) combo.getSelectedItem();
        servicioId = servicios.get(servicioKey);
        ClavePrivada = clavePrivada.getText().toString();
        ClavePublica = clavePublica.getText().toString();

        boolean ok = true;
        registrar();
    }

    public void registrar(){
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/RegistrarProveedor/";
        JSONObject obj = new JSONObject();
        JSONObject objVert = new JSONObject();
        try {
            obj.put("usuarioDireccion", address);
            obj.put("usuarioContraseña", password);
            obj.put("usuarioTelefono", mobile);
            obj.put("usuarioApellido", LastName);
            obj.put("usuarioNombre", name);
            obj.put("usuarioPromedioPuntaje", 0.0);
            obj.put("usuarioCorreo", email);
            obj.put("usuarioCiudad", ciudad);
            obj.put("estado", "OK");
            obj.put("gananciaTotal", 0);
            obj.put("porCobrar", 0);

            objVert.put("tipoVertical", "On-Site");
            objVert.put("proveedor", obj);

            AsyncHttpClient client = new AsyncHttpClient();
            ByteArrayEntity entity = null;
            entity = new ByteArrayEntity(objVert.toString().getBytes("UTF-8"));
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    asociarServicio(email, servicioId);
                }
                @Override
                public void onFailure(int statusCode, Throwable error, String content) {
                    Toast.makeText(getApplicationContext(), "No se pudo registrar el usuario", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "No se pudo registrar el usuario", Toast.LENGTH_LONG).show();
        }
    }

    public void asociarServicio(String email, String servicioId){
        System.out.println("---servicioid " + servicioId + "email: " + email);
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/AsociarServicio/" + email + "," + servicioId;
        System.out.println("---url " + url);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                saveCreditCard();
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                System.out.println("No se pudo asociar al servicio");
            }
        });
    }

    public void logear(){
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String token = sharedpreferences.getString(TokenKey, "");
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(EmailKey, email);
        editor.commit();
        /*****************  Consulta a BD si existe el user ********************/
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/Login";
        JSONObject obj = new JSONObject();
        try {
            obj.put("correo", email);
            obj.put("password", password);
            obj.put("deviceId", token);
            System.out.println("----" + email + " - " + password + " - " +token);
            AsyncHttpClient client = new AsyncHttpClient();
            ByteArrayEntity entity = null;
            entity = new ByteArrayEntity(obj.toString().getBytes("UTF-8"));
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    if (response.contains("true")) {
                        cambiarAHome();
                    } else {
                        Toast.makeText(getApplicationContext(), "Vuelva a loguearse", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(int statusCode, Throwable error, String content) {
                    Toast.makeText(getApplicationContext(), "Intente loguearse nuevamente", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Intente loguearse nuevamente", Toast.LENGTH_LONG).show();
        }
    }

    public void cambiarAHome(){
        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "La creación de cuenta ha fallado", Toast.LENGTH_LONG).show();
        Button signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;
        String name = nameText.getText().toString();
        String LastName = LastNameText.getText().toString();
        String ciudad = ciudadText.getText().toString();
        String address = addressText.getText().toString();
        String email = emailText.getText().toString();
        String mobile = mobileText.getText().toString();
        String password = passwordText.getText().toString();
        String reEnterPassword = reEnterPasswordText.getText().toString();
        String ClavePrivada = clavePrivada.getText().toString();
        String ClavePublica = clavePublica.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("Nombre con al menos 3 caracteres");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (LastName.isEmpty() || LastName.length() < 3) {
            LastNameText.setError("Apellido con al menos 3 caracteres");
            valid = false;
        } else {
            LastNameText.setError(null);
        }

        if (ciudad.isEmpty() || ciudad.length() < 3) {
            ciudadText.setError("Ciudad con al menos 4 caracteres");
            valid = false;
        } else {
            ciudadText.setError(null);
        }

        if (address.isEmpty()) {
            addressText.setError("Ingrese una direccion válida");
            valid = false;
        } else {
            addressText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Ingrese un email válido");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=9) {
            mobileText.setError("Ingrese un número de celular válido");
            valid = false;
        } else {
            mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("La contraseña debe ser de entre 4 y 10 caracteres alfanuméricos");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            reEnterPasswordText.setError("Las contraseñas no coinciden");
            valid = false;
        } else {
            reEnterPasswordText.setError(null);
        }

        if (ClavePrivada.isEmpty() || ClavePrivada.length() < 10) {
            clavePrivada.setError("Clave privada incorrecta");
            valid = false;
        } else {
            clavePrivada.setError(null);
        }

        if (ClavePublica.isEmpty() || ClavePublica.length() < 10) {
            clavePublica.setError("Clave publica incorrecta");
            valid = false;
        } else {
            clavePublica.setError(null);
        }

        return valid;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void saveCreditCard() {

        System.out.println("--->cardnumber: " + this.cardNumber);

        Card card = new Card(
                this.cardNumber,
                this.monthSpinner,
                this.yearSpinner,
                this.cvc);
        card.setCurrency(this.currencySpinner);

        System.out.println("--->month: " + this.monthSpinner);
           //startProgress();
        new Stripe().createToken(
            card, clavePublica.getText().toString(),
            new TokenCallback() {
                public void onSuccess(Token token) {
                    String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/AsociarMecanismoDePago/" + email + "," + token.getId().toString() + "," + clavePrivada.getText().toString();

                    System.out.println("--->url: " + url);
                    System.out.println("--->clave publica: " + clavePublica.getText().toString());


                    AsyncHttpClient client = new AsyncHttpClient();
                    ByteArrayEntity entity = null;
                    client.get(null, url, new AsyncHttpResponseHandler(){
                        @Override
                        public void onSuccess(String response) {
                            logear();
                        }
                        @Override
                        public void onFailure(int statusCode, Throwable error, String content){
                            if(statusCode == 404){
                                Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                            }else if(statusCode == 500){
                                Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    //finishProgress();
                }
                public void onError(Exception error) {
                    handleError(error.getLocalizedMessage());
                    System.out.println("--->error: " + error.getMessage());
                    //finishProgress();
                }
            });
    }

    private void handleError(String error) {
    }



}
