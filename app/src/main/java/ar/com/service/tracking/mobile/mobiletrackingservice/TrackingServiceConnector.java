package ar.com.service.tracking.mobile.mobiletrackingservice;

import android.content.Context;
import android.widget.Toast;

import com.gustavofao.jsonapi.JSONApiConverter;
import com.gustavofao.jsonapi.Models.ErrorModel;
import com.gustavofao.jsonapi.Models.JSONApiObject;
import com.gustavofao.jsonapi.Models.Resource;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by miglesias on 16/07/17.
 */

public class TrackingServiceConnector {

    private static TrackingServiceConnector instance = null;
    private Context lastContext = null;
    private Call<JSONApiObject> call;
    private TrackingService service;

    protected TrackingServiceConnector() {
        // Exists only to defeat instantiation.
    }

    public static TrackingServiceConnector getInstance(Context context) {

        if(instance == null) {
            instance = new TrackingServiceConnector();
            instance.configurar();
        }
        instance.setLastContext(context);
        return instance;

    }

    private void configurar() {

        setService(TrackingService.retrofit.create(TrackingService.class));

    }

    public void getMethod(){

//        new Thread(new Runnable() {
//            public void run() {
//
//            }
//        }).start();

        setCall(getService().getMethod());
        getCall().enqueue(new Callback<JSONApiObject>() {

            @Override
            public void onResponse(Call<JSONApiObject> call, Response<JSONApiObject> response) {
                // handle success
                if (response.body() != null) {
                    if (response.body().hasErrors()) {
                        List<ErrorModel> errorList = response.body().getErrors();
                        //Do something with the errors
                    } else {
                        Toast.makeText(getLastContext(), response.body().getData().toString(), Toast.LENGTH_LONG).show();
                        if (response.body().getData().size() > 0) {
                            Toast.makeText(getLastContext(), "Object With data", Toast.LENGTH_SHORT).show();
                            if (response.body().getData().size() == 1) {
                                //Single Object
                                ObjetoRespuesta article = (ObjetoRespuesta) response.body().getData(0);
                            } else {
                                //List of Objects
                                List<Resource> resources = response.body().getData();
                            }
                        } else {
                            Toast.makeText(getLastContext(), "No Items", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    try {
                        JSONApiConverter jsonApiConverter = new JSONApiConverter(ObjetoRespuesta.class);
                        JSONApiObject object = jsonApiConverter.fromJson(response.errorBody().string());
//                        manejar el error
//                        handleErrors(object.getErrors());
                    } catch (IOException e) {
                        Toast.makeText(getLastContext(), "Empty Body", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JSONApiObject> call, Throwable t) {
                // handle failure
                Toast.makeText(getLastContext(), "Falla en la conexcion con el servicio de posicionamiento. " + "error: " + t.toString(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    public Context getLastContext() {
        return lastContext;
    }

    public void setLastContext(Context lastContext) {
        this.lastContext = lastContext;
    }

    public Call<JSONApiObject> getCall() {
        return call;
    }

    public void setCall(Call<JSONApiObject> call) {
        this.call = call;
    }

    public TrackingService getService() {
        return service;
    }

    public void setService(TrackingService service) {
        this.service = service;
    }
}
