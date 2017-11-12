package ar.com.service.tracking.mobile.mobiletrackingservice.activity.state;

import android.content.Context;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.adapter.OrderAdapter;

/**
 * Created by miglesias on 27/09/17.
 */

public class MapsActivityState {

    private static final String TAG = "MapsActivityState";

    private List<MarkerOptions> markers = new LinkedList<MarkerOptions>();

    private PolylineOptions repartidorPolyline;

    private PolylineOptions entregaPolyline;

    private OrderAdapter orderAdapter;

    private static MapsActivityState instance = null;

    public static MapsActivityState getInstance(Context context) {

        if(instance == null) {
            instance = new MapsActivityState(context);
        }
        return instance;

    }

    public MapsActivityState(Context context){
        this.setOrderAdapter(new OrderAdapter(context, new LinkedList<Order>()));
    }


    public List<MarkerOptions> getMarkers() {
        return markers;
    }

    public void setMarkers(List<MarkerOptions> markers) {
        this.markers = markers;
    }

    public OrderAdapter getOrderAdapter() {
        return orderAdapter;
    }

    public void setOrderAdapter(OrderAdapter orderAdapter) {
        this.orderAdapter = orderAdapter;
    }

    public PolylineOptions getRepartidorPolyline() {
        return repartidorPolyline;
    }

    public void setRepartidorPolyline(PolylineOptions repartidorPolyline) {
        this.repartidorPolyline = repartidorPolyline;
    }

    public PolylineOptions getEntregaPolyline() {
        return entregaPolyline;
    }

    public void setEntregaPolyline(PolylineOptions entregaPolyline) {
        this.entregaPolyline = entregaPolyline;
    }
}
