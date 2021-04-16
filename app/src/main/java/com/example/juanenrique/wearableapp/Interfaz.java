package com.example.juanenrique.wearableapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.EulerAngles;
import com.mbientlab.metawear.module.SensorFusionBosch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import bolts.Continuation;
import bolts.Task;


public class Interfaz extends WearableActivity  implements ServiceConnection {
    private String TAG = Interfaz.class.getSimpleName();
    Button dolor;
    private TextView mTextView;
    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private SensorFusionBosch sensorFusion;
    private TextView texto;
    private StorageReference mStorageRef;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    int bt = 0; //Estado de bt
    int limite = 0;
    private static Subscriber DATA_HANDLER = new Subscriber() {
        @Override
        public void apply(Data data, Object... env) {
            try {
                FileOutputStream fos = (FileOutputStream) env[0];
                EulerAngles casted = data.value(EulerAngles.class);
                fos.write(String.format(Locale.US, "%s,%.3f,%.3f,%.3f%n",
                        data.formattedTimestamp(),
                        casted.pitch(), casted.roll(), casted.yaw()).getBytes());

            } catch (IOException ex) {
                Log.e("euler", "Error writing to file", ex);
            }
        }
    };
    private FileOutputStream fos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        super.onCreate(savedInstanceState);
        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);
        setContentView(R.layout.activity_intermedio);
        mTextView = (TextView) findViewById(R.id.text);
        dolor = findViewById(R.id.dolor);
        dolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Cambio de interfaz al realizar una accion con el boton Regitrar Dolor
                 */
                try {
                   ControlSensor();
                    Intent sdolor = new Intent(Interfaz.this, MainActivity.class);
                    startActivity(sdolor);
                } catch (Exception ex) {
                    ControlSensor();
                    Intent sdolor = new Intent(Interfaz.this, MainActivity.class);
                    startActivity(sdolor);
                    System.out.println("se proboco un error en el proceso");
                }
            }

        });
    }


    public void ControlSensor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (bt == 0) {
                        retrieveBoard("C6:3C:BC:A9:6C:E9");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        sensorFusion.eulerAngles().addRouteAsync(new RouteBuilder() {
                            @Override
                            public void configure(RouteComponent source) {
                                source.stream(DATA_HANDLER);
                            }
                        }).continueWith(new Continuation<Route, Void>() {
                            @Override
                            public Void then(final Task<Route> task) throws Exception {
                                int n = 1;
                                String a ="Reloj 1";
                                int j = 0;
                                while (true) {
                                    File f = new File("/data/data/com.example.juanenrique.wearableapp/files", String.valueOf(n) + ".csv");
                                    if (f.exists()) {
                                        if (j != 0) {
                                            Log.i("euler3", "Ya existe elemento " + n);
                                            Log.i("euler3", "Se enviara a firebase elemento " + n);
                                            //Intento con firebase
                                            Uri file = Uri.fromFile(new File("/data/data/com.example.juanenrique.wearableapp/files", String.valueOf(n) +".csv"));
                                            mStorageRef.child(n+a+ ".csv").putFile(file);
                                            n++;
                                        } else {
                                            if (f.exists()) {
                                                n++;
                                            } else {
                                                j++;
                                            }
                                        }
                                    } else {
                                        if (n == 1) {
                                            System.out.println("******************************************************************************************");
                                            Log.i("euler3", "No existe aun elemento" + n);
                                            texto = findViewById(R.id.Estado);
                                            texto.setText("FOS " + n);
                                            fos = openFileOutput(n +a+".csv", MODE_PRIVATE);
                                            Log.i("euler3", "Inicio FOS " + n);
                                            task.getResult().setEnvironment(0, fos);
                                            sensorFusion.eulerAngles().start();
                                            sensorFusion.start();
                                            int act = 0;
                                            while (act != 10) {
                                                Thread.sleep(1000);
                                                Log.i("EULER_4", "Actividad funcionando");
                                                act++;
                                                System.out.println(act);
                                            }
                                            sensorFusion.stop();
                                            sensorFusion.eulerAngles().stop();
                                            System.out.println("se detiene eel proceso  else de datos");
                                            fos.close();
                                            Log.i("EULER_3", "TerminoFOS de " + n);
                                            System.out.println(j);
                                            j++;
                                        } else {
                                            Log.i("euler3", "No existe aun elemento" + n);
                                            texto = findViewById(R.id.Estado);
                                            texto.setText("FOS " + n);
                                            fos = openFileOutput(n + ".csv", MODE_PRIVATE);
                                            Log.i("euler3", "InicioFOS " + n);
                                            task.getResult().setEnvironment(0, fos);
                                            int act = 0;
                                            sensorFusion.eulerAngles().start();
                                            sensorFusion.start();

                                            while (act != 10
                                            ) {
                                                Thread.sleep(1000);
                                                Log.i("euler4", "Actividad funcionando");
                                                act++;
                                            }
                                            sensorFusion.stop();
                                            sensorFusion.eulerAngles().stop();
                                            System.out.println("se detiene eel proceso  else de datos");
                                            fos.close();
                                            Log.i("euler3", "TerminoFOS de " + n);
                                            j++;
                                        }
                                    }


                                }


                            }


                        });
                    }


                }

            }

        }).start();

    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        Log.i("euler", "Servicio iniciado");
        retrieveBoard("C6:3C:BC:A9:6C:E9");

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }


    public void retrieveBoard(final String MW_MAC_ADDRESS) {
        final BluetoothManager btManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice =
                btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);
        board = serviceBinder.getMetaWearBoard(remoteDevice);
        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Route>>() {
            @Override
            public Task<Route> then(Task<Void> task) throws Exception {
                Log.i("euler", "Conectado a " + MW_MAC_ADDRESS);
                sensorFusion = board.getModule(SensorFusionBosch.class);
                sensorFusion.configure()
                        .mode(SensorFusionBosch.Mode.IMU_PLUS)
                        .accRange(SensorFusionBosch.AccRange.AR_16G)
                        .gyroRange(SensorFusionBosch.GyroRange.GR_2000DPS)
                        .commit();
                return sensorFusion.eulerAngles().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                Log.i("euler", data.value(EulerAngles.class).toString());
                            }
                        });
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                if (task.isFaulted()) {
                    texto = findViewById(R.id.Estado);
                    texto.setText("Conectando");
                    Log.w("euler", "Error en configuración", task.getError());
                    retrieveBoard("C6:3C:BC:A9:6C:E9");
                    bt = 0;
                } else {
                    Log.i("euler", "configurado");
                    texto = findViewById(R.id.Estado);
                    texto.setText("Conectado a CC6:3C:BC:A9:6C:E9");
                    bt = 1;
                }
                return null;
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
