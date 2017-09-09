package ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

public class GPSservice extends Service {

    private LocationManager locationManager;
    private PolylineOptions polylineOptions;
    private GoogleMap map;
    private List<MarkerOptions> markers;

//    private Geocoder geocoder;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    final int REQUEST_CHECK_SETTINGS = 0;

    private final IBinder mBinder = new GPSbinder(GPSservice.this);

    private SharedPreferences sharedPref;

    private Activity activity;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setParameters(PolylineOptions activiyMapPolylineOptions, GoogleMap activiyMapMap, List<MarkerOptions> markers, Activity activity) {

//        locationManager = activiyMapLocationManager;
        setPolylineOptions(activiyMapPolylineOptions);
        setMap(activiyMapMap);
        setMarkers(markers);
        setActivity(activity);

        sharedPref = getSharedPreferences("SettingFile", MODE_PRIVATE);

//        geocoder = new Geocoder(this, Locale.getDefault());

    }

//    public void startGPSUpdates() {
//
//        boolean ACCESS_FINE_OK = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//
//        if (ACCESS_FINE_OK) {
//
//            Long segundos = Long.valueOf(getSharedPref().getString("minTime", "3").split(" ")[0]);
//            Float metros = Float.valueOf(getSharedPref().getString("minDist", "10").split(" ")[0]);
//
//            getMap().setMyLocationEnabled(true);
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, segundos * 1000, metros, locationListenerGPS);
//            MessageHelper.toast(this,"GPS provider started running", Toast.LENGTH_LONG);
//
//        }
//
//    }

//    private final LocationListener locationListenerGPS = new LocationListener() {
//
//        private double longitudeGPS, latitudeGPS;
//        private LatLng centrar;
//
//        public void onLocationChanged(Location location) {
//
//            longitudeGPS = location.getLongitude();
//            latitudeGPS = location.getLatitude();
//            centrar = new LatLng(latitudeGPS, longitudeGPS);
//
//            String zoom = getSharedPref().getString("centerZoom", "1").split(" ")[0];
//
//            getMap().clear();
//            getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(centrar, 17));
////            map.addMarker(new MarkerOptions().position(centrar).title("Tu posición"));
//
//            getPolylineOptions().add(new LatLng(latitudeGPS, longitudeGPS));
//            getMap().addPolyline(getPolylineOptions());
//
//            // TODO > Acomodar vez que este probado
////            try{
////                ArrayList<Position> positions = new ArrayList<Position>();
////                positions.add(new Position(latitudeGPS, longitudeGPS));
////                TrackingServiceConnector.getInstance(GPSservice.this).nuevasPosiciones(3, positions);
////            }catch (Exception e){
////                MessageHelper.toast(GPSservice.this, "No se pudo enviar una posicion GPS", Toast.LENGTH_SHORT);
////            }
//
//            Toast.makeText(GPSservice.this, "GPS Provider update", Toast.LENGTH_SHORT).show();
//
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

    public void stopGPSUpdates(){

        // frenar Google Play Services Location
        if (this.getmLocationCallback() != null){
            this.getmFusedLocationClient().removeLocationUpdates(this.getmLocationCallback());
        }

//        locationManager.removeUpdates(locationListenerGPS);
        MessageHelper.toast(this,"GPS provider stoped", Toast.LENGTH_LONG);

    }

    public void generateLocationClient() {

        setmFusedLocationClient( LocationServices.getFusedLocationProviderClient(this));

        LocationRequest mLocationRequest = new LocationRequest();

        Long segundos = Long.valueOf(getSharedPref().getString("minTime", "3").split(" ")[0]);
        Float metros = Float.valueOf(getSharedPref().getString("minDist", "10").split(" ")[0]);

        mLocationRequest.setInterval( segundos * 1000 );
        // Google Play Service Location actualiza al menor intervalo definido por alguna aplicacion del equipo, por lo que se tiene que establecer FastestInterval para definir el tiempo minimo de actualizaciones que soporta la aplicacion, para que las actualizaciones con mayer frecuencia de otras apps no afecte el comportamiento de esta.
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setSmallestDisplacement( metros );
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        setmLocationRequest(mLocationRequest);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Se actualiza el mapa con las posiciones recibidas

                    double longitudeGPS, latitudeGPS;
                    LatLng centrar;

                    longitudeGPS = location.getLongitude();
                    latitudeGPS = location.getLatitude();
                    centrar = new LatLng(latitudeGPS, longitudeGPS);

                    String zoom = getSharedPref().getString("centerZoom", "1").split(" ")[0];

                    getMap().clear();
                    getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(centrar, 17));

                    getPolylineOptions().add(new LatLng(latitudeGPS, longitudeGPS));
                    getMap().addPolyline(getPolylineOptions());
                    for (MarkerOptions markerOptions: getMarkers()) {
                        Marker marker = getMap().addMarker(markerOptions);
                        marker.setTag("");
                    }

                    MessageHelper.toast(GPSservice.this, "Ultima latitud> " + String.valueOf(location.getLatitude()), Toast.LENGTH_SHORT );
                }
            };
        };

        setmLocationCallback(mLocationCallback);

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                boolean ACCESS_FINE_OK = ContextCompat.checkSelfPermission(GPSservice.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

                if (ACCESS_FINE_OK) {

                    getmFusedLocationClient().requestLocationUpdates(getmLocationRequest(), getmLocationCallback(), null /* Looper */);
                }
            }
        });

        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(getActivity(),
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public void setSharedPref(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }


    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public void setPolylineOptions(PolylineOptions polylineOptions) {
        this.polylineOptions = polylineOptions;
    }

    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }


    public FusedLocationProviderClient getmFusedLocationClient() {
        return mFusedLocationClient;
    }

    public void setmFusedLocationClient(FusedLocationProviderClient mFusedLocationClient) {
        this.mFusedLocationClient = mFusedLocationClient;
    }

    public LocationRequest getmLocationRequest() {
        return mLocationRequest;
    }

    public void setmLocationRequest(LocationRequest mLocationRequest) {
        this.mLocationRequest = mLocationRequest;
    }

    public LocationCallback getmLocationCallback() {
        return mLocationCallback;
    }

    public void setmLocationCallback(LocationCallback mLocationCallback) {
        this.mLocationCallback = mLocationCallback;
    }


    public List<MarkerOptions> getMarkers() {
        return markers;
    }

    public void setMarkers(List<MarkerOptions> markers) {
        this.markers = markers;
    }


    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void updateMap(GoogleMap map) {
        map.addPolyline(this.getPolylineOptions());
        this.setMap(map);
    }
}
