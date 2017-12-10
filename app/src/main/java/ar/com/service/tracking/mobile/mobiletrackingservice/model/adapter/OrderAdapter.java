package ar.com.service.tracking.mobile.mobiletrackingservice.model.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.LinkedList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;
import ar.com.service.tracking.mobile.mobiletrackingservice.activity.state.MapsActivityState;
import ar.com.service.tracking.mobile.mobiletrackingservice.endpoint.trackingService.TrackingServiceConnector;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

/**
 * Created by miglesias on 14/07/17.
 */

public class OrderAdapter extends ArrayAdapter<Order> {

    private List<Order> orders;

    private MapsActivityState mapsActivityState;

    public OrderAdapter(Context context, LinkedList<Order> orders, MapsActivityState mapsActivityState) {
        super(context, 0, orders);
        this.setOrders(orders);
        this.setMapsActivityState(mapsActivityState);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Order order = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.orders_view, parent, false);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                MessageHelper.toast(getContext(), "seleccione la orden numero: " + position, Toast.LENGTH_SHORT);

                final TrackingServiceConnector instancia = TrackingServiceConnector.getInstance(getContext(), null);

                String title = "Llegaste a destino !?";
                String message = "Pudiste entregar la orden de " + order.printOrdered_products() + "a " + order.getDestinatario();

                final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());

                dialog.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("Si, finalizar orden", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                instancia.marcarComoFinalizado(Integer.parseInt(order.getId()));
                                MessageHelper.toast(getContext(), "Orden finalizada", Toast.LENGTH_SHORT);
                                view.setBackgroundColor(0x7F00FF00);
                                view.setEnabled(false);
                                view.getNextFocusDownId();
                                getMapsActivityState().getMarkers().get(position+1).icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_finalized));
                            }
                        })
                        .setNegativeButton("No, cancelar orden", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                instancia.marcarComoCancelado(Integer.parseInt(order.getId()));
                                MessageHelper.toast(getContext(), "Orden canncelada", Toast.LENGTH_SHORT);
                                view.setBackgroundColor(0x7FFF0000);
                                view.setEnabled(false);
                                view.getNextFocusDownId();
                                getMapsActivityState().getMarkers().get(position+1).icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_discarted));
                                //TOOO > se deberia recalcular recorrido???? creeeeo que aca no porque no es un destino que se deberia saltear ya que el repartidor se encuentra en el mismo.
                            }
                        }).setNeutralButton("Regresar al mapa", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MessageHelper.toast(getContext(), "No se realizó ninguna operación", Toast.LENGTH_SHORT);
                            }
                });

                dialog.show();

            }
        });

        // Lookup view for data population
        TextView destinoView = (TextView) convertView.findViewById(R.id.destino);
        TextView destinatarioView = (TextView) convertView.findViewById(R.id.destinatario);
        TextView productoView = (TextView) convertView.findViewById(R.id.producto);
        TextView precioView = (TextView) convertView.findViewById(R.id.precio);
        // Populate the data into the template view using the data object
        destinoView.setText(order.getAddress());
        destinatarioView.setText( order.getDestinatario());
        productoView.setText(order.printOrdered_products());
        if(order.getValor() != null){
            precioView.setText(order.getValor().toString());
        }
        // Return the completed view to render on screen
        return convertView;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Order> getSendedOrders(){

        List<Order> sendedOrders = new LinkedList<Order>();

        for (Order order: this.getOrders()) {
            if(order.getStatus().compareTo("sended") == 0){
                sendedOrders.add(order);
            }
        }

        return sendedOrders;
    }

    public Boolean hasSendedOrders(){

        Boolean result = false;

        for (Order order: this.getOrders()) {
            if(order.getStatus().compareTo("sended") == 0){
                result = true;
            }
        }

        return result;
    }


    public MapsActivityState getMapsActivityState() {
        return mapsActivityState;
    }

    public void setMapsActivityState(MapsActivityState mapsActivityState) {
        this.mapsActivityState = mapsActivityState;
    }
}
