package ar.com.service.tracking.mobile.mobiletrackingservice.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

    }

    public void terminarActividad(View view) {
        onBackPressed();
    }

}
