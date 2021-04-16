package com.example.juanenrique.wearableapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class FitApi {
    private String TAG = FitApi.class.getSimpleName();
    private String url = "https://magister-server-app.herokuapp.com/api/v1";

    public void sendPainRecord(
            int painRecord,
            int hearRate,
            int pressure,
            int light,
            String rut,
            final Context context
    ) {

        RequestQueue queue = Volley.newRequestQueue(context);
        String painRecordURl = url + "/pain_records";

        JSONObject params = new JSONObject();
        JSONObject painRecordObject = new JSONObject();
        try {
            painRecordObject.put("pain", painRecord);
            painRecordObject.put("pressure", pressure);
            painRecordObject.put("heart_rate", hearRate);
            painRecordObject.put("light", light);
            painRecordObject.put("rut", rut);
            params.put("pain_record", painRecordObject);
        } catch (JSONException e) {
            Log.e(TAG, "unexpected JSON exception", e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
            (Request.Method.POST, painRecordURl, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {}
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse == null) {
                        Toast.makeText(
                                context,
                                "Falló la conexión a internet",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            });
        queue.add(jsonObjectRequest);
    }

    public void createUser(final String name, final String rut, final Context context) {
        if ("".equals(name) || "".equals(rut)) {
            Toast.makeText(context, "Ingrese rut y nombre", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        String painRecordURl = url + "/users";

        JSONObject params = new JSONObject();
        JSONObject painRecordObject = new JSONObject();
        try {
            painRecordObject.put("name", name);
            painRecordObject.put("rut", rut);
            params.put("user", painRecordObject);
        } catch (JSONException e) {}

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, painRecordURl, params, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("USER_ID", rut);
                        editor.apply();

                        // TODO: make transition on Activity, waiting this response
                        Intent intent = new Intent(context, Interfaz.class);
                        context.startActivity(intent);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse == null) {
                            Toast.makeText(
                                    context,
                                    "Falló la conexión a internet",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else if (error.networkResponse.statusCode == 400) {
                            Toast.makeText(
                                    context,
                                    "Usuario ya existe",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(
                                    context,
                                    "Hubo un error inesperado",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
        queue.add(jsonObjectRequest);
    }

    public void saveToken(String rut, String token, Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String saveTokenUrl = url + "/save_token";

        JSONObject params = new JSONObject();
        try {
            params.put("rut", rut);
            params.put("token", token);
        } catch (JSONException e) {}

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, saveTokenUrl, params, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "SEND TOKEN, response is: "+ response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {}
                });
        queue.add(jsonObjectRequest);
    }
}
