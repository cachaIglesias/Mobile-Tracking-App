package ar.com.service.tracking.mobile.mobiletrackingservice;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by miglesias on 01/07/17.
 */

public class PermissionHelper {

    /**
     *  @method Permite saber si una funcionalidad esta disponible en el dispositivo
     */
    public boolean saberSiUnaFuncionalidadEstaDisponibleEnElDispositivo(Context context, String feature){
        return context.getPackageManager().hasSystemFeature(feature);
    }

    /**
     * @method Comprobar si se dispone de un permiso
     */
    public boolean comprobarSiHayPermiso(Context context, String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
        return (permissionCheck == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * @method verifica si existe un permiso, y lo solicita si es necesario. En caso de que el usuario lo cancele reiteradamente esto se detecte y se podria mostrar un mensaje en el que se explica porque se requiere dicho permiso.
     */
    public void verificarSiExistePermisoYSolicitarSiEsNecesario(Context context, String permission, int requestCode, String title, String explanationMessage) {

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            // Para ayudar a detectar situaciones en las cuales el usuario podría necesitar una explicación, Android proporciona un método de utilidad: shouldShowRequestPermissionRationale(). Este método muestra true si la app solicita el permiso anteriormente y el usuario rechaza la solicitud.
            // Si el usuario rechaza la solicitud de permiso en el pasado y selecciona la opción Don't ask again en el diálogo de solicitud de permiso del sistema, el método muestra false. También muestra false si una política de dispositivo prohíbe que la app tenga ese permiso.
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    permission)) {

                // TODO: ver como hacer para cuando sale este mensaje y se de continuar, se vea el de solicitud de permiso pero que no se vean los dos a la vez en la pantalla.
                MessageHelper.showOnlyAlert((Activity) context, title, explanationMessage);

            } else{

                ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);

            }

        }
    }

}
