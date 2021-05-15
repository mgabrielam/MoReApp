package uc.edu.cl.olderapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import uc.edu.cl.olderapp.R;

public class Interfaz extends WearableActivity {
    DatoSensor datoSensor;
    private String TAG = Interfaz.class.getSimpleName();
    private Button dolor;
    private TextView mTextView;
    private TextView texto;
    private Button estado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermedio);
        mTextView = (TextView) findViewById(R.id.txtDato);
        dolor = findViewById(R.id.dolor);
        estado = findViewById(R.id.btnEstado);
        datoSensor = new DatoSensor(this.getBaseContext());
        if (datoSensor.getUserId().equals("none")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
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
