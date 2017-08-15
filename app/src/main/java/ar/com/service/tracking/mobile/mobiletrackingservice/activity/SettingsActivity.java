package ar.com.service.tracking.mobile.mobiletrackingservice.activity;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;

public class SettingsActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Spinner spinner = (Spinner) findViewById(R.id.spinner_min_time);

        spinner.setOnItemSelectedListener(this);

        List<String> planetas = new ArrayList<String>();
        planetas.add("Planeta 1");
        planetas.add("Planeta 2");
        planetas.add("Planeta 3");
        planetas.add("Planeta 4");

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, planetas);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


    }

    public void terminarActividad(View view) {

        Intent intent = new Intent();
        intent.putExtra("resultado","valor");
        intent.putExtra("resultado1","1");
        setResult(RESULT_OK, intent);
        finish();

//        onBackPressed();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
