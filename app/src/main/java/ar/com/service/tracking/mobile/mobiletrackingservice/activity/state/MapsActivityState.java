package ar.com.service.tracking.mobile.mobiletrackingservice.activity.state;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.activity.MapsActivity;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice.GPSServiceConnection;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.adapter.OrderAdapter;

/**
 * Created by miglesias on 27/09/17.
 */

public class MapsActivityState {

    private static final String TAG = "MapsActivityState";

    private List<MarkerOptions> markers = new LinkedList<MarkerOptions>();

    private OrderAdapter adapter;

    private static MapsActivityState instance = null;

    public static MapsActivityState getInstance(Context context) {

        if(instance == null) {
            instance = new MapsActivityState(context);
        }
        return instance;

    }

    public MapsActivityState(Context context){
        this.adapter = new OrderAdapter(context, new LinkedList<Order>());
    }


    public List<MarkerOptions> getMarkers() {
        return markers;
    }

    public void setMarkers(List<MarkerOptions> markers) {
        this.markers = markers;
    }

    public OrderAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(OrderAdapter adapter) {
        this.adapter = adapter;
    }
}
