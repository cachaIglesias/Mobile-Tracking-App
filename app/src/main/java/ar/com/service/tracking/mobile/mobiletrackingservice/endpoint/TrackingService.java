package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint;

import com.gustavofao.jsonapi.Models.JSONApiObject;
import com.gustavofao.jsonapi.Retrofit.JSONConverterFactory;

import ar.com.service.tracking.mobile.mobiletrackingservice.model.Business;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Delivery;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.DeliveryMan;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Trace;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Position;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by miglesias on 15/07/17.
 */

public interface TrackingService {

    Class[] classes = { Business.class, Delivery.class, DeliveryMan.class, Order.class, Position.class, Trace.class };

    // mascara que identifica a LocalHost en Android > "http://10.0.2.2:3000/"
    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000")
            .addConverterFactory(JSONConverterFactory.create(classes))
            .build();

    @POST("api/orders/{id}/mark_as_finalized")
    Call<JSONApiObject> marcarComoFinalizado(@Path("id") int orderID);

    @POST("api/orders/{id}/mark_as_canceled")
    Call<JSONApiObject> marcarComoCancelado(@Path("id") int orderID);

    @GET("/api/delivery_men/{id}/active_delivery_orders")
    Call<JSONApiObject> getEntregaActiva(@Path("id") int deliveryMenID);

//    @POST("/api/delivery_men/{id}/new_positions")
//    Call<JSONApiObject> nuevasPosiciones(@Path("id") int IDdeliveryMen, @Body List<Position> positions, int deliveryMenID);

    // TODO > este metodo envia algo que no respeta json api porque el servicio no respeta json api
//    @FormUrlEncoded
    @POST("/api/traces")
    Call<JSONApiObject> nuevasPosiciones(@Body Trace trace);

}
