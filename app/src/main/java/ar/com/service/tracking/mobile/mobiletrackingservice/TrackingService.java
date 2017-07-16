package ar.com.service.tracking.mobile.mobiletrackingservice;

import com.gustavofao.jsonapi.Models.JSONApiObject;
import com.gustavofao.jsonapi.Retrofit.JSONConverterFactory;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by miglesias on 15/07/17.
 */

public interface TrackingService {

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .addConverterFactory(JSONConverterFactory.create(ObjetoRespuesta.class))
            .build();

    @GET("users/{user}/followers")
    Call<List<String>> listRepos(@Path("user") String user);

    @GET("posts/1")
    Call<JSONApiObject> getMethod();

}
