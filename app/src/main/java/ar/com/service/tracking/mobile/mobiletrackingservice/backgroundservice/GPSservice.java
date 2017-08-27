package ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

public class GPSservice extends Service {

    private LocationManager locationManager;
    private PolylineOptions polylineOptions;
    private GoogleMap map;

    private final IBinder mBinder = new GPSbinder(GPSservice.this);

    private SharedPreferences sharedPref;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setParameters(LocationManager activiyMapLocationManager, PolylineOptions activiyMapPolylineOptions, GoogleMap activiyMapMap) {

        locationManager = activiyMapLocationManager;
        polylineOptions = activiyMapPolylineOptions;
        map = activiyMapMap;

        sharedPref = getSharedPreferences("SettingFile", MODE_PRIVATE);

    }

//    public void startNetworkUpdates() {
//
//        boolean ACCESS_FINE_OK = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//
//        if (ACCESS_FINE_OK) {
//
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListenerNetwork);
//            Toast.makeText(this, "Network provider started running", Toast.LENGTH_LONG).show();
//
//        }
//
//    }
//
//
//    private final LocationListener locationListenerNetwork = new LocationListener() {
//
//        private double longitudeNetwork, latitudeNetwork;
//        private LatLng centrar;
//
//        public void onLocationChanged(Location location) {
//            longitudeNetwork = location.getLongitude();
//            latitudeNetwork = location.getLatitude();
//
//            centrar = new LatLng(latitudeNetwork, longitudeNetwork);
//            map.addMarker(new MarkerOptions().position(centrar).title("Yo"));
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(centrar, 17));
//
////            runOnUiThread(new Runnable() {
////                @Override
////                public void run() {
//
//                    // se agrega al polyline la nuevo posicion
//                    polylineOptions.add(new LatLng(latitudeNetwork, longitudeNetwork));
//                    map.addPolyline(polylineOptions);
//
//                    Toast.makeText(GPSservice.this, "Network Provider update", Toast.LENGTH_SHORT).show();
////                }
////            });
//        }
//
//        @Override
//        public void onStatusChanged(String s, int i, Bundle bundle) {
//        }
//
//        @Override
//        public void onProviderEnabled(String s) {
//
//        }
//
//        @Override
//        public void onProviderDisabled(String s) {
//
//        }
//    };

    public void startGPSUpdates() {

        boolean ACCESS_FINE_OK = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (ACCESS_FINE_OK) {

            Long segundos = Long.valueOf(getSharedPref().getString("minTime", "3").split(" ")[0]);
            Float metros = Float.valueOf(getSharedPref().getString("minDist", "10").split(" ")[0]);

            map.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, segundos * 1000, metros, locationListenerGPS);
            MessageHelper.toast(this,"GPS provider started running", Toast.LENGTH_LONG);

        }

    }

    private final LocationListener locationListenerGPS = new LocationListener() {

        private double longitudeGPS, latitudeGPS;
        private LatLng centrar;

        public void onLocationChanged(Location location) {

            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();
            centrar = new LatLng(latitudeGPS, longitudeGPS);

            String zoom = getSharedPref().getString("centerZoom", "1").split(" ")[0];

            map.clear();
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(centrar, 17));
//            map.addMarker(new MarkerOptions().position(centrar).title("Tu posición"));

            polylineOptions.add(new LatLng(latitudeGPS, longitudeGPS));
            map.addPolyline(polylineOptions);

            // TODO > Acomodar vez que este probado
//            try{
//                ArrayList<Position> positions = new ArrayList<Position>();
//                positions.add(new Position(latitudeGPS, longitudeGPS));
//                TrackingServiceConnector.getInstance(GPSservice.this).nuevasPosiciones(3, positions);
//            }catch (Exception e){
//                MessageHelper.toast(GPSservice.this, "No se pudo enviar una posicion GPS", Toast.LENGTH_SHORT);
//            }

            Toast.makeText(GPSservice.this, "GPS Provider update", Toast.LENGTH_SHORT).show();

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

    public void stopGPSUpdates(){

        locationManager.removeUpdates(locationListenerGPS);
        MessageHelper.toast(this,"GPS provider stoped", Toast.LENGTH_LONG);

    }


    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public void setSharedPref(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }
}
