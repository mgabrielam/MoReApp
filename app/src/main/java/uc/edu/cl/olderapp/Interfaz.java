package uc.edu.cl.olderapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import uc.edu.cl.olderapp.R;

public class Interfaz extends WearableActivity {
    DatoSensor datoSensor;
    private String TAG = Interfaz.class.getSimpleName();
    private Button dolor;
    private TextView txtEstadoBt;
    private Button estado;
    public static String channelId = "MoreApp";
    public static String channelName = "MoreApp";
    final long[] VIBRATE_PATTERN = {0, 1000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.createNotificationChannel(this.getBaseContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermedio);
        txtEstadoBt = (TextView) findViewById(R.id.txtEstadoBT);
        dolor = findViewById(R.id.dolor);
        estado = findViewById(R.id.btnEstado);
        datoSensor = new DatoSensor(this.getBaseContext(), txtEstadoBt);
        if (datoSensor.getUserId().equals("none")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }else {
            estado.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Interfaz.this, EstadoActivity.class);
                    intent.putExtra("DatoSensor", datoSensor);
                    startActivity(intent);
                }
            });
            dolor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Interfaz.this, MainActivity.class);
                    intent.putExtra("DatoSensor", datoSensor);
                    startActivity(intent);
                }
            });
        }
    }
    @SuppressLint("NewApi")
    public void createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000, 1000});
            channel.enableVibration(true);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(context)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000})
                    .setContentText("Registro Dolor")
                    .build();
        }
    }
    @Override
    protected  void onResume() {
        super.onResume();
        if (!datoSensor.getUserId().equals("none")) {
            datoSensor.crearArchivo();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        datoSensor.guardarEnviarDatos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        datoSensor.guardarEnviarDatos();
    }
}
