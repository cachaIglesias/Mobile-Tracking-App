package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.gustavofao.jsonapi.Models.Resource;

import java.util.List;

import android.util.Log;
import android.widget.Toast;

import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice.GeofenceTransitionService;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Business;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.adapter.OrderAdapter;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

/**
 * Created by miglesias on 06/08/17.
 */

public class OrderTrackingServiceObserver extends AbstractTrackingServiceObserver {

    private static String TAG = "OrderTrackingServiceObs";

    private OrderAdapter orderAdapter;
    private List<MarkerOptions> markers;
    private GoogleMap map;
    private PolylineOptions polylineOptions;
    private GeofenceTransitionService geofenceTransitionService;
    private Business business;

    public OrderTrackingServiceObserver(OrderAdapter orderAdapter, List<MarkerOptions> markers, GoogleMap map, PolylineOptions polylineOptions, GeofenceTransitionService geofenceTransitionService){
        this.setOrderAdapter(orderAdapter);
        this.setMarkers(markers);
        this.setMap(map);
        this.setPolylineOptions(polylineOptions);
        this.setGeofenceTransitionService(geofenceTransitionService);
    }

    @Override
    public void update() {

        List<Order> adapterOrders = this.getOrderAdapter().getOrders();
        Boolean notificar = false;
        Integer posicion = -1;

        try{

            for (int i = 0 ; i <= this.getResponseObjectList().size() - 1; i++){
                if(i == 0){

                    Business business = (Business) this.getResponseObjectList().get(i);
                    this.setBusiness(business);

                }else{

                    Order order = (Order) this.getResponseObjectList().get(i);

                    Boolean cambioDeEstado = false;
                    int orderIndex = adapterOrders.indexOf(order);
                    if (orderIndex != -1) {
                        cambioDeEstado = adapterOrders.get(adapterOrders.indexOf(order)).getStatus().compareTo(order.getStatus()) != 0;
                    }
                    Boolean esUnEstadoFinal = order.getStatus().equalsIgnoreCase("canceled") || order.getStatus().equalsIgnoreCase("suspended") || order.getStatus().equalsIgnoreCase("finalized");

                    if (!esUnEstadoFinal) {
                        posicion += 1;
                    }

                    if (adapterOrders.contains(order)) {
                        if (cambioDeEstado && esUnEstadoFinal) {
                            adapterOrders.remove(order);
                            Log.w(TAG, "Se removió la orden: " + order.toString() + " | " + " De la posicion: " + orderIndex);
                            // this.getOrderAdapter().remove(order);
                            this.getMarkers().remove(orderIndex);
                            Log.w(TAG, "Se removió el marcador de la orden: " + order.toString() + " | " + " De la posicion: " + orderIndex);

                            // TODO > remover GEOFENCE de la orden eliminada.
                            Log.w(TAG, "Se removió el Geofence de la orden: " + order.toString() );

                            notificar = true;
                        }
                    } else {
                        if (!esUnEstadoFinal) {
                            adapterOrders.add(posicion, order);
                            Log.w(TAG, "Se agregó la orden: " + order.toString() + " | " + " En la posicion: " + posicion );
                            this.getOrderAdapter().notifyDataSetChanged();

                            LatLng position = new LatLng(order.getPosition().getLatitude(), order.getPosition().getLongitude());
                            this.getMarkers().add(new MarkerOptions().position(position).title(order.getAddress()));
                            Log.w(TAG, "Se agregó el marcador de la orden: " + order.toString() + " | " + " En la posicion: " + orderIndex);

                            this.getGeofenceTransitionService().addGeofence(order.getAddress(), position);
                            Log.w(TAG, "Se agregó el Geofence de la orden: " + order.toString() );

                            // this.getOrderAdapter().add(order);
                            notificar = true;
                        }
                    }

                }
            }

        }catch(Exception e){
            Log.e(TAG, "No se pudieron recuperar las ordenes: " + e.toString());
            MessageHelper.toast(getOrderAdapter().getContext(), "No se pudieron recuperar las ordenes: " + e.toString(),  Toast.LENGTH_LONG);
        }

        if (notificar){
            // limpiar el mapa, cargar los markers destinos , sin perder el polyline!!!!
            map.clear();
            map.addPolyline(this.getPolylineOptions());
            for (MarkerOptions markerOptions: this.getMarkers()) {
                Marker marker = this.getMap().addMarker(markerOptions);
                marker.setTag("");
            }

            // se inicia el servicio de Geofencing
            this.getGeofenceTransitionService().startGeofencingMonitoring();

            // TODO > aca se deberia armar el recorrido
            Log.i(TAG, "Recorrido del repartidor actualizado");
            MessageHelper.showOnlyAlert(this.getGeofenceTransitionService().getActivity(), "Atencion!", "Se actualizó la lista de ordenes, por lo tanto el recorrido sugerido tambien será actualizado." );
        }

    }

    public OrderAdapter getOrderAdapter() {
        return orderAdapter;
    }

    public void setOrderAdapter(OrderAdapter orderAdapter) {
        this.orderAdapter = orderAdapter;
    }

    public List<MarkerOptions> getMarkers() {
        return markers;
    }

    public void setMarkers(List<MarkerOptions> markers) {
        this.markers = markers;
    }

    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public void setPolylineOptions(PolylineOptions polylineOptions) {
        this.polylineOptions = polylineOptions;
    }

    public GeofenceTransitionService getGeofenceTransitionService() {
        return geofenceTransitionService;
    }

    public void setGeofenceTransitionService(GeofenceTransitionService geofenceTransitionService) {
        this.geofenceTransitionService = geofenceTransitionService;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }
}
