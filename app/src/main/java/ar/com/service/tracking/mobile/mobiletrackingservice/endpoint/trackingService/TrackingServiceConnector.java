package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint.trackingService;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.gustavofao.jsonapi.Models.JSONApiObject;

import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.activity.state.MapsActivityState;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservices.geofence.GeofenceTransitionService;
import ar.com.service.tracking.mobile.mobiletrackingservice.endpoint.OrderTrackingServiceObserver;
import ar.com.service.tracking.mobile.mobiletrackingservice.endpoint.ResponseObject;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Trace;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Position;
import retrofit2.Call;

/**
 * Created by miglesias on 16/07/17.
 */

public class TrackingServiceConnector {

    private static String TAG = "TrackingServiceCon";

    private static TrackingServiceConnector instance = null;
    private Context lastContext = null;
    private Activity lastActivity = null;
    private Call<JSONApiObject> call;
    private TrackingService service;

    protected TrackingServiceConnector() {
        // Exists only to defeat instantiation.
    }

    public static TrackingServiceConnector getInstance(Context context, Activity activity) {

        if(instance == null) {
            instance = new TrackingServiceConnector();
            instance.configurar();
        }
        instance.setLastContext(context);
        instance.setLastActivity(activity);
        return instance;

    }

    private void configurar() {

        setService(TrackingService.retrofit.create(TrackingService.class));

    }

    public void marcarComoFinalizado(Integer orderID){

        ResponseObject responseObject = new ResponseObject(getLastContext());
        this.setCall(getService().marcarComoFinalizado(orderID));
        this.getCall().enqueue(responseObject);

        Log.w(TAG, "Servicio HTTP utilizado: marcarComoFinalizado");

    }


    public void marcarComoCancelado(Integer orderID){

        ResponseObject responseObject = new ResponseObject(getLastContext());
        this.setCall(getService().marcarComoCancelado(orderID));
        this.getCall().enqueue(responseObject);

        Log.w(TAG, "Servicio HTTP utilizado: marcarComoCancelado");

    }

    public void obtenerEntregaActiva(Integer deliveryManID, GoogleMap map, MapsActivityState mapsActivityState){

//        setSharedPref( getSharedPreferences("SettingFile", MODE_PRIVATE));
//                Long segundos = Long.valueOf(getSharedPref().getString("minTime", "3").split(" ")[0]);
//        Float metros = Float.valueOf(getSharedPref().getString("minDist", "10").split(" ")[0]);
        // TODO > Geofence radio y distancia estan hardcodeados, se deberian obtener de la configuracion.
        GeofenceTransitionService geofenceTransitionService = GeofenceTransitionService.getInstance(this.getLastActivity(), 150, 3);
        OrderTrackingServiceObserver orderObserver = new OrderTrackingServiceObserver(map, mapsActivityState, geofenceTransitionService);
        ResponseObject responseObject = new ResponseObject(getLastContext(), orderObserver);
        setCall(getService().getEntregaActiva(deliveryManID));
            // forma asincronica
        getCall().enqueue(responseObject);
            // forma sincronica
//        try {
//            Response<JSONApiObject> response = getCall().execute();
//            responseObject.onResponse(getCall(),response);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Log.w(TAG, "Servicio HTTP utilizado: obtenerEntregaActiva");

    }

    public void nuevasPosiciones(Integer deliveryManID, List<Position> positions){

        ResponseObject responseObject = new ResponseObject(getLastContext());

        setCall(getService().nuevasPosiciones(new Trace(positions.get(0), deliveryManID)));
        getCall().enqueue(responseObject);

        Log.w(TAG, "Servicio HTTP utilizado: nuevasPosiciones");

    }

    public Context getLastContext() {
        return lastContext;
    }

    public void setLastContext(Context lastContext) {
        this.lastContext = lastContext;
    }

    public Activity getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Activity lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Call<JSONApiObject> getCall() {
        return call;
    }

    public void setCall(Call<JSONApiObject> call) {
        this.call = call;
    }

    public TrackingService getService() {
        return service;
    }

    public void setService(TrackingService service) {
        this.service = service;
    }
}
