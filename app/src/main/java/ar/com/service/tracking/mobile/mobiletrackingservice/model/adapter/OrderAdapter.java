package ar.com.service.tracking.mobile.mobiletrackingservice.model.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;

/**
 * Created by miglesias on 14/07/17.
 */

public class OrderAdapter extends ArrayAdapter<Order> {

    public OrderAdapter(Context context, ArrayList<Order> orders) {
        super(context, 0, orders);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Order order = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.orders_view, parent, false);
        }
        // Lookup view for data population
        TextView destinoView = (TextView) convertView.findViewById(R.id.destino);
        TextView destinatarioView = (TextView) convertView.findViewById(R.id.destinatario);
        TextView productoView = (TextView) convertView.findViewById(R.id.producto);
        TextView precioView = (TextView) convertView.findViewById(R.id.precio);
        // Populate the data into the template view using the data object
        destinoView.setText(order.getDireccion());
        destinatarioView.setText( order.getDestinatario());
        productoView.setText(order.getProducto());
        precioView.setText(order.getValor().toString());
        // Return the completed view to render on screen
        return convertView;
    }

}
