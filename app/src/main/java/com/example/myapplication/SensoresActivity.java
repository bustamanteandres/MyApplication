package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SensoresActivity extends AppCompatActivity implements SensorEventListener{
    DatabaseReference databaseReference;
    UserData usr;
    private String emailUsuario;
    private String token;
    private String tokenRefresh;
    private String origen;

    private SensorManager sensorManager;
    private TextView acelerometroX;
    private TextView acelerometroY;
    private TextView acelerometroZ;
    private TextView giroscopoX;
    private TextView giroscopoY;
    private TextView giroscopoZ;
    private TextView gravedadX;
    private TextView gravedadY;
    private TextView gravedadZ;
    private Button buttonAcelerometro;
    private Button buttonGiroscopo;
    private Button buttonGravedad;
    private TextView userEmail;
    private TextView previousLoginDateText;
    private TextView createdDateText;
    DecimalFormat dosdecimales = new DecimalFormat("###.###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Bundle extraInfo = intent.getExtras();
        emailUsuario = extraInfo.getString("email");
        token = extraInfo.getString("token");
        tokenRefresh = extraInfo.getString("tokenRefresh");
        origen = extraInfo.getString("origen");
        databaseReference = FirebaseDatabase.getInstance().getReference();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensores);
        setTitle("Sensores");
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelerometroX = (TextView) findViewById(R.id.acelerometroX);
        acelerometroY = (TextView) findViewById(R.id.acelerometroY);
        acelerometroZ = (TextView) findViewById(R.id.acelerometroZ);
        giroscopoX = (TextView) findViewById(R.id.giroscopoX);
        giroscopoY = (TextView) findViewById(R.id.giroscopoY);
        giroscopoZ = (TextView) findViewById(R.id.giroscopoZ);
        gravedadX = (TextView) findViewById(R.id.gravedadX);
        gravedadY = (TextView) findViewById(R.id.gravedadY);
        gravedadZ = (TextView) findViewById(R.id.gravedadZ);
        userEmail = (TextView) findViewById(R.id.userEmail);

        buttonAcelerometro = (Button) findViewById(R.id.eventoAcelerometro);
        buttonAcelerometro.setOnClickListener(listener);
        buttonGiroscopo = (Button) findViewById(R.id.eventoGiroscopo);
        buttonGiroscopo.setOnClickListener(listener);
        buttonGravedad = (Button) findViewById(R.id.eventoGravedad);
        buttonGravedad.setOnClickListener(listener);

        previousLoginDateText = (TextView) findViewById(R.id.previousLoginDate);
        createdDateText = (TextView) findViewById(R.id.createdDate);
        usr = new UserData();
        cargarUserDataAsync();
        eventoFuturoRefrescarToken();
        dispararEvento(getString(R.string.prod_env), "User Login", "Login exitoso!");
        mostrarBateriaRestante();
    }

    @Override
    protected void onResume() {
        super.onResume();
        iniciarSensores();
    }

    //Metodo usado para refrescar token cada 25 minutos.
    public void eventoFuturoRefrescarToken(){
        new Handler().postDelayed(new Runnable(){
            public void run(){
                refrescarToken();
                eventoFuturoRefrescarToken();
            }
        }, 1500000); //1500000 millisegundos = 25 minutos.
    }

    //Metodo asyncronico que carga los datos del usuario: LoginDate, CreatedDate y UserName
    public void cargarUserDataAsync(){
        UserDataAsyncTask thread = new UserDataAsyncTask();
        thread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class UserDataAsyncTask extends AsyncTask {
        @Override
        protected String doInBackground(Object[] params) {
            if(origen.equals("registro")) {
                registrarCreatedDateUsuario();
            }
            obtenerDatosUsuario();
            userEmail.setText("Usuario: " + emailUsuario);
            return null;
        }
    }

    //Metodo que se encarga de disparar los eventos de los sensores
    private View.OnClickListener listener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.eventoAcelerometro:
                    dispararEvento(getString(R.string.prod_env), "Sensor Acelerometro", "Evento acelerometro disparado");
                    break;
                case R.id.eventoGiroscopo:
                    dispararEvento(getString(R.string.prod_env), "Sensor Giroscopo", "Evento giroscopo disparado");
                    break;
                case R.id.eventoGravedad:
                    dispararEvento(getString(R.string.prod_env), "Sensor Gravedad", "Evento gravedad disparado");
                    break;
            }
        }
    };

    //Metodo que refresca el token
    public void refrescarToken(){
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getString(R.string.server_uri))
                .build();

        ServerService serverService = retrofit.create(ServerService.class);
        String authorization = "bearer " + tokenRefresh;
        Call<UserResponse> call = serverService.refresh(authorization);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if(response.isSuccessful()){
                    token = response.body().getToken();
                    response.body().getTokenRefresh();
                }else{
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.i("Failure", "Failure: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        pararSensores();
    }

    @Override
    protected void onStop(){
        super.onStop();
        pararSensores();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        pararSensores();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    //Metodo que escucha el cambio de los sensores
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Para asegurarnos ante los accesos simultaneos sincronizamos esto
        synchronized (this) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    acelerometroX.setText(dosdecimales.format(event.values[0]) + " m/seg2 \n");
                    acelerometroY.setText(dosdecimales.format(event.values[1]) + " m/seg2 \n");
                    acelerometroZ.setText(dosdecimales.format(event.values[2]) + " m/seg2 \n");
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    giroscopoX.setText(dosdecimales.format(event.values[0]) + " deg/s \n");
                    giroscopoY.setText(dosdecimales.format(event.values[1]) + " deg/s \n");
                    giroscopoZ.setText(dosdecimales.format(event.values[2]) + " deg/s \n");
                    break;
                case Sensor.TYPE_GRAVITY :
                    gravedadX.setText(event.values[0] + "\n");
                    gravedadY.setText(event.values[1] + "\n");
                    gravedadZ.setText(event.values[2] + "\n");
                    break;
            }
        }
    }

    public void iniciarSensores(){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void pararSensores(){
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
    }

    public void dispararEvento(String env, String eventType, String description){
        EventRequest request = new EventRequest();
        request.setEnv(env);
        request.setEventType(eventType);
        request.setDescription(description);

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getString(R.string.server_uri))
                .build();

        ServerService serverService = retrofit.create(ServerService.class);
        String authorization = "bearer " + token;
        Call<EventResponse> call = serverService.event(authorization, request);
        call.enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if(response.isSuccessful()){
                    Toast.makeText(getApplicationContext(), response.body().getEvent().getDescription(), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.i("Failure", "Failure: " + t.getMessage());
            }
        });
    }

    public void obtenerDatosUsuario(){
        ValueEventListener eventListener = new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                usr = snapshot.getValue(UserData.class);
                String conexionAnterior = usr.getPreviousLoginDate() != null ? usr.getPreviousLoginDate() : "No se registra ningun inicio de sesion anterior";
                String fechaCreacion = usr.getCreatedDate() != null ? usr.getCreatedDate() : "El usuario fue registrado en una version distinta";
                previousLoginDateText.setText("Ultima conexion anterior: " + conexionAnterior);
                createdDateText.setText("Fecha de creacion: " + fechaCreacion);
                registrarPreviuosLoginDateUsuario();
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        };
        databaseReference.child("Usuario").child(emailUsuario.replace(".", "-")).addListenerForSingleValueEvent(eventListener);
    }

    //Muestra en pantalla el nivel actual de bateria del dispositivo
    public void mostrarBateriaRestante(){
        Intent batteryStatus = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        String batteryPercentaje = "Bateria restante: " + String.valueOf(batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)) + "%";
        Toast.makeText(this, String.valueOf(batteryPercentaje), Toast.LENGTH_LONG).show();
    }

    //Carga en la base de datos de Firebase el createdDate del Usuario
    public void registrarCreatedDateUsuario(){
        databaseReference.child("Usuario").child(emailUsuario.replace(".", "-")).child("createdDate").setValue(LocalDateTime.now().toString().replace("T", " "));
    }

    //Carga en la base de datos de Firebase el previousLoginDate del Usuario
    public void registrarPreviuosLoginDateUsuario(){
        databaseReference.child("Usuario").child(emailUsuario.replace(".", "-")).child("previousLoginDate").setValue(LocalDateTime.now().toString().replace("T", " "));
    }
}
