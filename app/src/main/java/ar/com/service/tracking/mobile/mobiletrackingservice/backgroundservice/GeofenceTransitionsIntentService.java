package ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import ar.com.service.tracking.mobile.mobiletrackingservice.R;
import ar.com.service.tracking.mobile.mobiletrackingservice.activity.MapsActivity;
import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

/**
 * Created by miglesias on 10/09/17.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceTransitionsIS";

    public GeofenceTransitionsIntentService(){
        super(TAG);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    geofencingEvent.getErrorCode());
            String errorMessage = "";
            Log.e(TAG, "Error al realizar Geofence: " + errorMessage);
            MessageHelper.toast(getApplicationContext(), "Error al realizar Geofence: " + errorMessage, Toast.LENGTH_SHORT);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails( geofencingEvent );

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);

            Log.i(TAG, "Detalle de transicion Geofence: " + geofenceTransitionDetails);
        } else {
            // Log the error.
//            Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
//                    geofenceTransition));
            Log.e(TAG, "Tipo invalido de transicion Geofence: " + geofenceTransition);
            MessageHelper.toast(getApplicationContext(), "Tipo invalido de transicion Geofence: " + geofenceTransition, Toast.LENGTH_SHORT);
        }
    }

    private static String getGeofenceTransitionDetails(GeofencingEvent event) {
        String transitionString =
                GeofenceStatusCodes.getStatusCodeString(event.getGeofenceTransition());
        List triggeringIDs = new ArrayList();
        for (Geofence geofence : event.getTriggeringGeofences()) {
            triggeringIDs.add(geofence.getRequestId());
        }
        return String.format("%s: %s", transitionString, TextUtils.join(", ", triggeringIDs));
    }

    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts MainActivity.
        Intent notificationIntent = new Intent(getApplicationContext(), MapsActivity.class);

        // Get a PendingIntent containing the entire back stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MapsActivity.class).addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText("Click notification to return to App")
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);

        // Fire and notify the built Notification.
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

}
