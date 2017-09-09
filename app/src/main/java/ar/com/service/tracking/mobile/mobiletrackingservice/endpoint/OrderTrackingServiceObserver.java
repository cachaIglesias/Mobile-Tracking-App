package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.gustavofao.jsonapi.Models.Resource;

import java.util.List;

import android.widget.Toast;

import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.adapter.OrderAdapter;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

/**
 * Created by miglesias on 06/08/17.
 */

public class OrderTrackingServiceObserver extends AbstractTrackingServiceObserver {

    private OrderAdapter orderAdapter;
    private List<MarkerOptions> markers;
    private GoogleMap map;
    private PolylineOptions polylineOptions;

    public OrderTrackingServiceObserver(OrderAdapter orderAdapter, List<MarkerOptions> markers, GoogleMap map, PolylineOptions polylineOptions){
        this.setOrderAdapter(orderAdapter);
        this.setMarkers(markers);
        this.setMap(map);
        this.setPolylineOptions(polylineOptions);
    }

    @Override
    public void update() {

        List<Order> adapterOrders = this.getOrderAdapter().getOrders();
        Boolean notificar = false;
        Integer posicion = -1;

        for (Resource resource: this.getResponseObjectList()) {

            Order order = (Order) resource;

            Boolean cambioDeEstado = false;
            int orderIndex = adapterOrders.indexOf(order);
            if(orderIndex != -1){
                cambioDeEstado = adapterOrders.get(adapterOrders.indexOf(order)).getStatus().compareTo(order.getStatus()) != 0;
            }
            Boolean esUnEstadoFinal = order.getStatus().equalsIgnoreCase("canceled") || order.getStatus().equalsIgnoreCase("suspended") || order.getStatus().equalsIgnoreCase("finalized");

            if (!esUnEstadoFinal){
                posicion += 1;
            }

            if (adapterOrders.contains(order)){
                if (cambioDeEstado && esUnEstadoFinal){
                    adapterOrders.remove(order);
//                    this.getOrderAdapter().remove(order);
                    this.getMarkers().remove(orderIndex);

                    notificar = true;
                }
            } else{
                if (!esUnEstadoFinal){
                    adapterOrders.add(posicion, order);
                    this.getOrderAdapter().notifyDataSetChanged();

                    LatLng position = new LatLng(order.getPosition().getLatitude(), order.getPosition().getLongitude());
                    this.getMarkers().add(new MarkerOptions().position(position).title(order.getAddress()));

//                    this.getOrderAdapter().add(order);
                    notificar = true;
                }
            }
        }

        if (notificar){
            // limpiar el mapa, cargar los markers destinos , sin perder el polyline!!!!
            map.clear();
            map.addPolyline(this.getPolylineOptions());
            for (MarkerOptions markerOptions: this.getMarkers()) {
                Marker marker = this.getMap().addMarker(markerOptions);
                marker.setTag("");
            }

            // TODO > aca se deberia armar el recorrido

            // TODO > MOSTRAR OTRO TIPO DE MENSAJE
            MessageHelper.toast(getOrderAdapter().getContext(), "Se actualizó la lista de ordenes, por lo tanto el recorrido sugerido tambien será actualizado.", Toast.LENGTH_LONG);
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
}
