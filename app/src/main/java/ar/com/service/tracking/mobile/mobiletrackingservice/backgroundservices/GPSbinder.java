package ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservices;

import android.os.Binder;

/**
 * Created by miglesias on 06/07/17.
 */

public class GPSbinder extends Binder {

    GPSservice service = null ;

    public GPSbinder(GPSservice gpSservice){
        service = gpSservice;
    }

    public GPSservice getService() {
        // Return this instance of LocalService so clients can call public methods
        return service;
    }

}
