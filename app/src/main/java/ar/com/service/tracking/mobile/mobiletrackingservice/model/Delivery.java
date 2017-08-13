package ar.com.service.tracking.mobile.mobiletrackingservice.model;

import com.gustavofao.jsonapi.Annotations.Excluded;
import com.gustavofao.jsonapi.Annotations.Type;
import com.gustavofao.jsonapi.Models.Resource;

import java.util.Date;

/**
 * Created by miglesias on 01/08/17.
 */
@Type("deliveries")
public class Delivery extends Resource {

    private Date start_date;
    private Date end_date;
    @Excluded
    private DeliveryMan deliveryMan; // "delivery_man_id": 3

    public Delivery(){}

    public Date getStart_date() {
        return start_date;
    }

    public void setStart_date(Date start_date) {
        this.start_date = start_date;
    }

    public Date getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }

    public DeliveryMan getDeliveryMan() {
        return deliveryMan;
    }

    public void setDeliveryMan(DeliveryMan deliveryMan) {
        this.deliveryMan = deliveryMan;
    }
}
