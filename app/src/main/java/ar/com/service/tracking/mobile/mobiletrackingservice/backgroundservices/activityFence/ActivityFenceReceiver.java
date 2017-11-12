package ar.com.service.tracking.mobile.mobiletrackingservice.backgroundservices.activityFence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.awareness.fence.FenceState;

import ar.com.service.tracking.mobile.mobiletrackingservice.utils.MessageHelper;

import static android.content.ContentValues.TAG;

/**
 * Created by miglesias on 22/09/17.
 */

public class ActivityFenceReceiver extends BroadcastReceiver {

    // Handle the callback on the Intent.
    @Override
    public void onReceive(Context context, Intent intent) {

        FenceState fenceState = FenceState.extract(intent);

        if (TextUtils.equals(fenceState.getFenceKey(), "walkingWithInVehicleFenceKey")) {
            switch(fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    MessageHelper.toast(context, "Te estas moviendo" , Toast.LENGTH_SHORT);
                    Log.w(TAG, "Te estas moviendo");
                    break;
                case FenceState.FALSE:
                    MessageHelper.toast(context, "Estas quieto" , Toast.LENGTH_SHORT);
                    Log.w(TAG, "Estas quieto");
                    break;
                case FenceState.UNKNOWN:
                    MessageHelper.toast(context, "Estado desconocido", Toast.LENGTH_SHORT);
                    Log.w(TAG, "Estado desconocido");
                    break;
            }
        }

    }
}
