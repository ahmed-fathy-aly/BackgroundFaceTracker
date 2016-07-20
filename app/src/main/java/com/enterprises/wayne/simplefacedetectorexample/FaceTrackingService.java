package com.enterprises.wayne.simplefacedetectorexample;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.view.Display;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * a background service that tracks faces and feeds the info of each instance to a FaceTracker
 */
public class FaceTrackingService extends Service
{
    private CameraSource mCameraSource;
    private FaceDetector mdetector;
    private DeviceIdleReceiver receiver;
    private PowerManager.WakeLock wakeLock;

    public FaceTrackingService()
    {
    }


    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.e("Game", "onCreate");

        // remain alive
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(
                getApplicationContext().POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "faceTrackerWakeLock");
        wakeLock.acquire();

        // track
        startTracking();

        // stop tracking if the screen goes idle
        addDeviceIdleMonitor();

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Log.e("Game", "onDestroy");
        stopTracking();
        wakeLock.release();
    }

    /**
     * sets up the detector and the camera source and starts monitoring for faces
     */
    public void startTracking()
    {
        Log.e("Game", "starting tracking");

        // setup the face tracker
        mdetector = new FaceDetector.Builder(this)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        // assign the processor which gets invoked with camera updates
        mdetector.setProcessor(
                new MultiProcessor.Builder<>(new FaceTrackerFactory())
                        .build());
        Log.e("Game", "detector operational " + mdetector.isOperational());

        // setup camera source
        mCameraSource = new CameraSource.Builder(this, mdetector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        // start the camera source
        try
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                stopSelf();
            mCameraSource.start();
            Log.e("Game", "camera source started");
        } catch (Exception e)
        {
            Log.e("Game", "error starting camera source");
            e.printStackTrace();
        }

    }

    /**
     * releases the face detector and the camera
     */
    public void stopTracking()
    {
        Log.e("Game", "stop tracking");

        if (mCameraSource != null)
        {
            mCameraSource.release();
            mdetector.release();
        }

    }

    /**
     * registers a receiver that stops tracking if the device is idle
     */
    public void addDeviceIdleMonitor()
    {
        // create the intent filter
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);

        // create and registers the receiver
        receiver = new DeviceIdleReceiver();
        getApplicationContext().registerReceiver(receiver, filter);
    }

    /**
     * unregisters the receiver that checks if the device is idle
     */
    public void removeDeviceIdleMonitor()
    {
        if (receiver != null)
            try
            {
                unregisterReceiver(receiver);
            } catch (Exception e)
            {
            }
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    class DeviceIdleReceiver extends WakefulBroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.e("Game", "device idle monitor" + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
                startTracking();
            else
                stopTracking();
        }
    }
}
