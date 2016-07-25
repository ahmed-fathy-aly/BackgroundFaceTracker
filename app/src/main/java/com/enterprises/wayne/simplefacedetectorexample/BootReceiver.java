package com.enterprises.wayne.simplefacedetectorexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ahmed on 7/21/2016.
 * relaunches the face tracking service on device reboot if it was running when the device was powered off
 */
public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        boolean shouldRelaunch = PreferencesUtils.getServiceStarted(context.getApplicationContext());
        Log.e("Game", "boot up receiver, relaunch face tracker service ? " + shouldRelaunch);
        if (shouldRelaunch)
            context.startService(new Intent(context, FaceTrackingService.class));
    }
}
