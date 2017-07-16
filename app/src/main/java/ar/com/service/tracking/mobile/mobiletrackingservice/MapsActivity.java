package ar.com.service.tracking.mobile.mobiletrackingservice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.gustavofao.jsonapi.JSONApiConverter;
import com.gustavofao.jsonapi.Models.ErrorModel;
import com.gustavofao.jsonapi.Models.JSONApiObject;
import com.gustavofao.jsonapi.Models.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

//    Call<JSONApiObject> call;

    private static final String TAG = "MapsActivity";

    GPSservice mService;
    boolean mBound = false;

    private LocationManager locationManager;
    private PolylineOptions polylineOptions;
    private GoogleMap map;

    private static final int ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST = 1;

    private PermissionHelper permissionHelper = new PermissionHelper();

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GPSbinder binder = (GPSbinder) service;
            mService = binder.getService();
            mBound = true;
//            if (mBound){
                // siempre agregar esta excepcion que es la unica que tira los serivcios y ocurre cuando se pierde la coneccion: DeadObjectException
                mService.setParameters(locationManager, polylineOptions, map);
                toggleLocationUpdates();
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    /**
     * @method Inicia el ciclo de vida completo de la actividad. En este metodo se debe configurar el estado global de la actividad ya que es el primero en el ciclo de vida de la misma.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        String title = "Atencion!";
        String explanationMessage = "Debe aceptar los permisos solicitados para un correcto funcionamiento de la aplicación";
        permissionHelper.verificarSiExistePermisoYSolicitarSiEsNecesario(this, permission, ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST, title, explanationMessage);

        ArrayList<Order> orders = new ArrayList<Order>();
        orders.add(new Order("Diagonal 74","Pepe","Piza",new Float(10)));
        orders.add(new Order("plaza paso","luz","termo",new Float(20)));
        orders.add(new Order("plaza italia","agos","factura",new Float(30)));
        orders.add(new Order("plaza rocha","anto","pastel",new Float(40)));

        // listView de ordenes
        OrderAdapter adapter = new OrderAdapter(this, orders);

        ListView listView = findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);

        adapter.add(new Order("plaza san martin","hugo","borratinta",new Float(50)));

        // TODO: en algun lado llamar al servicio de actualizacion de posiciones.
        // Bind to LocalService
        Intent intent = new Intent(this, GPSservice.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        android.os.Debug.waitForDebugger();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {

            case ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // TODO: hacer algo cuando se tienen los permisos


                } else {

                    String title = "Atencion!";
                    String explanationMessage = "Debe aceptar los permisos solicitados para un correcto funcionamiento de la aplicación";
                    MessageHelper.showOnlyAlert(this, title, explanationMessage);
                }

            }

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        LatLng laPlata = new LatLng(-34.9212,-57.95562);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(laPlata, 13));

        polylineOptions = new PolylineOptions().geodesic(true).visible(true).width(8).color(Color.RED);

        map.getUiSettings().setZoomControlsEnabled(true);
//        map.getUiSettings().setCompassEnabled(true);
//        map.getUiSettings().setMyLocationButtonEnabled(true);

//        TrackingService service = TrackingService.retrofit.create(TrackingService.class);
//        call = service.getMethod();
//
//        call.enqueue(new Callback<JSONApiObject>() {
//
//            @Override
//            public void onResponse(Call<JSONApiObject> call, Response<JSONApiObject> response) {
//                // handle success
//                if (response.body() != null) {
//                    if (response.body().hasErrors()) {
//                        List<ErrorModel> errorList = response.body().getErrors();
//                        //Do something with the errors
//                    } else {
//                        Toast.makeText(MapsActivity.this, response.body().getData().toString(), Toast.LENGTH_LONG).show();
//                        if (response.body().getData().size() > 0) {
//                            Toast.makeText(MapsActivity.this, "Object With data", Toast.LENGTH_SHORT).show();
//                            if (response.body().getData().size() == 1) {
//                                //Single Object
//                                ObjetoRespuesta article = (ObjetoRespuesta) response.body().getData(0);
//                            } else {
//                                //List of Objects
//                                List<Resource> resources = response.body().getData();
//                            }
//                        } else {
//                            Toast.makeText(MapsActivity.this, "No Items", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                } else {
//                    try {
//                        JSONApiConverter jsonApiConverter = new JSONApiConverter(ObjetoRespuesta.class);
//                        JSONApiObject object = jsonApiConverter.fromJson(response.errorBody().string());
////                        manejar el error
////                        handleErrors(object.getErrors());
//                    } catch (IOException e) {
//                        Toast.makeText(MapsActivity.this, "Empty Body", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<JSONApiObject> call, Throwable t) {
//                // handle failure
//                Toast.makeText(MapsActivity.this, "Falla en la conexcion con el servicio de posicionamiento. " + "error: " + t.toString(), Toast.LENGTH_SHORT).show();
//            }
//
//        });

    }

    /**
     * @method Verifica si la localizacion GPS y por RED este activa y envia un mensaje de alerta en caso de no estarlo
     */
    private boolean checkLocation() {
        if (!isLocationEnabled()) {

            Intent source_location_intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            String titulo = "Habilite geolocalización";
            String mensaje = "Su ubicación esta desactivada.\npor favor active su ubicación";
            String intent_mensaje = "Configuración de ubicación";

            MessageHelper.showAlertWithIntent(this, source_location_intent, titulo, mensaje, intent_mensaje);

        }
        return isLocationEnabled();
    }

    private boolean isLocationEnabled() {

        // TODO : network provider no se puede probar en el emulador, revisar si se puede debaguear conectando el celular.
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); //|| locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void toggleLocationUpdates() {

        if (!checkLocation())
            return;

//        mService.toggleNetworkUpdates();
        mService.toggleGPSUpdates();

    }

    public void settingsActivity(View view){
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * @method Inicia el ciclo de vida visible de la actividad
     */
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
    }

    /**
     * @method Inicia el ciclo de vida en primer plano de la actividad
     */
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").

    }

    /**
     * @method Finaliza el ciclo de vida en primer plano de la actividad. Cuando el SO necesita liberar recursos, debido a que onPause() es el primero de los tres (onPause(), onStop() y onDestroy()), una vez que se crea la actividad, onPause() es el último método al que se llamará para que se pueda finalizar el proceso; si el sistema debe recuperar memoria en una emergencia, es posible que no se llame a onStop() ni a onDestroy(). Por lo tanto, debes usar onPause() para escribir datos persistentes fundamentales (como realizar en envio al servicio para que notifique las posiciones registradas).
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }

    /**
     * @method Finaliza el ciclo de vida visible de la actividad
     */
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }

    /**
     * @method Finaliza el ciclo de vida completo de la actividad. En este metodo se debe liberar todos los recursos de la actividad ya que es el ultimo en el ciclo de vida de la misma.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: va a servir para liberar el servicio de actualizacion de posiciones.
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * @method Guarda el estado de la UI de la actividad si esta es cerrada por el SO antes de llamar al metodo onStop() o onPause() del ciclo de actividad o si el usuario gira la pantalla ya que en este caso el SO  destruye y vuelve a crear la actividad para que el sistema pueda dibujarla con recursos alternativos que podrian estar disponibles para la nueva configuracion de pantalla.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        outState.putSerializable("map", map);

    }

    /**
     * @method Recupera el estado de la UI de la actividad si esta es cerrada por el SO antes de llamar al metodo onStop() o onPause() del ciclo de actividad o si el usuario gira la pantalla ya que en este caso el SO  destruye y vuelve a crear la actividad para que el sistema pueda dibujarla con recursos alternativos que podrian estar disponibles para la nueva configuracion de pantalla.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

//        savedInstanceState.getSerializable("map");

    }

}
