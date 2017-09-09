package ar.com.service.tracking.mobile.mobiletrackingservice.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice.GPSServiceConnection;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice.GPSbinder;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice.GPSservice;
import ar.com.service.tracking.mobile.mobiletrackingservice.endpoint.TrackingServiceConnector;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Position;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.adapter.OrderAdapter;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.PermissionHelper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapsActivity";

    private GPSservice mService;
    GPSbinder binder;
    private boolean mBound = false;

    private LocationManager locationManager;
    private PolylineOptions polylineOptions;
    private GoogleMap map = null;

    private static final int ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST = 1;

    private PermissionHelper permissionHelper = new PermissionHelper();

    private String permission = Manifest.permission.ACCESS_FINE_LOCATION;

    private OrderAdapter adapter;

    private final Handler handler = new Handler();

    public static Timer timer;
//    private Timer timer = new Timer();

    private SharedPreferences sharedPref;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    final int REQUEST_CHECK_SETTINGS = 0;

    private List<MarkerOptions> markers = new LinkedList<MarkerOptions>();

    /** Defines callbacks for service binding, passed to bindService() */
//    private ServiceConnection mConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            binder = (GPSbinder) service;
//            mService = binder.getService();
//            mService.setParameters(getPolylineOptions(), getMap(), getMarkers(), MapsActivity.this);
//
//            Log.e(TAG, "Conexion con servicio GPS background establecida");
//            mBound = true;
//            // if (mBound){
//            // siempre agregar esta excepcion que es la unica que tira los serivcios y ocurre cuando se pierde la coneccion: DeadObjectException
//            Log.e(TAG, "Servicio GPS background iniciado");
//            // se llama al servicio de actualizacion de posicones geograficas GPS o GPSA.
//            mService.generateLocationClient();
//            //TODO >
////            startLocationUpdates();
//            // }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            Log.e(TAG, "Conexion con servicio GPS background terminada");
//            mBound = false;
//        }
//    };
    private GPSServiceConnection mConnection ;


    /**
     * @method Inicia el ciclo de vida completo de la actividad. En este metodo se debe configurar el estado global de la actividad ya que es el primero en el ciclo de vida de la misma.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        setSharedPref( getSharedPreferences("SettingFile", MODE_PRIVATE));


        setmFusedLocationClient( LocationServices.getFusedLocationProviderClient(this));

//        mLocationRequest = new LocationRequest();
//
//        Long segundos = Long.valueOf(getSharedPref().getString("minTime", "3").split(" ")[0]);
//        Float metros = Float.valueOf(getSharedPref().getString("minDist", "10").split(" ")[0]);
//
//        mLocationRequest.setInterval( segundos * 1000 );
//        // Google Play Service Location actualiza al menor intervalo definido por alguna aplicacion del equipo, por lo que se tiene que establecer FastestInterval para definir el tiempo minimo de actualizaciones que soporta la aplicacion, para que las actualizaciones con mayer frecuencia de otras apps no afecte el comportamiento de esta.
//        mLocationRequest.setFastestInterval(3000);
//        mLocationRequest.setSmallestDisplacement( metros );
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        String title = "Atencion!";
        String explanationMessage = "Debe aceptar los permisos solicitados para un correcto funcionamiento de la aplicación";
        permissionHelper.verificarSiExistePermisoYSolicitarSiEsNecesario(this, permission, ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST, title, explanationMessage);

//        Intent intent = new Intent(this, GPSservice.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        this.initializeOrderAndConfigureAdapter();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void initializeOrderAndConfigureAdapter() {

        // listView de ordenes
        this.setAdapter(new OrderAdapter(this, new LinkedList<Order>()));

        ListView listView = findViewById(R.id.mobile_list);

        // configurar vista de lista vacia
        TextView list_message_text_view = findViewById(R.id.list_menssage);
        list_message_text_view.setText("No tienes una entrega activa");
        list_message_text_view.setPadding(5, 5, 5, 40);
        listView.setEmptyView(list_message_text_view);

        listView.setAdapter(this.getAdapter());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {

            case ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // TODO: hacer algo cuando se tienen los permisos
                    Log.e(TAG, "Se tienen los permisos para: " + permission);

                } else {

                    String title = "Atencion!";
                    String explanationMessage = "Debe aceptar los permisos solicitados para un correcto funcionamiento de la aplicación";
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

//        if( getMap() == null ) {
            setMap(googleMap);
//        }

        // Bind to LocalService
        mConnection = GPSServiceConnection.getInstance(getPolylineOptions(), getMap(), getMarkers(), MapsActivity.this, MapsActivity.this);
        if(mConnection.ismBound()){
            mConnection.updateMap(this.getMap());
            this.setPolylineOptions(mConnection.getPolylineOptions());
            this.setMarkers(mConnection.getMarkers());
            this.setmService(mConnection.getmService());
            this.setmBound(true);
//            mConnection = GPSServiceConnection.getInstance(getPolylineOptions(), getMap(), getMarkers(), MapsActivity.this, MapsActivity.this);
//            Intent intent = new Intent(this, GPSservice.class);
//            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        if (getPolylineOptions() == null) {

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
                        }
                    }
                });

            }

            getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 13));

            setPolylineOptions(new PolylineOptions().geodesic(true).visible(true).width(8).color(Color.RED));
        }else{
            getMap().addPolyline(getPolylineOptions());
        }

        // TODO>
//        Button button = findViewById(R.id.deliver_button);
//        if(button.getText().equals(getResources().getString(R.string.pause))){
//            this.generateLocationClient();
//        }

        getMap().getUiSettings().setZoomControlsEnabled(true);
        getMap().setMyLocationEnabled(true);
        getMap().setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

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
    private boolean checkLocation() {

        if (!isLocationEnabled()) {

            Intent source_location_intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            String titulo = "Habilite geolocalización";
            String mensaje = "Su ubicación esta desactivada.\npor favor activela.";
            String intent_mensaje = "Configuración de ubicación";

            MessageHelper.showAlertWithIntent(this, source_location_intent, titulo, mensaje, intent_mensaje);

        }
        return isLocationEnabled();
    }

    private boolean isLocationEnabled() {

        // TODO : network provider no se puede probar en el emulador, revisar si se puede debaguear conectando el celular.
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); //|| locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * @method Verifica que se encuentre activa la geolocalizacion y solicita activacion, para disparar el servicio background de actualizacion de posiciones gps.
     */
    public void startLocationUpdates() {

        if (!checkLocation())
            return;

        //mService.startNetworkUpdates();
//        mService.startGPSUpdates();

    }

    /**
     * @method  Dispara la actividad de configuracion
     *
     * @param view
     */
    public void settingsActivity(View view){

        Intent settingsIntent = new Intent(this, SettingsActivity.class);
// TODO > esto deberia hacerse como estaba antes y los valores guardarlos en alguna base de datos. los mismo deberian levantarse en el oncreate y estar disponible para todos.
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

    public void clickear(View view){

        // TODO > TODAVIA NO MANDA EL FORMATO QUE TIENE QUE MANDAR

        ArrayList<Position> positions = new ArrayList<Position>();
        positions.add(new Position(-34.91573983088295, -57.94549774378538));
//        positions.add(new Position(-34.915714812737185, -57.94518744572997));
//        positions.add(new Position(-34.91573240791741, -57.944844122976065));
//        positions.add(new Position(-34.91573240791741, -57.94462954625487));
//        positions.add(new Position(-34.915723610327774, -57.944318410009146));
//        positions.add(new Position(-34.915714812737185, -57.94399654492736));
//        positions.add(new Position(-34.915785193435504, -57.943846341222525));
//        positions.add(new Position(-34.915934752219066, -57.94367467984557));

        TrackingServiceConnector.getInstance(MapsActivity.this).nuevasPosiciones(3, positions);
    }

    public void enviarPosiciones(View view){

        Button button = (Button) view;

        if (button.getText().equals(getResources().getString(R.string.pause))) {

//            mService.stopGPSUpdates();

            // libera el servicio background de actualizacion de posiciones gps
//            if (ismBound()) {
//                unbindService(mConnection);
//                setmBound(false);
//            }
//             TODO > frenar Google Play Services Location
//            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
//            getmService().stopGPSUpdates();

            button.setText(R.string.deliver);

            this.cancelarObtencionDeEntregaActivaCadaUnMinuto();

        } else {

            if(!this.getAdapter().isEmpty()){

                MessageHelper.showOnlyAlert(this, "Atención!", "Existen ordenes sin repartir, por lo que la lista de ordenes no se actualizará");

            }else{

                try{
                    TrackingServiceConnector.getInstance(MapsActivity.this).getEntregaActiva(3, this.getAdapter(), this.getMarkers(), this.getMap(), this.getPolylineOptions());
                }catch (Exception e){
                    Log.e(TAG, "No se pudo recuperar una entrega activa");
                    MessageHelper.toast(this, "No se pudo recuperar una entrega activa", Toast.LENGTH_SHORT);
                }

                this.obtenerEntregaActivaCadaUnMinuto();

            }

            // limpiar pililyne
            setPolylineOptions(new PolylineOptions().geodesic(true).visible(true).width(8).color(Color.RED));
            getMap().addPolyline(getPolylineOptions());

//            // Bind to LocalService
            mConnection = GPSServiceConnection.getInstance(getPolylineOptions(), getMap(), getMarkers(), MapsActivity.this, MapsActivity.this);
            if(!mConnection.ismBound()){
                Intent intent = new Intent(this, GPSservice.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                this.setmBound(true);
            }
//            else{
//                this.setMap(mConnection.getMap());
//                this.setPolylineOptions(mConnection.getPolylineOptions());
//                this.setMarkers(mConnection.getMarkers());
//                this.setmService(mConnection.getmService());
//                this.setmBound(true);
//            }
//            getmService().generateLocationClient();
            // TODO > Iniciar Google Play Services Location
//            this.generateLocationClient();

// TODO > servicio en primer plano, pone en la barra de notificaciones iconito para avisar que se esta realizando el seguimiento gps
//            Notification notification = new Notification(R.drawable.icon, getText(R.string.ticker_text),
//                    System.currentTimeMillis());
//            Intent notificationIntent = new Intent(this, GPSservice.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//            notification.setLatestEventInfo(this, getText(R.string.notification_title),
//                    getText(R.string.notification_message), pendingIntent);
//            int ONGOING_NOTIFICATION_ID = 1;
//            startForeground(ONGOING_NOTIFICATION_ID, notification);

            button.setText(R.string.pause);

        }

    }

//    private void generateLocationClient() {
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
//
//        SettingsClient client = LocationServices.getSettingsClient(this);
//        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
//
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                for (Location location : locationResult.getLocations()) {
//                    // Se actualiza el mapa con las posiciones recibidas
//
//                    double longitudeGPS, latitudeGPS;
//                    LatLng centrar;
//
//                    longitudeGPS = location.getLongitude();
//                    latitudeGPS = location.getLatitude();
//                    centrar = new LatLng(latitudeGPS, longitudeGPS);
//
//                    String zoom = getSharedPref().getString("centerZoom", "1").split(" ")[0];
//
//                    map.clear();
//                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(centrar, 17));
//
//                    polylineOptions.add(new LatLng(latitudeGPS, longitudeGPS));
//                    map.addPolyline(polylineOptions);
//                    for (MarkerOptions markerOptions: getMarkers()) {
//                        Marker marker = getMap().addMarker(markerOptions);
//                        marker.setTag("");
//                    }
//
//                    MessageHelper.toast(MapsActivity.this, String.valueOf(location.getLatitude()), Toast.LENGTH_SHORT );
//                }
//            };
//        };
//
//        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
//            @Override
//            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                // All location settings are satisfied. The client can initialize
//                // location requests here.
//                // ...
//                boolean ACCESS_FINE_OK = ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//
//                if (ACCESS_FINE_OK) {
//
//                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
//                }
//            }
//        });
//
//        task.addOnFailureListener(this, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                int statusCode = ((ApiException) e).getStatusCode();
//                switch (statusCode) {
//                    case CommonStatusCodes.RESOLUTION_REQUIRED:
//                        // Location settings are not satisfied, but this can be fixed
//                        // by showing the user a dialog.
//                        try {
//                            // Show the dialog by calling startResolutionForResult(),
//                            // and check the result in onActivityResult().
//                            ResolvableApiException resolvable = (ResolvableApiException) e;
//                            resolvable.startResolutionForResult(MapsActivity.this,
//                                    REQUEST_CHECK_SETTINGS);
//                        } catch (IntentSender.SendIntentException sendEx) {
//                            // Ignore the error.
//                        }
//                        break;
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                        // Location settings are not satisfied. However, we have no way
//                        // to fix the settings so we won't show the dialog.
//                        break;
//                }
//            }
//        });
//
//    }

    private void cancelarObtencionDeEntregaActivaCadaUnMinuto() {

        this.getTimer().cancel();

    }

    private void obtenerEntregaActivaCadaUnMinuto() {

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getHandler().post(new Runnable() {
                    public void run() {
                        try {
                            //Ejecuta tu AsyncTask!
                            TrackingServiceConnector.getInstance(MapsActivity.this).getEntregaActiva(3, getAdapter(), getMarkers(), getMap(), getPolylineOptions());
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                            MessageHelper.toast(MapsActivity.this, "No se pudo recuperar una entrega activa cada 1 minuto", Toast.LENGTH_SHORT);
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

        // libera el servicio background de actualizacion de posiciones gps
//        if (ismBound()) {
//            unbindService(mConnection);
//            setmBound(false);
//        }
        // TODO > frenar Google Play Services Location
//        if (mLocationCallback != null){
//            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
//        }
    }

    /**
     * @method Guarda el estado de la UI de la actividad si esta es cerrada por el SO antes de llamar al metodo onStop() o onPause() del ciclo de actividad o si el usuario gira la pantalla ya que en este caso el SO  destruye y vuelve a crear la actividad para que el sistema pueda dibujarla con recursos alternativos que podrian estar disponibles para la nueva configuracion de pantalla.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("polylineOptions", getPolylineOptions());

        Button button = findViewById(R.id.deliver_button);
        outState.putCharSequence("buttonState", button.getText());

        // TODO > guardar lista de ordenes, markers
// para las cosas que se complican como el adaptador de ordenes y los markers, debo hacer un singleton que guarde el estado de la actividad para consultarlo cuando esta retorne.

    }

    /**
     * @method Recupera el estado de la UI de la actividad si esta es cerrada por el SO antes de llamar al metodo onStop() o onPause() del ciclo de actividad o si el usuario gira la pantalla ya que en este caso el SO  destruye y vuelve a crear la actividad para que el sistema pueda dibujarla con recursos alternativos que podrian estar disponibles para la nueva configuracion de pantalla.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        setPolylineOptions((PolylineOptions) savedInstanceState.getParcelable("polylineOptions"));

        Button button = findViewById(R.id.deliver_button);
        button.setText(savedInstanceState.getCharSequence("buttonState"));

        // TODO > guardar lista de ordenes, markers

    }


    public OrderAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(OrderAdapter adapter) {
        this.adapter = adapter;
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


    public List<MarkerOptions> getMarkers() {
        return markers;
    }

    public void setMarkers(List<MarkerOptions> markers) {
        this.markers = markers;
    }


    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }


    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public void setPolylineOptions(PolylineOptions polylineOptions) {
        this.polylineOptions = polylineOptions;
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
}
