package ar.com.service.tracking.mobile.mobiletrackingservice.model;

import com.gustavofao.jsonapi.Annotations.Type;
import com.gustavofao.jsonapi.Models.Resource;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by miglesias on 09/09/17.
 */

@Type("traces")
public class Trace extends Resource{

    private Date date;
    private double latitude;
    private double longitude;
    private DeliveryMan delivery_man;

    public Trace(Position position, int deliveryManID) {
        this.setLatitude(position.getLatitude());
        this.setLongitude(position.getLongitude());
        this.setDate(GregorianCalendar.getInstance().getTime());
        DeliveryMan deliveryMan = new DeliveryMan();
        deliveryMan.setId(String.valueOf(deliveryManID));
        this.setDelivery_man(deliveryMan);
//        DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
//        Calendar cal = Calendar.getInstance();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public DeliveryMan getDelivery_man() {
        return delivery_man;
    }

    public void setDelivery_man(DeliveryMan delivery_man) {
        this.delivery_man = delivery_man;
    }
}
