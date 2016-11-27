package yuber.yuberProveedorServicios.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import yuber.yuberProveedorServicios.R;

public class ConfiguracionActivity extends AppCompatActivity {

    private String Ip = "54.203.12.195";
    private String Puerto = "8080";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //BOTON LOGIN
        Button modificar = (Button) findViewById(R.id.btn_actualizar);
        modificar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //llamo a la logica de modificar los datos

                //actualizo
                actualizo();
            }
        });
    }

    public void actualizo(){
        //Obtengo el mail
        Intent homeIntent = new Intent(getApplicationContext(), ConfiguracionActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public boolean validate() {
        boolean valid = true;
/*
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailView.setError("Introduzca un email válido");
            valid = false;
        } else {
            mEmailView.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordView.setError("La contraseña debe de contener entre 4 y 10 caracteres alfanuméricos");
            valid = false;
        } else {
            mPasswordView.setError(null);
        }*/

        return valid;
    }

}
