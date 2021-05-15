package uc.edu.cl.olderapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import uc.edu.cl.olderapp.R;

public class EstadoActivity extends WearableActivity {
    int tipo = 1;
    private TextView mTextView;
    private ImageButton btnAtras;
    private ImageButton btnImagen;
    private DatoSensor datoSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estado);
        mTextView = (TextView) findViewById(R.id.txtDato);
        btnAtras = (ImageButton) findViewById(R.id.btnAtras);
        btnImagen = (ImageButton) findViewById(R.id.imgEstado);
        datoSensor = (DatoSensor) getIntent().getSerializableExtra("DatoSensor");
        btnImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipo = (tipo < 3) ? tipo + 1 : 1;
                procesarVista();
            }
        });
        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewEstado = new Intent(EstadoActivity.this, Interfaz.class);
                startActivity(viewEstado);
            }
        });
        procesarVista();
        setAmbientEnabled();
    }

    public void procesarVista() {
        if (tipo == 1) {
            mTextView.setText(String.valueOf(datoSensor.getHeartRate()));
            if (datoSensor.getHeartRate() < 66)
                btnImagen.setImageResource(R.drawable.excelente);
            else if (datoSensor.getHeartRate() < 74)
                btnImagen.setImageResource(R.drawable.bueno);
            else if (datoSensor.getHeartRate() < 88)
                btnImagen.setImageResource(R.drawable.normal);
            else
                btnImagen.setImageResource(R.drawable.alterado);
        } else if (tipo == 2) {
            mTextView.setText(String.valueOf(datoSensor.getPressure()));
            if (datoSensor.getPressure() <= 90)
                btnImagen.setImageResource(R.drawable.normal90);
            else if (datoSensor.getPressure() <= 105)
                btnImagen.setImageResource(R.drawable.normal105);
            else if (datoSensor.getPressure() <= 120)
                btnImagen.setImageResource(R.drawable.normal120);
            else if (datoSensor.getPressure() <= 130)
                btnImagen.setImageResource(R.drawable.elevada130);
            else if (datoSensor.getPressure() <= 140)
                btnImagen.setImageResource(R.drawable.elevada140);
            else if (datoSensor.getPressure() <= 160)
                btnImagen.setImageResource(R.drawable.alta160);
            else
                btnImagen.setImageResource(R.drawable.alta190);
        } else {
            String texto = "NO ACTIVO BT";
            if (datoSensor.isEstadoBT()) {
                if (datoSensor.getPitch() > -55 && datoSensor.getPitch() < -8 && datoSensor.getRoll() > 44 && datoSensor.getRoll() < 65 && datoSensor.getYaw() > 160 && datoSensor.getYaw() < 272) {
                    texto = "SENTADO";
                    btnImagen.setImageResource(R.drawable.sentado);
                } else if (datoSensor.getPitch() > -60 && datoSensor.getPitch() < -12 && datoSensor.getRoll() > 51 && datoSensor.getRoll() < 70 && datoSensor.getYaw() > 1 && datoSensor.getYaw() < 360) {
                    texto = "PARADO";
                    btnImagen.setImageResource(R.drawable.parado);
                } else if (datoSensor.getPitch() > -57 && datoSensor.getPitch() < 30 && datoSensor.getRoll() > -65 && datoSensor.getRoll() < 74 && datoSensor.getYaw() > 1 && datoSensor.getYaw() < 359) {
                    texto = "CAMINANDO";
                    btnImagen.setImageResource(R.drawable.caminando);
                } else if (datoSensor.getPitch() > -52 && datoSensor.getPitch() < 23 && datoSensor.getRoll() > -56 && datoSensor.getRoll() < 59 && datoSensor.getYaw() > 147 && datoSensor.getYaw() < 201) {
                    texto = "AGACHADO";
                    btnImagen.setImageResource(R.drawable.agachado1);
                } else {
                    texto = "ACOSTADO";
                    btnImagen.setImageResource(R.drawable.acostado);
                }
                mTextView.setText(texto);
            } else {
                mTextView.setText(texto);
                btnImagen.setImageResource(R.drawable.close_button);
            }
        }
    }
}