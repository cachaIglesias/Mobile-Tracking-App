package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.gustavofao.jsonapi.JSONApiConverter;
import com.gustavofao.jsonapi.Models.ErrorModel;
import com.gustavofao.jsonapi.Models.JSONApiObject;
import com.gustavofao.jsonapi.Models.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Position;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.adapter.OrderAdapter;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by miglesias on 16/07/17.
 */

public class TrackingServiceConnector {

    private static TrackingServiceConnector instance = null;
    private Context lastContext = null;
    private Call<JSONApiObject> call;
    private TrackingService service;

    protected TrackingServiceConnector() {
        // Exists only to defeat instantiation.
    }

    public static TrackingServiceConnector getInstance(Context context) {

        if(instance == null) {
            instance = new TrackingServiceConnector();
            instance.configurar();
        }
        instance.setLastContext(context);
        return instance;

    }

    private void configurar() {

        setService(TrackingService.retrofit.create(TrackingService.class));

    }

    public void marcarComoFinalizado(Integer orderID){

        ResponseObject responseObject = new ResponseObject(getLastContext());
        this.setCall(getService().marcarComoFinalizado(orderID));
        this.getCall().enqueue(responseObject);

    }


    public void marcarComoCancelado(Integer orderID){

        ResponseObject responseObject = new ResponseObject(getLastContext());
        this.setCall(getService().marcarComoCancelado(orderID));
        this.getCall().enqueue(responseObject);

    }

    public void getEntregaActiva(Integer deliveryManID, OrderAdapter orderAdapter, List<MarkerOptions> markers, GoogleMap map, PolylineOptions polylineOptions){

            OrderTrackingServiceObserver orderObserver = new OrderTrackingServiceObserver(orderAdapter, markers, map, polylineOptions);
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

    }

    public void nuevasPosiciones(Integer deliveryManID, List<Position> positions){

        ResponseObject responseObject = new ResponseObject(getLastContext());
        setCall(getService().nuevasPosiciones(deliveryManID, positions));
        getCall().enqueue(responseObject);

    }

    public Context getLastContext() {
        return lastContext;
    }

    public void setLastContext(Context lastContext) {
        this.lastContext = lastContext;
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
