package uc.edu.cl.olderapp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;

import uc.edu.cl.olderapp.R;


public class LoginActivity extends WearableActivity {
    private String TAG = LoginActivity.class.getSimpleName();
    private FitApi fitApi = new FitApi();
    private String name = "";
    private String rut = "";
    private LoginActivity actual;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actual = this;
        setContentView(R.layout.login);
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.BODY_SENSORS},
                1
        );
        EditText nameField = findViewById(R.id.nameInput);
        EditText rutField = findViewById(R.id.rutInput);
        Button loginButton = findViewById(R.id.loginButton);
        nameField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                name = c.toString();
            }
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
            }
            public void afterTextChanged(Editable c) {
            }
        });
        rutField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                rut = c.toString();
            }
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
            }
            public void afterTextChanged(Editable c) {
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fitApi.createUser(name, rut, getApplicationContext());
                actual.onBackPressed();
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
