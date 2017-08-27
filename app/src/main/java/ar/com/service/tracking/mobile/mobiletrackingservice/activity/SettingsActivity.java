package ar.com.service.tracking.mobile.mobiletrackingservice.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

public class SettingsActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener {

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPref = getSharedPreferences("SettingFile", MODE_PRIVATE);

        inicializarMinTimeSpinner();
        inicializarMinDistSpinner();

    }

    private void inicializarMinTimeSpinner() {

        Spinner spinner = (Spinner) findViewById(R.id.spinner_min_time);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getSharedPref().edit().putString("minTime", (String) adapterView.getSelectedItem()).commit();
                getSharedPref().edit().putInt("minTimeSpinnerPosition", i).commit();
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

    public void terminarActividad(View view) {

//        Intent intent = new Intent();
//        intent.putExtra("resultado","valor");
//        intent.putExtra("resultado1","1");
//        setResult(RESULT_OK, intent);
//        finish();

        onBackPressed();

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
