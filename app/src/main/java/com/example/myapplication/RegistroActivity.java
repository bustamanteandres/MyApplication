package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.Toast;

import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistroActivity extends AppCompatActivity{
    private EditText nombre;
    private EditText apellido;
    private EditText dni;
    private EditText email;
    private EditText contraseña;
    private EditText comision;
    private Button registroButton;
    ProgressDialog progressDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        nombre = (EditText) findViewById(R.id.nombreRegistro);
        apellido = (EditText) findViewById(R.id.apellidoRegistro);
        dni = (EditText) findViewById(R.id.dniRegistro);
        email = (EditText) findViewById(R.id.emailRegistro);
        contraseña = (EditText) findViewById(R.id.contraseñaRegistro);
        comision = (EditText) findViewById(R.id.comisionRegistro);
        registroButton = (Button) findViewById(R.id.registroButton);
        registroButton.setOnClickListener(registrarUsuario);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private View.OnClickListener registrarUsuario = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mostrarMensajeCargando();
            UserRequest request = new UserRequest();
            request.setAmbiente(getString(R.string.prod_env));
            request.setNombre(nombre.getText().toString());
            request.setApellido(apellido.getText().toString());
            request.setDni(Long.parseLong(dni.getText().toString()));
            request.setEmail(email.getText().toString());
            request.setContraseña(contraseña.getText().toString());
            request.setComision(Long.parseLong(comision.getText().toString()));

            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(getString(R.string.server_uri))
                    .build();

            Converter<ResponseBody, UserResponse> converter = retrofit.responseBodyConverter(UserResponse.class, new Annotation[0]);
            ServerService serverService = retrofit.create(ServerService.class);
            Call<UserResponse> call = serverService.register(request);
            if(internetAccess()) {
                call.enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        if (response.isSuccessful()) {
                            llamarVentanaSensores(response.body().getToken(), response.body().getTokenRefresh());
                        } else {
                            progressDialog.dismiss();
                            try {
                                UserResponse errors = converter.convert(response.errorBody());
                                Toast.makeText(getApplicationContext(), errors.getMsg(), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        progressDialog.dismiss();
                    }
                });
            }else{
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Compruebe su conexion a internet y vuelva a intentarlo", Toast.LENGTH_LONG).show();
            }
        }
    };

    //Metodo que comprueba que exista una conexion a Internet
    public Boolean internetAccess(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void llamarVentanaSensores(String token, String tokenRefresh){
        Intent intent = new Intent(RegistroActivity.this, SensoresActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("tokenRefresh", tokenRefresh);
        intent.putExtra("email", email.getText().toString());
        intent.putExtra("origen", "registro");
        startActivity(intent);
    }


    public void mostrarMensajeCargando(){
        progressDialog.setIcon(R.mipmap.ic_launcher);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
        progressDialog.dismiss();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        progressDialog.dismiss();
    }
}
