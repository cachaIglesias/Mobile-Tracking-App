package ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservices.geofence;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

/**
 * Created by miglesias on 10/09/17.
 */

public class GeofenceTransitionService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    private static final String TAG = "GeofenceTransitionS";

    private int GEOFENCE_EXPIRATION_IN_MILLISECONDS = 3000;
    private int GEOFENCE_RADIUS_IN_METERS = 150;

    private List<Geofence> mGeofenceList;
    private GeofencingClient mGeofencingClient;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;

    private Activity activity;

    private static GeofenceTransitionService instance = null ;

    public GeofenceTransitionService(Activity activity) {

        this.setActivity(activity);
        this.setmGeofenceList(new ArrayList<Geofence>());

    }

    public static GeofenceTransitionService getInstance(Activity activity, int metros, int segundos) {

        if(instance == null) {
            instance = new GeofenceTransitionService(activity);
            instance.GEOFENCE_RADIUS_IN_METERS = metros;
            instance.GEOFENCE_EXPIRATION_IN_MILLISECONDS = segundos * 1000 ;
        }
        return instance;

    }

    public void addGeofence(String geofenceKey, LatLng latLng) {

        getmGeofenceList().add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(geofenceKey)

                .setCircularRegion(
                        latLng.latitude,
                        latLng.longitude,
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        Log.i(TAG, "Geofence añadida en posición: " + latLng.toString() + " | Radio en metros: " + GEOFENCE_RADIUS_IN_METERS + " | Tiempo de expiracion: " + GEOFENCE_EXPIRATION_IN_MILLISECONDS );

    }

    private GeofencingRequest getGeofencingRequest() {

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();

    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this.getActivity(), GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this.getActivity(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void startGeofencingMonitoring() {

        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        this.buildGoogleApiClient();
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

//        this.setmGeofencingClient(LocationServices.getGeofencingClient(this.getActivity()));
//
//        this.getmGeofencingClient().addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
//                .addOnSuccessListener(this.getActivity(), new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        // Geofences added
//                        // ...
//                        MessageHelper.toast(getActivity(), "llegaste a tu destino", Toast.LENGTH_LONG);
//                        Log.e(TAG, "llegaste a tu destino");
//                        if (getmGeofenceList().isEmpty()){
//                            stopGeofencingMonitoring();
//                        }
//                    }
//                })
//                .addOnFailureListener(this.getActivity(), new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Failed to add geofences
//                        // ...
//                        MessageHelper.toast(getActivity(), "geofence fallo", Toast.LENGTH_LONG);
//                        Log.e(TAG, "geofence fallo");
//                        if (getmGeofenceList().isEmpty()){
//                            stopGeofencingMonitoring();
//                        }
//                    }
//                });

        Log.i(TAG, "Se inicia el servicio de Geofencing");

    }

    public void stopGeofencingMonitoring(){

        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(this.getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                        Log.w(TAG, "Servicio de Geofence detenido");
                    }
                })
                .addOnFailureListener(this.getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                        Log.e(TAG, "Falla en la detencion del servicio de Geofence");
                        MessageHelper.toast(getActivity(), "Falla en la detencion del servicio de Geofence", Toast.LENGTH_LONG);
                    }
                });

    }

    public GeofencingClient getmGeofencingClient() {
        return mGeofencingClient;
    }

    public void setmGeofencingClient(GeofencingClient mGeofencingClient) {
        this.mGeofencingClient = mGeofencingClient;
    }

    public List<Geofence> getmGeofenceList() {
        return mGeofenceList;
    }

    public void setmGeofenceList(List<Geofence> mGeofenceList) {
        this.mGeofenceList = mGeofenceList;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
// hacer algo
        Log.i(TAG, "Coneccion Geofence establecida");
        MessageHelper.toast(this.getActivity(), "Coneccion Geofence establecida", Toast.LENGTH_LONG);
        if (!mGoogleApiClient.isConnected()) {
            Log.e(TAG, "Google API Client not connected!");
            MessageHelper.toast(this.getActivity(), "Google API Client not connected!", Toast.LENGTH_SHORT);
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
            Log.i(TAG, "Geofence configurado exitosamente");
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, "Excepcion se seguridad en Geofence: " + securityException.toString());
            MessageHelper.toast(this.getActivity(), "Excepcion se seguridad en Geofence: " + securityException.toString(), Toast.LENGTH_LONG);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
//        mGoogleApiClient.connect();
        Log.i(TAG, "Coneccion Geofence suspendida");
        MessageHelper.toast(this.getActivity(), "Coneccion Geofence suspendida", Toast.LENGTH_LONG);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Do something with connectionResult.getErrorCode());
        Log.e(TAG, "Coneccion Geofence fallida");
        MessageHelper.toast(this.getActivity(), "Coneccion Geofence fallida", Toast.LENGTH_LONG);
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.i(TAG, "Resultado Geofence: " + status.getStatusMessage());
            MessageHelper.toast(this.getActivity(), "Resultado Geofence: " + status.getStatusMessage(), Toast.LENGTH_LONG);
        } else {
            // Get the status code for the error and log it using a user-friendly message.
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    status.getStatusCode());
            Log.i(TAG, "Resultado Geofence: " + status.getStatusMessage());
            MessageHelper.toast(this.getActivity(), "Resultado Geofence: " + status.getStatusMessage(), Toast.LENGTH_LONG);
        }
    }
}
