package uc.edu.cl.olderapp;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import uc.edu.cl.olderapp.R;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends WearableActivity {
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
    DatoSensor datoSensor;
    private String TAG = MainActivity.class.getSimpleName();
    private FitApi fitApi = new FitApi();
    private Timer timer;
    MainActivity actual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actual = this;
        datoSensor = (DatoSensor) getIntent().getSerializableExtra("DatoSensor");
        Runnable runnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                if (!mStopHandler) {
                    if (t == 11) {
                        t = 0;
                    }
                    if (timer1 != null) {
                        timer1.cancel();
                    }
                    datoSensor.setPainNumber(t);
                    NumberDictation();
                    Vibrator vb = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vb.vibrate(VibrationEffect.createOneShot(150, 10));
                    pain.setText(String.format(Locale.getDefault(), "%d", datoSensor.getPainNumber()));
                    view.setBackgroundColor(Color.parseColor(colorlist.get(datoSensor.getPainNumber())));
                    mainview.setBackgroundColor(Color.parseColor(colorlist.get(datoSensor.getPainNumber())));
                    t++;
                    mHandler.postDelayed(this, 1000);
                }
            }
        };

        setContentView(R.layout.activity_main);
        fitApi.saveToken(
                datoSensor.getUserId(),
                FirebaseInstanceId.getInstance().getToken(),
                getApplicationContext()
        );
        final Button sendButton = findViewById(R.id.send_button);
        sendButton.setText(String.format(Locale.getDefault(), "%d", datoSensor.getPainNumber()));
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timer != null) {
                    timer.cancel();
                }
                datoSensor.setPainNumber(datoSensor.getPainNumber() + 1);
                colores();
                NumberDictation();
                if (datoSensor.getPainNumber() > 10) {
                    datoSensor.setPainNumber(0);
                }
                sendButton.setText(String.format(Locale.getDefault(), "%d", datoSensor.getPainNumber()));
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fitApi.sendPainRecord(
                                        datoSensor.getPainNumber(),
                                        datoSensor.getHeartRate(),
                                        datoSensor.getPressure(),
                                        datoSensor.getLight(),
                                        datoSensor.getUserId(),
                                        getApplicationContext()
                                );
                                datoSensor.crearRegistro();
                                sendButton.setText(String.format(Locale.getDefault(), "%d", datoSensor.getPainNumber()));
                                actual.onBackPressed();
                            }
                        });
                    }
                }, 5000);
            }
        });
        setAmbientEnabled();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void NumberDictation() {
        if (datoSensor.getPainNumber() != 0) {
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
            mPlayer = new MediaPlayer();
            try {
                if (datoSensor.getPainNumber() != 0) {
                    mPlayer.setDataSource(getApplication(),
                            Uri.parse("android.resource://uc.edu.cl.olderapp/"
                                    + soundlist.get(datoSensor.getPainNumber() - 1)));
                }
            } catch (IOException err) {
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
    public void updateUI() {
        Vibrator vb = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        pain.setText(String.format(Locale.getDefault(), "%d", datoSensor.getPainNumber()));
        view.setBackgroundColor(Color.parseColor(colorlist.get(datoSensor.getPainNumber())));
        mainview.setBackgroundColor(Color.parseColor(colorlist.get(datoSensor.getPainNumber())));
        vb.vibrate(VibrationEffect.createOneShot(150, 10));
    }

    public void colores() {
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
        updateUI();
    }

}
