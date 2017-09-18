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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;
import ar.com.service.tracking.mobile.mobiletrackingservice.activity.MapsActivity;
import ar.com.service.tracking.mobile.mobiletrackingservice.endpoint.TrackingServiceConnector;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

/**
 * Created by miglesias on 14/07/17.
 */

public class OrderAdapter extends ArrayAdapter<Order> {

    private List<Order> orders;

    public OrderAdapter(Context context, LinkedList<Order> orders) {
        super(context, 0, orders);
        this.setOrders(orders);
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
            public void onClick(View view) {

                MessageHelper.toast(getContext(), "seleccione la orden numero: " + position, Toast.LENGTH_SHORT);

                final TrackingServiceConnector instancia = TrackingServiceConnector.getInstance(getContext(), null);

                String title = "Llegaste a destino !?";
                String message = "Pudiste entregar la orden de " + order.getProducto() + "a " + order.getDestinatario();

                final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());

                dialog.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("Si, finalizar orden", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                instancia.marcarComoFinalizado(Integer.parseInt(order.getId()));
                                MessageHelper.toast(getContext(), "Orden finalizada", Toast.LENGTH_SHORT);
                            }
                        })
                        .setNegativeButton("No, cancelar orden", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                instancia.marcarComoCancelado(Integer.parseInt(order.getId()));
                                MessageHelper.toast(getContext(), "Orden canncelada", Toast.LENGTH_SHORT);
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
        destinoView.setText(order.getDireccion());
        destinatarioView.setText( order.getDestinatario());
        productoView.setText(order.getProducto());
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

}
