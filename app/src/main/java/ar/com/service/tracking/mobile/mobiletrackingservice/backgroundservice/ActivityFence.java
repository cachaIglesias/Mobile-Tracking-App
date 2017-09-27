package ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservice;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.DetectedActivity;

import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

import static android.content.ContentValues.TAG;

/**
 * Created by miglesias on 22/09/17.
 */

public class ActivityFence {

    // Declare variables for pending intent and fence receiver.
    private PendingIntent myPendingIntent;
    private ActivityFenceReceiver myFenceReceiver;
    private GoogleApiClient mGoogleApiClient;
    private Context context;


    // Initialize myPendingIntent and fence receiver in onCreate().


    public ActivityFence(Context context){

        this.setContext(context);

//        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
//        setMyPendingIntent(PendingIntent.getBroadcast(this.getContext(), 0, intent, 0));
//        setActivityFenceReceiver(new ActivityFenceReceiver());
//        registerReceiver(getActivityFenceReceiver(), new IntentFilter(FENCE_RECEIVER_ACTION));

       // double currentLocationLat;  // current location latitude
//        double currentLocationLng;  // current location longitude
//        long nowMillis = System.currentTimeMillis();
//        long oneHourMillis = 1L * 60L * 60L * 1000L;
//
//        AwarenessFence orExample = AwarenessFence.or(
//                AwarenessFence.not(LocationFence.in(
//                        currentLocationLat,
//                        currentLocationLng,
//                        100.0,
//                        100.0,
//                        0L)),
//                TimeFence.inInterval(nowMillis + oneHourMillis, Long.MAX_VALUE));

    }

    public void startFence(){
        this.setmGoogleApiClient(new GoogleApiClient.Builder(this.getContext())
                .addApi(Awareness.API)
                .build());
        this.getmGoogleApiClient().connect();

        // Create a fence.
        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);
        AwarenessFence inVehicleFence = DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE);

        long nowMillis = System.currentTimeMillis();
        long secondsMillis = 3L * 1000L;
        AwarenessFence timeFence = TimeFence.inInterval(nowMillis + secondsMillis, Long.MAX_VALUE);

        // Create a combination fence to AND primitive fences.
        AwarenessFence walkingWithInVehicleFence = AwarenessFence.or(inVehicleFence, timeFence);

        AwarenessFence movementFence = AwarenessFence.or(DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE), DetectedActivityFence.during(DetectedActivityFence.WALKING), DetectedActivityFence.during(DetectedActivityFence.ON_FOOT), DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE), DetectedActivityFence.during(DetectedActivityFence.RUNNING));

//        DetectedActivityFence.starting(DetectedActivityFence.IN_VEHICLE)
//        DetectedActivityFence.stopping(DetectedActivityFence.IN_VEHICLE)

//                  .addFence(ActivityChangeReceiver.FENCE_START_DRIVING,
//                DetectedActivityFence.starting(DetectedActivityFence.IN_VEHICLE),
//                ActivityChangeReceiver.getStartDrivingPendingIntent(MainActivity.this))
//                .addFence(ActivityChangeReceiver.FENCE_STOP_DRIVING,
//                        DetectedActivityFence.stopping(DetectedActivityFence.IN_VEHICLE),
//                        ActivityChangeReceiver.getStopDrivingPendingIntent(MainActivity.this))

        this.registerFence("walkingWithInVehicleFenceKey", DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE));
//        this.registerFence("walkingWithInVehicleFenceKey", walkingWithInVehicleFence);
    }

    protected void registerFence(final String fenceKey, final AwarenessFence fence) {
        Awareness.FenceApi.updateFences(
                this.getmGoogleApiClient(),
                new FenceUpdateRequest.Builder()
                        .addFence(fenceKey, fence, getMyPendingIntent())
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess()) {
                            MessageHelper.toast(getContext(), "Fence was successfully registered.", Toast.LENGTH_SHORT);
                            Log.w(TAG, "Fence was successfully registered.");
                        } else {
                            MessageHelper.toast(getContext(), "Fence could not be registered: " + status, Toast.LENGTH_SHORT);
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    }
                });
    }

    public PendingIntent getMyPendingIntent() {
        return myPendingIntent;
    }

    public void setMyPendingIntent(PendingIntent myPendingIntent) {
        this.myPendingIntent = myPendingIntent;
    }

    public ActivityFenceReceiver getActivityFenceReceiver() {
        return myFenceReceiver;
    }

    public void setActivityFenceReceiver(ActivityFenceReceiver myFenceReceiver) {
        this.myFenceReceiver = myFenceReceiver;
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
