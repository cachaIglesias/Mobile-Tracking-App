package ar.com.service.tracking.mobile.mobiletrackingservice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private double longitudeNetwork, latitudeNetwork;
    private LocationManager locationManager;
    private GoogleMap map;
    private PolylineOptions polylineOptions;

    private static final int ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST = 1;

    private PermissionHelper permissionHelper = new PermissionHelper();

    // Array of strings...
    private String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","Windows7","Max OS X"};

//    String[] fromColumns = {"", ""};

//    int[] toViews = {R.id.display_name, R.id.phone_number};

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

        // TODO: en algun lado llamar al servicio de actualizacion de posiciones.

        // listView de ordenes
        ArrayAdapter adapter = new ArrayAdapter<>(this,
                R.layout.orders_view, mobileArray);

        ListView listView = findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        polylineOptions = new PolylineOptions().geodesic(true).visible(true).width(4).color(Color.RED);

        this.toggleNetworkUpdates();

        // SE AGREGA UN MARCADOR
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // EFECTO DE ROTACION
        // Flat markers will rotate when the map is rotated,
        // and change perspective when the map is tilted.
//        map.addMarker(new MarkerOptions()
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.direction_arrow))
//                .position(mapCenter)
//                .flat(true)
//                .rotation(245));
//
//        CameraPosition cameraPosition = CameraPosition.builder()
//                .target(mapCenter)
//                .zoom(13)
//                .bearing(90)
//                .build();
//
//        // Animate the change in camera view over 2 seconds
//        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
//                2000, null);


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
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void toggleNetworkUpdates() {

        if (!checkLocation())
            return;

        boolean ACCESS_FINE_OK = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (ACCESS_FINE_OK) {

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListenerNetwork);
            Toast.makeText(this, "Network provider started running", Toast.LENGTH_LONG).show();

        }

    }

    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeNetwork = location.getLongitude();
            latitudeNetwork = location.getLatitude();

            LatLng centrar = new LatLng(latitudeNetwork, longitudeNetwork);
            map.addMarker(new MarkerOptions().position(centrar).title("Yo"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(centrar, 18));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // se agrega al polyline la nuevo posicion
                    polylineOptions.add(new LatLng(latitudeNetwork, longitudeNetwork));
                    map.addPolyline(polylineOptions);

                    Toast.makeText(MapsActivity.this, "Network Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

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
    }

    /**
     * @method Guarda el estado de la UI de la actividad si esta es cerrada por el SO antes de llamar al metodo onStop() o onPause() del ciclo de actividad o si el usuario gira la pantalla ya que en este caso el SO  destruye y vuelve a crear la actividad para que el sistema pueda dibujarla con recursos alternativos que podrian estar disponibles para la nueva configuracion de pantalla.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    /**
     * @method Recupera el estado de la UI de la actividad si esta es cerrada por el SO antes de llamar al metodo onStop() o onPause() del ciclo de actividad o si el usuario gira la pantalla ya que en este caso el SO  destruye y vuelve a crear la actividad para que el sistema pueda dibujarla con recursos alternativos que podrian estar disponibles para la nueva configuracion de pantalla.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

    }

}
