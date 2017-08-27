package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.ArraySet;

import com.gustavofao.jsonapi.Models.Resource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import android.util.ArraySet;
import android.widget.Toast;

import ar.com.service.tracking.mobile.mobiletrackingservice.activity.MapsActivity;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.adapter.OrderAdapter;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

/**
 * Created by miglesias on 06/08/17.
 */

public class OrderTrackingServiceObserver extends AbstractTrackingServiceObserver {

    private OrderAdapter orderAdapter;

    public OrderTrackingServiceObserver(OrderAdapter orderAdapter){
        this.setOrderAdapter(orderAdapter);
    }

    @Override
    public void update() {

        List<Order> adapterOrders = this.getOrderAdapter().getOrders();
        Boolean notificar = false;
        Integer posicion = -1;

        for (Resource resource: this.getResponseObjectList()) {

            Order order = (Order) resource;

            Boolean cambioDeEstado = false;
            if(adapterOrders.indexOf(order) != -1){
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
                    notificar = true;
                }
            } else{
                if (!esUnEstadoFinal){
                    adapterOrders.add(posicion, order);
                    this.getOrderAdapter().notifyDataSetChanged();
//                    this.getOrderAdapter().add(order);
                    notificar = true;
                }
            }
        }

        if (notificar){
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
}
