package ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.common.internal.safeparcel.zzd;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.Serializable;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.activity.MapsActivity;
import ar.com.service.tracking.mobile.mobiletrackingservice.endpoint.TrackingServiceConnector;

/**
 * Created by miglesias on 07/09/17.
 */

public class GPSServiceConnection implements ServiceConnection {

    private static final String TAG = "GPSServiceConnection";

    private GPSservice mService;
    private GPSbinder binder;
    private boolean mBound = false;

    private PolylineOptions polylineOptions;
    private GoogleMap map;
    private List<MarkerOptions> markers;

    private Activity activity = null;
    private Context lastContext = null;

    private static GPSServiceConnection instance = null;

    public static GPSServiceConnection getInstance(PolylineOptions polylineOptions, GoogleMap map, List<MarkerOptions> markers, MapsActivity mapsActivity, Context context) {

        if(instance == null) {
            instance = new GPSServiceConnection(polylineOptions, map, markers, mapsActivity, context);
        }
        if (instance.getMap() == null ){
            instance.setMap(map);
        }
        if (instance.getPolylineOptions() == null ){
            instance.setPolylineOptions(polylineOptions);
        }
        if (instance.getMarkers() == null ){
            instance.setMarkers(markers);
        }
        return instance;

    }

    public GPSServiceConnection(PolylineOptions polylineOptions, GoogleMap map, List<MarkerOptions> markers, MapsActivity mapsActivity, Context context) {
        this.setPolylineOptions(polylineOptions);
        this.setMap(map);
        this.setMarkers(markers);
        this.setActivity(mapsActivity);
        this.setLastContext(context);
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        setBinder((GPSbinder) service);
        setmService(getBinder().getService());
        getmService().setParameters(getPolylineOptions(), getMap(), getMarkers(), this.getActivity());
        setmBound(true);

        Log.w(TAG, "Conexion con servicio GPS background establecida");

        // siempre agregar esta excepcion que es la unica que tira los serivcios y ocurre cuando se pierde la coneccion: DeadObjectException
        // se llama al servicio de actualizacion de posicones geograficas GPS o GPSA.
        this.generateLocationClient();
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        Log.w(TAG, "Conexion con servicio GPS background terminada");
        this.stopGPSUpdates();
        setmBound(false);
    }

    public void generateLocationClient(){

        if( getmService() != null ){

            getmService().generateLocationClient();

        }

    }

    public void stopGPSUpdates(){

        if( getmService() != null ){

            getmService().stopGPSUpdates();

        }

    }

    public GPSservice getmService() {
        return mService;
    }

    public void setmService(GPSservice mService) {
        this.mService = mService;
    }

    public GPSbinder getBinder() {
        return binder;
    }

    public void setBinder(GPSbinder binder) {
        this.binder = binder;
    }

    public boolean ismBound() {
        return mBound;
    }

    public void setmBound(boolean mBound) {
        this.mBound = mBound;
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


    public Context getLastContext() {
        return lastContext;
    }

    public void setLastContext(Context lastContext) {
        this.lastContext = lastContext;
    }

    public void updateMap(GoogleMap map) {
        this.setMap(map);
        if(this.getmService() != null){
            this.getmService().updateMap(map);
        }
    }
}
