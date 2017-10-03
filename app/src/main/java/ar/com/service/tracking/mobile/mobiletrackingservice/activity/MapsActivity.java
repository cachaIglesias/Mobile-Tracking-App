package ar.com.service.tracking.mobile.mobiletrackingservice.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Timer;
import java.util.TimerTask;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;
import ar.com.service.tracking.mobile.mobiletrackingservice.activity.state.MapsActivityState;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice.GPSServiceConnection;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice.GPSservice;
import ar.com.service.tracking.mobile.mobiletrackingservice.endpoint.TrackingServiceConnector;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.PermissionHelper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapsActivity";

    private String permission = Manifest.permission.ACCESS_FINE_LOCATION;

    private static final int ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST = 1;

    private GPSservice mService;
    private boolean mBound = false;

    private GoogleMap map = null;

    private PermissionHelper permissionHelper = new PermissionHelper();

    private final Handler handler = new Handler();

    public static Timer timer;

    private SharedPreferences sharedPref;

    private FusedLocationProviderClient mFusedLocationClient;

    private GPSServiceConnection mConnection;

    private MapsActivityState mapsActivityState;

    /**
     * @method Inicia el ciclo de vida completo de la actividad. En este metodo se debe configurar el estado global de la actividad ya que es el primero en el ciclo de vida de la misma.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setSharedPref( getSharedPreferences("SettingFile", MODE_PRIVATE));

        setmFusedLocationClient( LocationServices.getFusedLocationProviderClient(this));

        String title = "Atencion!";
        String explanationMessage = "Debe aceptar los permisos solicitados para un correcto funcionamiento de la aplicación";
        permissionHelper.verificarSiExistePermisoYSolicitarSiEsNecesario(this, permission, ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST, title, explanationMessage);

        this.setMapsActivityState(MapsActivityState.getInstance(this));

        this.initializeAndConfigureOrderAdapter();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Log.i(TAG, "Inicio de la actividad: " + TAG);

    }

    private void initializeAndConfigureOrderAdapter() {

        // configurar vista de lista vacia
        TextView list_message_text_view = findViewById(R.id.list_menssage);
        list_message_text_view.setText("No tienes una entrega activa");
        list_message_text_view.setPadding(5, 5, 5, 40);

        // configurar lista con adaptador y con vista
        ListView listView = findViewById(R.id.mobile_list);
        listView.setEmptyView(list_message_text_view);
        listView.setAdapter(getMapsActivityState().getOrderAdapter());

    }

    // TODO > ver si sigue aplicando
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {

            case ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO: hacer algo cuando se tienen los permisos
                    Log.i(TAG, "Se tienen los permisos para: " + permission);
                } else {
                    String title = "Atencion!";
                    String explanationMessage = "Debe aceptar los permisos solicitados para un correcto funcionamiento de la aplicación";
                    Log.e(TAG, "No se tienen los permisos para: " + permission);
                    MessageHelper.showOnlyAlert(this, title, explanationMessage);
                }

            }

        }
    }

    /**
     * @method Establece la configuracion inicial del mapa.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        setMap(googleMap);

        // Bind to LocalService
        setmConnection(GPSServiceConnection.getInstance(getMapsActivityState(), getMap(), MapsActivity.this, MapsActivity.this));
        if(getmConnection().ismBound()){
            getmConnection().updateMap(this.getMap());
            this.setmService(getmConnection().getmService());
            Log.w(TAG, "Conexion con Google Play Services Location API reestablecida!");
        }

        if (getMapsActivityState().getRepartidorPolyline() == null) {

            LatLng initialPosition = new LatLng(-34.9212,-57.95562);
            boolean ACCESS_FINE_OK = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (ACCESS_FINE_OK) {

                Task<Location> locationTask = getmFusedLocationClient().getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng initialPosition = new LatLng(location.getLatitude(), location.getLongitude());
                            getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 13));
                            Log.i(TAG, "Se logró obtener la ultima posición conocida");
                        }
                    }
                });

            }

            getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 13));

            // Inicializa el polyline la primera vez
            PolylineOptions newRepartidorPolyline = new PolylineOptions().geodesic(true).visible(true).width(15).color(0x7FFF0000);
            getMapsActivityState().setRepartidorPolyline( newRepartidorPolyline );

        }else{
            // restauro polilyne del recorrido del repatidor
            getMap().addPolyline(getMapsActivityState().getRepartidorPolyline());
        }

        // restauro marcadores
        if(!this.getMapsActivityState().getMarkers().isEmpty()){
            for (MarkerOptions markerOptions: this.getMapsActivityState().getMarkers()) {
                Marker marker = this.getMap().addMarker(markerOptions);
                marker.setTag("");
            }
        }

        // restauro polyline de la entrega
        if( getMapsActivityState().getEntregaPolyline() != null){
            getMap().addPolyline(getMapsActivityState().getEntregaPolyline());
        }

        getMap().getUiSettings().setZoomControlsEnabled(true);
        getMap().setMyLocationEnabled(true);
        getMap().setOnMarkerClickListener(this);

        Log.i(TAG, "Mapa creado y configurado");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
// TODO > aca podria mostrar un mensaje cuando toco cada marcador
        // Retrieve the data from the marker.
//        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
//        marker.setTag(clickCount);
//        Toast.makeText(this, marker.getTitle() + " has been clicked " + clickCount + " times.", Toast.LENGTH_SHORT).show();

        return false;
    }

    /**
     * @method Verifica si la localizacion GPS y por RED este activa y envia un mensaje de alerta en caso de no estarlo
     */
//    private boolean checkLocation() {
//
//
//        if (!isLocationEnabled()) {
//
//            Intent source_location_intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            String titulo = "Habilite geolocalización";
//            String mensaje = "Su ubicación esta desactivada.\npor favor activela.";
//            String intent_mensaje = "Configuración de ubicación";
//
//            MessageHelper.showAlertWithIntent(this, source_location_intent, titulo, mensaje, intent_mensaje);
//
//        }
//        return isLocationEnabled();
//    }


    /**
     * @method Verifica que se encuentre activa la geolocalizacion y solicita activacion, para disparar el servicio background de actualizacion de posiciones gps.
     */
//    public void startLocationUpdates() {
//
//        if (!checkLocation())
//            return;
//
//        //mService.startNetworkUpdates();
////        mService.startGPSUpdates();
//
//    }

    /** TODO >
     * esto deberia hacerse como el boton de retroceso provisto por android.
     * Los valores guardarlos en SharedPreferences, en la key="SettingFile".
     * Los mismo deberian levantarse en el oncreate y estar disponible para todos. */
    /**
     * @method  Dispara la actividad de configuracion
     *
     * @param view
     */
    public void settingsActivity(View view){

        Intent settingsIntent = new Intent(this, SettingsActivity.class);

//        int requestCode = 123;
//      startActivityForResult(settingsIntent, requestCode);
        startActivity(settingsIntent);

    }

//    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data){
//        super.onActivityResult(requestCode, resultCode, data);
//
//        int responseCode = 123;
//
//        if (requestCode == responseCode && resultCode==RESULT_OK) {
//
//            String res = data.getExtras().getString("resultado");
//            String res1 = data.getExtras().getString("resultado1");
//            MessageHelper.toast(this, res + res1, Toast.LENGTH_SHORT);
//
//        }
//
//    }

    public void enviarPosiciones(View view){

        Button button = (Button) view;

        if (button.getText().equals(getResources().getString(R.string.pause))) {

           if( getMapsActivityState().getOrderAdapter().isEmpty() ){
                // detengo el servicio background de actualizacion de posiciones gps
                // TODO > ver que onda este medoto !
                this.getmConnection().stopGPSUpdates();
                // detengo la solucitud de entregas activas cada 1 minuto
                this.cancelarObtencionDeEntregaActivaCadaUnMinuto();

                button.setText(R.string.deliver);
           }else{
                Log.i(TAG, "Existen ordenes sin repartir, por lo que no es posible pausar la entrega");
                MessageHelper.showOnlyAlert(this, "Atención!", "Existen ordenes sin repartir, por lo que no es posible pausar la entrega");
           }

        } else {

            try{
                TrackingServiceConnector.getInstance(MapsActivity.this, this).obtenerEntregaActiva(3, this.getMap(), getMapsActivityState());
            }catch (Exception e){
                Log.e(TAG, "No se pudo recuperar una entrega activa");
                MessageHelper.toast(this, "No se pudo recuperar una entrega activa", Toast.LENGTH_SHORT);
            }

            this.obtenerEntregaActivaCadaUnMinuto();

            // Crea un nuevo polyline por cada recorrido de una entrega activa
            PolylineOptions newRepartidorPolyline = new PolylineOptions().geodesic(true).visible(true).width(15).color(0x7FFF0000);
            getMapsActivityState().setRepartidorPolyline( newRepartidorPolyline );

            getMap().addPolyline(getMapsActivityState().getRepartidorPolyline());

            // Bind to LocalService
            setmConnection(GPSServiceConnection.getInstance(getMapsActivityState(), getMap(), this, this.getApplicationContext()));
            if(!getmConnection().ismBound()){
                Intent intent = new Intent(this, GPSservice.class);
                bindService(intent, getmConnection(), Context.BIND_AUTO_CREATE);
            }

            button.setText(R.string.pause);

        }

    }

    private void obtenerEntregaActivaCadaUnMinuto() {

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getHandler().post(new Runnable() {
                    public void run() {
                        try {
                            //Ejecuta tu AsyncTask!
                            TrackingServiceConnector.getInstance(MapsActivity.this, MapsActivity.this).obtenerEntregaActiva(3, getMap(), getMapsActivityState());
                        } catch (Exception e) {
                            Log.e(TAG, "No se pudo recuperar una entrega activa cada 1 minuto" + e.getMessage());
                            MessageHelper.toast(MapsActivity.this, "No se pudo recuperar una entrega activa, en 1 minuto se volverá a intentar", Toast.LENGTH_LONG);
                        }
                    }
                });
            }
        };

        if (this.getTimer() == null){
            this.setTimer(new Timer());
        }
        else{
            this.getTimer().cancel();
            this.setTimer(new Timer());
        }

        this.getTimer().schedule(task, 60000, 60000);
    }

    private void cancelarObtencionDeEntregaActivaCadaUnMinuto() {

        this.getTimer().cancel();
        Log.w(TAG, "Consulta de entrega activa cada 1 minuto cancelada");

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

        // TODO > ver si hacer esto !!!  Tal vez ahora no tiene sentido
//        if (getmConnection().ismBound()){
//            getmService().unbindService(getmConnection());
//        }
    }

    /**
     * @method Guarda el estado de la UI de la actividad si esta es cerrada por el SO antes de llamar al metodo onStop() o onPause() del ciclo de actividad o si el usuario gira la pantalla ya que en este caso el SO  destruye y vuelve a crear la actividad para que el sistema pueda dibujarla con recursos alternativos que podrian estar disponibles para la nueva configuracion de pantalla.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Button button = findViewById(R.id.deliver_button);
        outState.putCharSequence("buttonState", button.getText());

    }

    /**
     * @method Recupera el estado de la UI de la actividad si esta es cerrada por el SO antes de llamar al metodo onStop() o onPause() del ciclo de actividad o si el usuario gira la pantalla ya que en este caso el SO  destruye y vuelve a crear la actividad para que el sistema pueda dibujarla con recursos alternativos que podrian estar disponibles para la nueva configuracion de pantalla.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        Button button = findViewById(R.id.deliver_button);
        button.setText(savedInstanceState.getCharSequence("buttonState"));

    }

    public Handler getHandler() {
        return handler;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public void setSharedPref(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }

    public FusedLocationProviderClient getmFusedLocationClient() {
        return mFusedLocationClient;
    }

    public void setmFusedLocationClient(FusedLocationProviderClient mFusedLocationClient) {
        this.mFusedLocationClient = mFusedLocationClient;
    }

    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public GPSservice getmService() {
        return mService;
    }

    public void setmService(GPSservice mService) {
        this.mService = mService;
    }

    public boolean ismBound() {
        return mBound;
    }

    public void setmBound(boolean mBound) {
        this.mBound = mBound;
    }

    /** Defines callbacks for service binding, passed to bindService() */
    public GPSServiceConnection getmConnection() {
        return mConnection;
    }

    public void setmConnection(GPSServiceConnection mConnection) {
        this.mConnection = mConnection;
    }

    public MapsActivityState getMapsActivityState() {
        return mapsActivityState;
    }

    public void setMapsActivityState(MapsActivityState mapsActivityState) {
        this.mapsActivityState = mapsActivityState;
    }
}
