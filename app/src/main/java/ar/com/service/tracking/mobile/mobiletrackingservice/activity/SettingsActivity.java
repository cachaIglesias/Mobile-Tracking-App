package ar.com.service.tracking.mobile.mobiletrackingservice.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;
import ar.com.service.tracking.mobile.mobiletrackingservice.activity.state.MapsActivityState;

public class SettingsActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "SettingsActivity";

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.setSharedPref(getSharedPreferences("SettingFile", MODE_PRIVATE));

        inicializarMinTimeSpinner();
        inicializarMinDistSpinner();
        inicializarNgrokURL();
        inicializarIdRepartidor();

        Log.i(TAG, "Inicio de la actividad: " + TAG) ;

    }

    private void inicializarMinTimeSpinner() {

        Spinner spinner = (Spinner) findViewById(R.id.spinner_min_time);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getSharedPref().edit().putString("minTime", (String) adapterView.getSelectedItem()).commit();
                getSharedPref().edit().putInt("minTimeSpinnerPosition", i).commit();
                Log.i(TAG, "Se seleccionó tiempo minimo de actualización de posicion GPS: " + (String) adapterView.getSelectedItem()) ;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        List<String> planetas = new ArrayList<String>();
        planetas.add("3 segundos");
        planetas.add("5 segundos");
        planetas.add("10 segundos");
        planetas.add("20 segundos");

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, planetas);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        int lastSelection = this.getSharedPref().getInt("minTimeSpinnerPosition", 0);
        spinner.setSelection( lastSelection );

    }

    private void inicializarMinDistSpinner() {

        Spinner spinner = (Spinner) findViewById(R.id.spinner_min_dist);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getSharedPref().edit().putString("minDist", (String) adapterView.getSelectedItem()).commit();
                getSharedPref().edit().putInt("minDistSpinnerPosition", i).commit();
                Log.i(TAG, "Se seleccionó la distancia minimo de actualización de posicion GPS: " + (String) adapterView.getSelectedItem()) ;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        List<String> planetas = new ArrayList<String>();
        planetas.add("10 metros");
        planetas.add("25 metros");
        planetas.add("50 metros");
        planetas.add("75 metros");

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, planetas);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        int lastSelection = this.getSharedPref().getInt("minDistSpinnerPosition", 0);
        spinner.setSelection( lastSelection );

    }

    private void inicializarNgrokURL() {
        final EditText editText = (EditText) findViewById(R.id.ngrok_url);

        MapsActivityState mapsActivityState = MapsActivityState.getInstance(null);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                // getSharedPref().edit().putString("ngrokUrl", ((EditText) view).getText().toString() ).commit();
                MapsActivityState.getInstance(null).setUrl(((EditText) view).getText().toString());
                }
            }
        );

        // String ngrokURL = this.getSharedPref().getString("ngrokUrl", "http://10.0.2.2:3000/");
        editText.setText(mapsActivityState.getUrl());
    }

    private void inicializarIdRepartidor() {

        final EditText editText = (EditText) findViewById(R.id.deliveryMan_id);

        MapsActivityState mapsActivityState = MapsActivityState.getInstance(null);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                // getSharedPref().edit().putString("ngrokUrl", ((EditText) view).getText().toString() ).commit();
                MapsActivityState.getInstance(null).setUserId(Integer.parseInt(((EditText) view).getText().toString()));
                }
            }
        );

        // int ngrokURL = this.getSharedPref().getInt("deliveryManID", "http://10.0.2.2:3000/");
        editText.setText(mapsActivityState.getUserId().toString());

    }

    public void terminarActividad(View view) {

//        Intent intent = new Intent();
//        intent.putExtra("resultado","valor");
//        intent.putExtra("resultado1","1");
//        setResult(RESULT_OK, intent);
//        finish();

        onBackPressed();

        Log.i(TAG, "Fin de la actividad: " + TAG) ;

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public void setSharedPref(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }
}
