package ar.com.service.tracking.mobile.mobiletrackingservice.activity.state;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.model.Business;
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

    private GoogleMap map;

    private Business business;

    private String url = "http://10.0.2.2:3000/";

    private Integer userId = 1;

    private static MapsActivityState instance = null;

    public static MapsActivityState getInstance(Context context) {

        if(instance == null) {
            instance = new MapsActivityState(context);
        }
        return instance;

    }

    public MapsActivityState(Context context){
        this.setOrderAdapter(new OrderAdapter(context, new LinkedList<Order>(), this));
    }


    public List<MarkerOptions> getMarkers() {
        return markers;
    }

    public void setMarkers(List<MarkerOptions> markers) {
        this.markers = markers;
        this.refreshMarkers();
    }

    private void refreshMarkers() {
        for (MarkerOptions markerOptions: this.getMarkers()) {
            this.getMap().addMarker(markerOptions);
        }
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
        this.refreshRepartidorPolyline();
    }

    public void refreshRepartidorPolyline(){
        this.getMap().addPolyline(this.getRepartidorPolyline());
    }

    public PolylineOptions getEntregaPolyline() {
        return entregaPolyline;
    }

    public void setEntregaPolyline(PolylineOptions entregaPolyline) {
        this.entregaPolyline = entregaPolyline;
//        this.refreshEntregaPolyline();
    }

    public void refreshEntregaPolyline(){
        this.getMap().addPolyline(this.getEntregaPolyline());
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
        this.refreshMap();
    }

    public void refreshMap() {
        this.getMap().clear();
        if(this.getEntregaPolyline() != null){
        this.getMap().addPolyline(this.getEntregaPolyline());
        }
        if(this.getRepartidorPolyline() != null) {
            this.getMap().addPolyline(this.getRepartidorPolyline());
        }
        for (MarkerOptions markerOptions: this.getMarkers()) {
            this.getMap().addMarker(markerOptions);
        }
    }

    public void cleanEntregaPolylineFromMap(){
        this.getMap().clear();
        this.setEntregaPolyline(null);
        if(this.getRepartidorPolyline() != null) {
            this.getMap().addPolyline(this.getRepartidorPolyline());
        }
        for (MarkerOptions markerOptions: this.getMarkers()) {
            this.getMap().addMarker(markerOptions);
        }
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public void resetMapsActivityState() {
        this.setMarkers(new LinkedList<MarkerOptions>() );
        this.setEntregaPolyline(null);
        this.setRepartidorPolyline(null);
        this.setBusiness(null);
        this.setOrderAdapter(null);
    }
}
