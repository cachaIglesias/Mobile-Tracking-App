package ar.com.service.tracking.mobile.mobiletrackingservice;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Random;

public class GPSservice extends Service {

    private LocationManager locationManager;
    private PolylineOptions polylineOptions;
    private GoogleMap map;

    // Binder given to clients
    private final IBinder mBinder = new GPSbinder(this);

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setParameters(LocationManager ActiviyMapLocationManager, PolylineOptions ActiviyMapPolylineOptions, GoogleMap ActiviyMapMap) {

        locationManager = ActiviyMapLocationManager;
        polylineOptions = ActiviyMapPolylineOptions;
        map = ActiviyMapMap;

    }

    public void toggleNetworkUpdates() {

        boolean ACCESS_FINE_OK = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (ACCESS_FINE_OK) {

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListenerNetwork);
            Toast.makeText(this, "Network provider started running", Toast.LENGTH_LONG).show();

        }

    }


    private final LocationListener locationListenerNetwork = new LocationListener() {

        private double longitudeNetwork, latitudeNetwork;

        public void onLocationChanged(Location location) {
            longitudeNetwork = location.getLongitude();
            latitudeNetwork = location.getLatitude();

            LatLng centrar = new LatLng(latitudeNetwork, longitudeNetwork);
            map.addMarker(new MarkerOptions().position(centrar).title("Yo"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(centrar, 18));

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {

                    // se agrega al polyline la nuevo posicion
                    polylineOptions.add(new LatLng(latitudeNetwork, longitudeNetwork));
                    map.addPolyline(polylineOptions);

                    Toast.makeText(GPSservice.this, "Network Provider update", Toast.LENGTH_SHORT).show();
//                }
//            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

}
