package ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice;

import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

/**
 * Created by miglesias on 01/10/17.
 */

public class GoogleDirectionsAPIObserver {

    private static final String TAG = "GoogleDirectionsAPI";

    private GoogleMap map;
//    private List<LatLng> route;
    private Handler handler = new Handler();

    public GoogleDirectionsAPIObserver(GoogleMap map){
        this.map = map;
    }

    public void notify(final List<LatLng> route) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                // Código a ejecutar
                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.color(0x7F00FF00);
                polyOptions.width(15);
                polyOptions.addAll(route);
//                polyOptions.co/
                Polyline polyline = map.addPolyline(polyOptions);

                Log.w(TAG, "Recorrido establecido");
            }
        });

        //                runOnUiThread(new Runnable() {
//                    public void run() {
//                        PolylineOptions polyOptions = new PolylineOptions();
//                        polyOptions.color(Color.BLUE);
//                        polyOptions.width(13);
//                        polyOptions.addAll(route);
//                        Polyline polyline = map.addPolyline(polyOptions);
//
//                        Log.w(TAG, "Recorrido establecido");
//                    }
//                });

    }
}
