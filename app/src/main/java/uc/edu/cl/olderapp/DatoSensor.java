package uc.edu.cl.olderapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import bolts.Continuation;
import bolts.Task;

public class DatoSensor implements ServiceConnection, Serializable {
    private transient Context context;
    private int painNumber = 0;
    private int pressure;
    private int heartRate;
    private int light;
    private float yaw;
    private float roll;
    private float pitch;
    private int conteo;
    private String userId;
    private transient BtleService.LocalBinder serviceBinder;
    private transient SensorFusionBosch sensorFusion;
    private transient StorageReference mStorageRef;
    private transient FirebaseAuth mAuth;
    private transient EulerAngles casted;
    private transient File file;
    private transient MetaWearBoard board;
    private String estadoBT = "Buscando ...";
    private transient TextView txtEstadoBt;

    public DatoSensor(Context context, TextView txtEstadoBt) {
        this.context = context;
        this.txtEstadoBt = txtEstadoBt;
        conteo = 0;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance("gs://magister-app-cb15e.appspot.com").getReference();
        context.getApplicationContext().bindService(new Intent(context, BtleService.class), this, Context.BIND_AUTO_CREATE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        userId = prefs.getString("USER_ID", "none");
        SensorManager mSensorManager = ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
        Sensor mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        Sensor mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                pressure = (int) event.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        }, mPressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                heartRate = (int) event.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        }, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                light = (int) event.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        }, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if(!userId.equals("none")) {
            crearArchivo();
        }
    }
    public void crearArchivo(){
        String fecha = new SimpleDateFormat("dd-MM-yyyy").format(new Date())+"_";
        file = new File("/data/data/uc.edu.cl.olderapp/files", fecha + String.valueOf(userId) + ".csv");
        if (!file.exists()) {
            conteo = 0;
            try {
                file.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                writer.append(String.format(Locale.US, "%s,%n", "Date, Pitch, Roll, Yaw, HeartRate, Pressure, Ligth, Pain, CountView").toString());
                writer.close();
            } catch (IOException e) {
            }
        }
    }

    public void retrieveBoard(final String MW_MAC_ADDRESS) {
        final BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);
        board = serviceBinder.getMetaWearBoard(remoteDevice);
        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Route>>() {
            @Override
            public Task<Route> then(Task<Void> task) throws Exception {
                Log.i("euler", "Conectado a " + MW_MAC_ADDRESS);
                estadoBT = "Conectado";
                txtEstadoBt.setText(estadoBT);
                sensorFusion = board.getModule(SensorFusionBosch.class);
                sensorFusion.configure()
                        .mode(SensorFusionBosch.Mode.IMU_PLUS)
                        .accRange(SensorFusionBosch.AccRange.AR_16G)
                        .gyroRange(SensorFusionBosch.GyroRange.GR_2000DPS)
                        .commit();
                sensorFusion.eulerAngles().start();
                sensorFusion.start();
                sensorFusion.eulerAngles().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                casted = data.value(EulerAngles.class);
                                Log.d("Datos", casted.toString());
                                try {
                                    setPitch(casted.pitch());
                                    setRoll(casted.roll());
                                    setYaw(casted.yaw());
                                    BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                                    writer.append(String.format(Locale.US, "%s,%.3f,%.3f,%.3f,%d,%d,%d,%d,%d,%n",
                                            data.formattedTimestamp(),
                                            casted.pitch(), casted.roll(), casted.yaw(), getHeartRate(), getPressure(), getLight(), getPainNumber(), getConteo()).toString());
                                    writer.close();
                                    try {
                                        Thread.sleep(3000);
                                    } catch (InterruptedException e) {
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                return null;
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                if (task.isFaulted())
                    retrieveBoard("EA:51:34:53:9B:1A");
                return null;
            }
        });
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if(!estadoBT.equals("Conectado")){
            Log.i("euler", "Servicio iniciado");
            serviceBinder = (BtleService.LocalBinder) service;
            estadoBT = "Buscando ...";
            txtEstadoBt.setText(estadoBT);
            retrieveBoard("EA:51:34:53:9B:1A");
        }
    }

    public void guardarEnviarDatos() {
        Log.i("Enviar", "Enviando Datos Firebase");
        String fecha = new SimpleDateFormat("dd-MM-yyyy").format(new Date())+"_";
        Uri f = Uri.fromFile(new File("/data/data/uc.edu.cl.olderapp/files",fecha+ String.valueOf(userId) + ".csv"));
        mStorageRef.child(fecha+userId + ".csv").putFile(f);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i("Desconectando", "Enviando Datos Firebase");
        guardarEnviarDatos();
        context.getApplicationContext().unbindService(this);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getPainNumber() {
        return painNumber;
    }

    public void setPainNumber(int painNumber) {
        this.painNumber = painNumber;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getLight() {
        return light;
    }

    public void setLight(int light) {
        this.light = light;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BtleService.LocalBinder getServiceBinder() {
        return serviceBinder;
    }

    public void setServiceBinder(BtleService.LocalBinder serviceBinder) {
        this.serviceBinder = serviceBinder;
    }

    public SensorFusionBosch getSensorFusion() {
        return sensorFusion;
    }

    public void setSensorFusion(SensorFusionBosch sensorFusion) {
        this.sensorFusion = sensorFusion;
    }

    public StorageReference getmStorageRef() {
        return mStorageRef;
    }

    public void setmStorageRef(StorageReference mStorageRef) {
        this.mStorageRef = mStorageRef;
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public void setmAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public EulerAngles getCasted() {
        return casted;
    }

    public void setCasted(EulerAngles casted) {
        this.casted = casted;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public int getConteo() {
        return conteo;
    }

    public void setConteo(int conteo) {
        this.conteo = conteo;
    }

    public String getEstadoBT() {
        return estadoBT;
    }

    public void setEstadoBT(String estadoBT) {
        this.estadoBT = estadoBT;
    }
}