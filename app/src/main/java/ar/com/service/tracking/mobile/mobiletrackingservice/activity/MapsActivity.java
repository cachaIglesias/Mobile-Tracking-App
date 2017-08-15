package ar.com.service.tracking.mobile.mobiletrackingservice.activity;

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
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import ar.com.service.tracking.mobile.mobiletrackingservice.endpoint.TrackingServiceConnector;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Position;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.PermissionHelper;
import ar.com.service.tracking.mobile.mobiletrackingservice.R;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice.GPSbinder;
import ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice.GPSservice;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.Order;
import ar.com.service.tracking.mobile.mobiletrackingservice.model.adapter.OrderAdapter;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    GPSservice mService;
    boolean mBound = false;

    private LocationManager locationManager;
    private PolylineOptions polylineOptions;
    private GoogleMap map;

    private static final int ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST = 1;

    private PermissionHelper permissionHelper = new PermissionHelper();

    private String permission = Manifest.permission.ACCESS_FINE_LOCATION;

    private ArrayList<Order> orders;
    private OrderAdapter adapter;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GPSbinder binder = (GPSbinder) service;
            mService = binder.getService();
            mService.setParameters(locationManager, polylineOptions, map);

            Log.e(TAG, "Conexion con servicio GPS background establecida");
            mBound = true;
            // if (mBound){
            // siempre agregar esta excepcion que es la unica que tira los serivcios y ocurre cuando se pierde la coneccion: DeadObjectException
            Log.e(TAG, "Servicio GPS background iniciado");
            // se llama al servicio de actualizacion de posicones geograficas GPS o GPSA.
            startLocationUpdates();
            // }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Conexion con servicio GPS background terminada");
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

        String title = "Atencion!";
        String explanationMessage = "Debe aceptar los permisos solicitados para un correcto funcionamiento de la aplicación";
        permissionHelper.verificarSiExistePermisoYSolicitarSiEsNecesario(this, permission, ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST, title, explanationMessage);



        // ######################################################################### //
        // Lista de ordenes, se deberian mostrar cuando se recuperar desde el endpoint
        this.setOrders(new ArrayList<Order>());
        this.getOrders().add(new Order("No hay entregas pendientes","","",null));
//        this.getOrders().add(new Order("plaza paso","luz","termo",Float.valueOf(20)));
//        this.getOrders().add(new Order("plaza italia","agos","factura",Float.valueOf(30)));
//        this.getOrders().add(new Order("plaza rocha","anto","pastel",Float.valueOf(40)));

        // listView de ordenes
        this.setAdapter(new OrderAdapter(this, this.getOrders()));

        ListView listView = findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);

//        adapter.add(new Order("plaza san martin","hugo","borratinta",Float.valueOf(50)));
       //  ######################################################################### //



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        map = googleMap;

        if(polylineOptions == null) {
            // TODO: aca deberia obtener la ultima posicion GPS conocida para centrar el mapa ahi.
            LatLng laPlata = new LatLng(-34.9212,-57.95562);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(laPlata, 13));

            polylineOptions = new PolylineOptions().geodesic(true).visible(true).width(8).color(Color.RED);
        }else{
            map.addPolyline(polylineOptions);
        }

        map.getUiSettings().setZoomControlsEnabled(true);

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
        mService.startGPSUpdates();

    }

    /**
     * @method  Dispara la actividad de configuracion
     *
     * @param view
     */
    public void settingsActivity(View view){

        Intent settingsIntent = new Intent(this, SettingsActivity.class);

        int requestCode = 123;
      startActivityForResult(settingsIntent, requestCode);
//        startActivity(settingsIntent);

    }

    @Override protected void onActivityResult (int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        int responseCode = 123;

        if (requestCode == responseCode && resultCode==RESULT_OK) {

            String res = data.getExtras().getString("resultado");
            String res1 = data.getExtras().getString("resultado1");
            MessageHelper.toast(this, res + res1, Toast.LENGTH_SHORT);

        }

    }

    public void clickear(View view){

//        TrackingServiceConnector.getInstance(MapsActivity.this).getEntregaActiva(3, this.getAdapter());

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

            mService.stopGPSUpdates();

            // libera el servicio background de actualizacion de posiciones gps
            if (mBound) {
                unbindService(mConnection);
                mBound = false;
            }

            button.setText(R.string.deliver);

        } else {

            TrackingServiceConnector.getInstance(MapsActivity.this).getEntregaActiva(3, this.getAdapter());

            // Bind to LocalService
            Intent intent = new Intent(this, GPSservice.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            button.setText(R.string.pause);

        }

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

        outState.putParcelable("polylineOptions",polylineOptions);

    }

    /**
     * @method Recupera el estado de la UI de la actividad si esta es cerrada por el SO antes de llamar al metodo onStop() o onPause() del ciclo de actividad o si el usuario gira la pantalla ya que en este caso el SO  destruye y vuelve a crear la actividad para que el sistema pueda dibujarla con recursos alternativos que podrian estar disponibles para la nueva configuracion de pantalla.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        polylineOptions = savedInstanceState.getParcelable("polylineOptions");

    }


    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }


    public OrderAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(OrderAdapter adapter) {
        this.adapter = adapter;
    }
}
