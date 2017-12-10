package ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservices.directions;

import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.activity.state.MapsActivityState;

/**
 * Created by miglesias on 01/10/17.
 */

public class GoogleDirectionsAPIObserver {

    private static final String TAG = "GoogleDirectionsAPI";

    private GoogleMap map;

    private MapsActivityState mapsActivityState;

    private Handler handler = new Handler();

    public GoogleDirectionsAPIObserver(MapsActivityState mapsActivityState){
//        this.setMap(map);
        this.setMapsActivityState(mapsActivityState);
    }

    public void notify(final List<LatLng> route) {

        getHandler().post(new Runnable() {
            @Override
            public void run() {
                // Código a ejecutar
                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.color(0x7F00FF00);
                polyOptions.width(15);
                polyOptions.addAll(route);
                getMapsActivityState().setEntregaPolyline( polyOptions );
                getMapsActivityState().refreshMap();
//                getMap().addPolyline(getMapsActivityState().getEntregaPolyline());
                // TODO > que es esta latlang ? sera la del local de repartos ?
                //polyline.getPoints().add(new LatLng(-34.934428, -57.963613));

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


//    public GoogleMap getMap() {
//        return map;
//    }
//
//    public void setMap(GoogleMap map) {
//        this.map = map;
//    }

    public MapsActivityState getMapsActivityState() {
        return mapsActivityState;
    }

    public void setMapsActivityState(MapsActivityState mapsActivityState) {
        this.mapsActivityState = mapsActivityState;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
