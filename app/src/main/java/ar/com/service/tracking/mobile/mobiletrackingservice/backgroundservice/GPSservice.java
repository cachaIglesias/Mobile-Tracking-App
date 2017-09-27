package ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
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

import java.util.ArrayList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.endpoint.TrackingServiceConnector;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Position;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

public class GPSservice extends Service {

    private static final String TAG = "GPSService";

    private PolylineOptions polylineOptions;
    private GoogleMap map;
    private List<MarkerOptions> markers;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    final int REQUEST_CHECK_SETTINGS = 0;

    private final IBinder mBinder = new GPSbinder(GPSservice.this);

    private SharedPreferences sharedPref;

    private Activity activity;

    private GoogleApiClient mGoogleApiClient = null;

    private ActivityFence activityFence;

    private final static String FENCE_RECEIVER_ACTION = "ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice.FENCE_RECEIVER_ACTION";

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setParameters(PolylineOptions activiyMapPolylineOptions, GoogleMap activiyMapMap, List<MarkerOptions> markers, Activity activity) {

        setPolylineOptions(activiyMapPolylineOptions);
        setMap(activiyMapMap);
        setMarkers(markers);
        setActivity(activity);

        sharedPref = getSharedPreferences("SettingFile", MODE_PRIVATE);

//        activityFence = new ActivityFence(activity);
//
//        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
//        activityFence.setMyPendingIntent(PendingIntent.getBroadcast(activity, 0, intent, 0));
//        activityFence.setActivityFenceReceiver(new ActivityFenceReceiver());
//        this.registerReceiver(activityFence.getActivityFenceReceiver(), new IntentFilter(FENCE_RECEIVER_ACTION));
//
//        activityFence.startFence();

    }

    public void stopGPSUpdates(){

        // frenar Google Play Services Location
        if (this.getmLocationCallback() != null){
            this.getmFusedLocationClient().removeLocationUpdates(this.getmLocationCallback());
        }

        Log.w(TAG, "Servicio GPS background detenido");

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

                    // String zoom = getSharedPref().getString("centerZoom", "1").split(" ")[0];

                    getMap().clear();
                    getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(centrar, 17));

                    getPolylineOptions().add(new LatLng(latitudeGPS, longitudeGPS));
                    getMap().addPolyline(getPolylineOptions());

                    try{
                        ArrayList<Position> positions = new ArrayList<Position>();
                        positions.add(new Position(latitudeGPS, longitudeGPS));
                        TrackingServiceConnector.getInstance(GPSservice.this, null).nuevasPosiciones(3, positions);
                    }catch (Exception e){
                        Log.e(TAG, "No se pudo enviar una posicion GPS, Error: " + e.toString());
                        MessageHelper.toast(GPSservice.this, "No se pudo enviar una posicion GPS", Toast.LENGTH_SHORT);
                    }

                    for (MarkerOptions markerOptions: getMarkers()) {
                        Marker marker = getMap().addMarker(markerOptions);
                        marker.setTag("");
                    }

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

        Log.w(TAG, "Servicio GPS background iniciado");

//        if (mGoogleApiClient == null){
//
//            mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
//                    .addApi(Awareness.API)
//                    .build();
//            mGoogleApiClient.connect();
//
//            Awareness.SnapshotApi.getDetectedActivity(mGoogleApiClient)
//                    .setResultCallback(new ResultCallback<DetectedActivityResult>() {
//                        @Override
//                        public void onResult(@NonNull DetectedActivityResult detectedActivityResult) {
//                            if (!detectedActivityResult.getStatus().isSuccess()) {
//                                Log.e(TAG, "Could not get the current activity.");
//                                return;
//                            }
//                            ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
//                            DetectedActivity probableActivity = ar.getMostProbableActivity();
//                            Log.e(TAG, probableActivity.toString());
//                            MessageHelper.showOnlyAlert(getActivity(),"aviso","se la actividad! : " + probableActivity.toString());
//                        }
//                    });
//
//        }

//        Awareness.SnapshotApi.getLocation(mGoogleApiClient)
//                .setResultCallback(new ResultCallback<LocationResult>() {
//                    @Override
//                    public void onResult(@NonNull LocationResult locationResult) {
//                        if (!locationResult.getStatus().isSuccess()) {
//                            Log.e(TAG, "Could not get location.");
//                            return;
//                        }
//                        Location location = locationResult.getLocation();
//                        Log.i(TAG, "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
//                    }
//                });

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
