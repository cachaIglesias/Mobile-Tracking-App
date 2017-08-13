package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint;

import com.gustavofao.jsonapi.Models.Resource;

import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.adapter.OrderAdapter;

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
        for (Resource resource: this.getResponseObjectList()) {
            this.getOrderAdapter().add((Order) resource);
        }
    }


    public OrderAdapter getOrderAdapter() {
        return orderAdapter;
    }

    public void setOrderAdapter(OrderAdapter orderAdapter) {
        this.orderAdapter = orderAdapter;
    }
}
