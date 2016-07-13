package com.enterprises.wayne.simplefacedetectorexample;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * a background service that tracks faces and feeds the info of each instance to a FaceTracker
 */
public class FaceTrackingService extends Service
{
    private IBinder mBinder = new LocalBinder();
    private CameraSource mCameraSource;
    private FaceDetector mdetector;
    private boolean mIsTracking;

    public FaceTrackingService()
    {
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
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build();

        // start the camera source
        try
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                stopSelf();
            mCameraSource.start();
            mIsTracking = true;
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
        if (mCameraSource != null)
        {
            mCameraSource.release();
            mdetector.release();
        }

        mIsTracking = false;
    }

    /**
     * checks if the detector is currently monitoring
     */
    public boolean isTracking()
    {
        return mIsTracking;
    }

    /**
     * binds the service to an activity
     */
    public class LocalBinder extends Binder
    {
        FaceTrackingService getService()
        {
            return FaceTrackingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

}
