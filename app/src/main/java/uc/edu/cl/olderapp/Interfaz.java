package uc.edu.cl.olderapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

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
    public GoogleSignInClient cliente;
    public GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermedio);
        this.createNotificationChannel(this.getBaseContext());
        txtEstadoBt = (TextView) findViewById(R.id.txtEstadoBT);
        dolor = findViewById(R.id.dolor);
        estado = findViewById(R.id.btnEstado);
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("899270739919-bje7uu9spfbghre7luofp95a2s3h3egm.apps.googleusercontent.com")
                .requestEmail()
                .build();
        cliente = GoogleSignIn.getClient(this, gso);
        datoSensor = new DatoSensor(this.getBaseContext(), txtEstadoBt);
        Intent signInIntent = cliente.getSignInIntent();
        startActivityForResult(signInIntent, 1);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            Fitness.getHistoryClient(this, account)
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                        @Override
                        public void onSuccess(DataSet result) {
                            if(result.getDataPoints().get(0).getValue(Field.FIELD_STEPS) != null) {
                                datoSensor.setPasos(result.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("PASOS error", e.getMessage());
                }
            });
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }
}
