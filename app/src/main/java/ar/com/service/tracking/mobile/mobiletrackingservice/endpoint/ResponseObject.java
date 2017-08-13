package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint;

import android.content.Context;
import android.widget.Toast;

import com.gustavofao.jsonapi.JSONApiConverter;
import com.gustavofao.jsonapi.Models.ErrorModel;
import com.gustavofao.jsonapi.Models.JSONApiObject;
import com.gustavofao.jsonapi.Models.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by miglesias on 15/07/17.
 */
public class ResponseObject implements Callback<JSONApiObject>{

    private Context context;
    private AbstractTrackingServiceObserver observer;
    private ArrayList<Resource> responseObjectList;

    public ResponseObject(Context lastContext){
        this.setResponseObjectList(new ArrayList<Resource>());
        this.setContext(lastContext);
    }

    public ResponseObject(Context lastContext, AbstractTrackingServiceObserver observer){
        this.setResponseObjectList(new ArrayList<Resource>());
        this.setContext(lastContext);
        this.setObserver(observer);
    }

    @Override
    public void onResponse(Call<JSONApiObject> call, Response<JSONApiObject> response) {
        // handle success
        if (response.body() != null) {
            if (response.body().hasErrors()) {
                //Do something with the errors
                List<ErrorModel> errorList = response.body().getErrors();
            } else {
                Toast.makeText(getContext(), response.body().getData().toString(), Toast.LENGTH_LONG).show();
                if (response.body().getData().size() > 0) {
                    Toast.makeText(getContext(), "Object With data", Toast.LENGTH_SHORT).show();
                    if (response.body().getData().size() == 1) {
                        //Single Object
                        this.getResponseObjectList().add(response.body().getData(0));
                    } else {
                        //List of Objects
                        for (Resource resource: response.body().getData()) {
                            this.getResponseObjectList().add(resource);
                        }
                    }
                    this.notifyObserver();
                } else {
                    Toast.makeText(getContext(), "No Items", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            try {
                // manejar el error
                // JSONApiConverter jsonApiConverter = new JSONApiConverter(Order.class);
                // JSONApiObject object = jsonApiConverter.fromJson(response.errorBody().string());
                // handleErrors(object.getErrors());
                response.errorBody().string();
                Toast.makeText(getContext(), "Error Body", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Empty Body", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onFailure(Call<JSONApiObject> call, Throwable t) {
        // handle failure
        Toast.makeText(getContext(), "Falla en la conexción con el servicio de posicionamiento. " + "error: " + t.toString(), Toast.LENGTH_SHORT).show();
    }

    private void notifyObserver() {
        if(this.getObserver() != null) {
            this.getObserver().setResponseObjectList(this.getResponseObjectList());
            this.getObserver().update();
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    public ArrayList<Resource> getResponseObjectList() {
        return responseObjectList;
    }

    public void setResponseObjectList(ArrayList<Resource> responseObjectList) {
        this.responseObjectList = responseObjectList;
    }

    public AbstractTrackingServiceObserver getObserver() {
        return observer;
    }

    public void setObserver(AbstractTrackingServiceObserver observer) {
        this.observer = observer;
    }
}
