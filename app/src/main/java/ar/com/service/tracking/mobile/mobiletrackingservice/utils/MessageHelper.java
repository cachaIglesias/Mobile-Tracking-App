package ar.com.service.tracking.mobile.mobiletrackingservice.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;

/**
 * Created by miglesias on 01/07/17.
 */

public class MessageHelper {

    /**
     * @method Dibuja dialogo de alerta con intent
     */

    static public void showAlertWithIntent(final Activity activity, final Intent intent, String title, String message, String intentMessage) {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);

        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(intentMessage, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        activity.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    /**
     * @method Dibuja dialogo de alerta
     */

    static public void showOnlyAlert(final Activity activity, String title, String message) {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);

        dialog.setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });

        dialog.show();
    }

    /**
     * @method muestra un mensaje de sistema
     *
     * @param context contexto en el que se muestra
     * @param message texto a mostrar
     * @param duration puede ser Toast.LENGTH_SHORT o Toast.LENGTH_LONG
     */
    static public void toast(Context context, String message, int duration){
        Toast.makeText(context, message, duration).show();
    }

}
