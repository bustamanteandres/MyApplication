package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.annotation.Annotation;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private Button registroButton;
    private Button loginButton;
    private EditText email;
    private EditText contraseña;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Bienvenido");
        email = (EditText) findViewById(R.id.emailLogin);
        contraseña = (EditText) findViewById(R.id.contraseñaLogin);
        loginButton = (Button) findViewById(R.id.loginButton);
        registroButton = (Button) findViewById(R.id.registroButton);
        registroButton.setOnClickListener(listener);
        loginButton.setOnClickListener(listener);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private View.OnClickListener listener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.registroButton:
                    abrirVentanaRegistro();
                    break;
                case R.id.loginButton:
                    iniciarSesion();
                    break;
            }
        }
    };

    //Metodo que comprueba que exista una conexion a Internet
    public Boolean internetAccess(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void abrirVentanaRegistro(){
        Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
        startActivity(intent);
    }

    public void mostrarMensajeCargando(){
        progressDialog.setIcon(R.mipmap.ic_launcher);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();
    }

    private void iniciarSesion(){
        mostrarMensajeCargando();
        UserRequest request = new UserRequest();
        request.setAmbiente(getString(R.string.prod_env));
        request.setEmail(email.getText().toString());
        request.setContraseña(contraseña.getText().toString());

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getString(R.string.server_uri))
                .build();

        Converter<ResponseBody, UserResponse> converter = retrofit.responseBodyConverter(UserResponse.class, new Annotation[0]);
        ServerService serverService = retrofit.create(ServerService.class);
        Call<UserResponse> call = serverService.login(request);
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
                    Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }else{
            progressDialog.dismiss();
            Toast.makeText(this, "Compruebe su conexion a internet y vuelva a intentarlo", Toast.LENGTH_LONG).show();
        }
    }

    private void llamarVentanaSensores(String token, String tokenRefresh){
        Intent intent = new Intent(MainActivity.this, SensoresActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("tokenRefresh", tokenRefresh);
        intent.putExtra("email", email.getText().toString());
        intent.putExtra("origen", "login");
        startActivity(intent);
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
