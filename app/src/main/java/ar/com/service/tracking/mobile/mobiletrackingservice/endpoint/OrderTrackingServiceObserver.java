package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import android.util.Log;
import android.widget.Toast;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;
import ar.com.service.tracking.mobile.mobiletrackingservice.activity.state.MapsActivityState;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservices.geofence.GeofenceTransitionService;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservices.directions.GoogleDirectionsAPI;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservices.directions.GoogleDirectionsAPIObserver;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Business;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

/**
 * Created by miglesias on 06/08/17.
 */

public class OrderTrackingServiceObserver extends AbstractTrackingServiceObserver {

    private static String TAG = "OrderTrackingServiceObs";

    private GoogleMap map;
    private MapsActivityState mapsActivityState;
    private GeofenceTransitionService geofenceTransitionService;
    private Business business;

    public OrderTrackingServiceObserver(GoogleMap map, MapsActivityState mapsActivityState, GeofenceTransitionService geofenceTransitionService){

        this.setMap(map);
        this.setMapsActivityState(mapsActivityState);
        this.setGeofenceTransitionService(geofenceTransitionService);
    }

    @Override
    public void update() {

        List<Order> adapterOrders = this.getMapsActivityState().getOrderAdapter().getOrders();
        Boolean notificar = false;
        Integer posicion = -1;

        try{

            for (int i = 0 ; i <= this.getResponseObjectList().size() - 1; i++){
                if(i == 0){
                    // TODO > CREO QUE ACA DEBERIA ENTRAR SOLO UNA VEZ Y NO POR CADA ACTUALIZACION DEL OBSERVER.

                    this.setBusiness((Business) this.getResponseObjectList().get(i));

                    LatLng position = new LatLng(this.getBusiness().getPosition().getLatitude(), this.getBusiness().getPosition().getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(position)
                            .title(this.getBusiness().getAddress())
                            .snippet(this.getBusiness().getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pizza_business))
                            .alpha(0.7f);
                    this.getMapsActivityState().getMarkers().add(markerOptions);
                    Log.w(TAG, "Se agregó el marcador del negocio: " + this.getBusiness().toString() );

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
                            this.getMapsActivityState().getMarkers().remove(orderIndex);
                            Log.w(TAG, "Se removió el marcador de la orden: " + order.toString() + " | " + " De la posicion: " + orderIndex);

                            // TODO > remover GEOFENCE de la orden eliminada.
                            Log.w(TAG, "Se removió el Geofence de la orden: " + order.toString() );

                            notificar = true;
                        }
                    } else {
                        if (!esUnEstadoFinal) {
                            adapterOrders.add(posicion, order);
                            Log.w(TAG, "Se agregó la orden: " + order.toString() + " | " + " En la posicion: " + posicion );
                            this.getMapsActivityState().getOrderAdapter().notifyDataSetChanged();

                            LatLng position = new LatLng(order.getPosition().getLatitude(), order.getPosition().getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(position)
                                    .title(order.getAddress())
                                    .snippet(order.getProducto() + " valor: " + order.getValor() + " cliente: " + order.getDestinatario() )
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination))
                                    .alpha(0.7f);
                            this.getMapsActivityState().getMarkers().add(markerOptions);
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
            MessageHelper.toast(getMapsActivityState().getOrderAdapter().getContext(), "No se pudieron recuperar las ordenes: " + e.toString(),  Toast.LENGTH_LONG);
        }

        if (notificar){
            // limpiar el mapa, cargar los markers destinos , sin perder el polyline!!!!
            this.getMap().clear();
            this.getMap().addPolyline(this.getMapsActivityState().getRepartidorPolyline());
//            map.notify();
//            this.getPolylineOptions().notify();
            for (MarkerOptions markerOptions: this.getMapsActivityState().getMarkers()) {
                Marker marker = this.getMap().addMarker(markerOptions);
                marker.setTag("");
            }

            // se inicia el servicio de Geofencing
            this.getGeofenceTransitionService().startGeofencingMonitoring();

            // se arma el recorrido
            GoogleDirectionsAPIObserver googleDirectionsAPIObserver = new GoogleDirectionsAPIObserver(map, this.getMapsActivityState());
            GoogleDirectionsAPI googleDirectionsAPI = new GoogleDirectionsAPI(this.getMapsActivityState().getOrderAdapter().getContext(), googleDirectionsAPIObserver);
            googleDirectionsAPI.route(this.getBusiness(), this.getMapsActivityState().getOrderAdapter());

            Log.i(TAG, "Recorrido del repartidor actualizado");
            MessageHelper.showOnlyAlert(this.getGeofenceTransitionService().getActivity(), "Atencion!", "Se actualizó la lista de ordenes, por lo tanto el recorrido sugerido tambien será actualizado." );
        }

    }

    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
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


    public MapsActivityState getMapsActivityState() {
        return mapsActivityState;
    }

    public void setMapsActivityState(MapsActivityState mapsActivityState) {
        this.mapsActivityState = mapsActivityState;
    }
}
