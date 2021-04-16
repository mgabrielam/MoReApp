package com.example.juanenrique.wearableapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import java.util.Locale;

public class MainActivity extends WearableActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private FitApi fitApi = new FitApi();
    private int painNumber = 0;
    private int pressure;
    private int heartRate;
    private int light;
    private String userId;
    Timer timer;
    RelativeLayout view;
    LinearLayout mainview;
    Button pain;
    int t = 0;
    MediaPlayer mPlayer = new MediaPlayer();
    Timer timer1 = new Timer();
    List<String> colorlist = new ArrayList<>();
    List<Integer> soundlist = new ArrayList<>();
    boolean mStopHandler = false;
    Handler mHandler = new Handler();
    Runnable runnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            if (!mStopHandler) {
                if (t==11) {
                    t = 0;
                }
                if (timer1 != null) {
                    timer1.cancel();
                }
                painNumber = t;
                NumberDictation();
                Vibrator vb = (Vibrator)   getSystemService(VIBRATOR_SERVICE);
                vb.vibrate(VibrationEffect.createOneShot(150, 10));
                pain.setText(String.format(Locale.getDefault(), "%d", painNumber ));
                view.setBackgroundColor(Color.parseColor(colorlist.get(painNumber)));
                mainview.setBackgroundColor(Color.parseColor(colorlist.get(painNumber)));
                t++;
                mHandler.postDelayed(this, 1000);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userId = prefs.getString("USER_ID", "none");

        if (userId.equals("none")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fitApi.saveToken(
                userId,
                FirebaseInstanceId.getInstance().getToken(),
                getApplicationContext()
        );

        // *****************************************************************************************
        // **************************** Register button listeners **********************************
        // *****************************************************************************************

        final Button sendButton = findViewById(R.id.send_button);
        sendButton.setText(String.format(Locale.getDefault(), "%d", painNumber));
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timer != null) {
                    timer.cancel();
                }
                painNumber += 1;
                colores();
                NumberDictation();
                if (painNumber > 10) {
                    painNumber = 0;
                }
                sendButton.setText(String.format(Locale.getDefault(), "%d", painNumber));
                timer = new Timer();

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fitApi.sendPainRecord(
                                    painNumber,
                                    heartRate,
                                    pressure,
                                    light,
                                    userId,
                                    getApplicationContext()
                                );
                                painNumber = 0;
                                sendButton.setText(String.format(Locale.getDefault(), "%d", painNumber));
                                Intent inten = new Intent(MainActivity.this, Interfaz.class);
                                startActivity(inten);
                            }
                        });
                    }
                }, 5000);
            }
        });

        // *****************************************************************************************
        // **************************** Listen sensor data *****************************************
        // *****************************************************************************************

        SensorManager mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));

        Sensor mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        Sensor mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

         mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                pressure = (int) event.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
         }, mPressureSensor, SensorManager.SENSOR_DELAY_NORMAL);


        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                heartRate = (int) event.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        }, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                light = (int) event.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        }, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        setAmbientEnabled();
    }
    public void NumberDictation() {
        if (painNumber != 0) {
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
            mPlayer = new MediaPlayer();
            try {
                if (painNumber != 0) {
                    mPlayer.setDataSource(getApplication(),
                            Uri.parse("android.resource://com.example.juanenrique.wearableapp/"
                                    + soundlist.get(painNumber - 1)));
                }
            } catch (IOException err)  {
                err.printStackTrace();
            }
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayer.start();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mPlayer.stop();
                            mPlayer.release();
                            mPlayer = null;
                        }
                    });
                }
            });
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void UpdateUI() {
        Vibrator vb = (Vibrator)   getSystemService(VIBRATOR_SERVICE);
        pain.setText(String.format(Locale.getDefault(), "%d", painNumber));
        view.setBackgroundColor(Color.parseColor(colorlist.get(painNumber)));
        mainview.setBackgroundColor(Color.parseColor(colorlist.get(painNumber)));
        vb.vibrate(VibrationEffect.createOneShot(150, 10));
    }
    public  void colores()
    {
        colorlist.add("#31F113");
        colorlist.add("#44DC14");
        colorlist.add("#58C715");
        colorlist.add("#6CB316");
        colorlist.add("#809E17");
        colorlist.add("#948A18");
        colorlist.add("#A77519");
        colorlist.add("#BB601A");
        colorlist.add("#CF4C1B");
        colorlist.add("#E3371C");
        colorlist.add("#F7231D");

        soundlist.add(R.raw.sound1);
        soundlist.add(R.raw.sound2);
        soundlist.add(R.raw.sound3);
        soundlist.add(R.raw.sound4);
        soundlist.add(R.raw.sound5);
        soundlist.add(R.raw.sound6);
        soundlist.add(R.raw.sound7);
        soundlist.add(R.raw.sound8);
        soundlist.add(R.raw.sound9);
        soundlist.add(R.raw.sound10);
        soundlist.add(R.raw.sound0);

        view = findViewById(R.id.view);
        pain = findViewById(R.id.send_button);
        mainview = findViewById(R.id.mainview);
        view.setBackgroundColor(Color.parseColor("#31F113"));
        mainview.setBackgroundColor(Color.parseColor("#31F113"));
        UpdateUI();

    }
}
