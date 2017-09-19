package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.gustavofao.jsonapi.JSONApiConverter;
import com.gustavofao.jsonapi.Models.ErrorModel;
import com.gustavofao.jsonapi.Models.JSONApiObject;
import com.gustavofao.jsonapi.Models.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by miglesias on 15/07/17.
 */
public class ResponseObject implements Callback<JSONApiObject>{

    private static String TAG = "ResponseObject";

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
                // Do something with the errors
                List<ErrorModel> errorList = response.body().getErrors();
                /** Cuando se llama a un servicio que no recibe respuesta, se produce un Error en JSCONApiConverter@103 porque no tiene ninguna json para convertir.
                 por lo que el JSONApiObject del response tiene la marca que tuvo errores pero en el cuerpo no contiene errores. */
                if (errorList != null){
                    Log.e(TAG, "Objeto respuesta con errores: " + response.body().getErrors().toString());
                    MessageHelper.toast(getContext(), "Objeto respuesta con errores: " + response.body().getErrors().toString(), Toast.LENGTH_SHORT);
                }

            } else {

                if (response.body().getData().size() > 0) {

                    Log.i(TAG, "Objeto respuesta con datos: " + response.body().getData().toString());

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

                    Log.w(TAG, "Objeto respuesta sin datos");
                    MessageHelper.toast(getContext(), "Objeto respuesta sin datos", Toast.LENGTH_SHORT);

                }
            }
        } else {

            try {
                // manejar el error
                // JSONApiConverter jsonApiConverter = new JSONApiConverter(Order.class);
                // JSONApiObject object = jsonApiConverter.fromJson(response.errorBody().string());
                // handleErrors(object.getErrors());
                Log.e(TAG, "Objeto respuesta sin cuerpo: " + response.errorBody().string());
                MessageHelper.toast(getContext(), "Objeto respuesta sin cuerpo: " + response.errorBody().string(), Toast.LENGTH_SHORT);
            } catch (IOException e) {
                Log.e(TAG, "Objeto respuesta sin cuerpo");
                MessageHelper.toast(getContext(), "Objeto respuesta sin cuerpo", Toast.LENGTH_SHORT);
            }

        }
    }

    @Override
    public void onFailure(Call<JSONApiObject> call, Throwable t) {
        // handle failure
        Log.e(TAG, "Falla en la conexción con el servicio de posicionamiento. Error: " + t.toString() + " | " + t.getMessage());
        Toast.makeText(getContext(), "Falla en la conexción con el servicio de posicionamiento. Error: " + t.toString(), Toast.LENGTH_SHORT).show();
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
