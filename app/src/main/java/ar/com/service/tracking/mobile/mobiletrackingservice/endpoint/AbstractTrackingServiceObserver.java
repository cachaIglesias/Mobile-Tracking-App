package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint;

import com.gustavofao.jsonapi.Models.Resource;

import java.util.List;

/**
 * Created by miglesias on 06/08/17.
 */

public abstract class AbstractTrackingServiceObserver {

    private List<Resource> responseObjectList;


    public List<Resource> getResponseObjectList() {
        return responseObjectList;
    }

    public void setResponseObjectList(List<Resource> responseObjectList) {
        this.responseObjectList = responseObjectList;
    }

    public abstract void update();

}
