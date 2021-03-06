package com.enterprises.wayne.simplefacedetectorexample;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static android.hardware.Camera.getCameraInfo;
import static android.hardware.Camera.getNumberOfCameras;

/**
 * a background service that tracks faces and feeds the info of each instance to a FaceTracker
 */
public class FaceTrackingService extends Service implements TrackerListener {
    /* fields */
    private CameraSource mCameraSource;
    private FaceDetector mDetector;
    private DeviceIdleReceiver receiver;
    private PowerManager.WakeLock wakeLock;

    public FaceTrackingService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("Game", "onCreate");

        // remain alive
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(
                getApplicationContext().POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "faceTrackerWakeLock");
        wakeLock.acquire();

        // track
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager.isScreenOn())
            startTracking();

        // stop tracking if the screen goes idle
        addDeviceIdleMonitor();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("Game", "onDestroy");
        stopTracking();
        wakeLock.release();
    }

    /**
     * sets up the detector and the camera source and starts monitoring for faces
     */
    public void startTracking() {
        Log.e("Game", "starting tracking");

        // setup the face tracker
        mDetector = new FaceDetector.Builder(this)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        // assign the processor which gets invoked with camera updates
        mDetector.setProcessor(
                new MultiProcessor.Builder<>(new FaceTrackerFactory(this))
                        .build());
        Log.e("Game", "detector operational " + mDetector.isOperational());


        // get the maximum preview sizes of the font facing camera
        Camera.Size optimalCameraSize = null;
        int frontFacingCameraId = getFrontFacingCameraId();
        if (frontFacingCameraId != -1) {
            android.hardware.Camera camera = android.hardware.Camera.open(frontFacingCameraId);
            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            optimalCameraSize = getMaxCameraSize(sizes);
        }
        if (optimalCameraSize != null)
            Log.e("Game", "optimal size " + optimalCameraSize.width + " " + optimalCameraSize.height);

        // setup camera source
        CameraSource.Builder cameraBuilder = new CameraSource.Builder(this, mDetector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(10.0f);
        if (optimalCameraSize != null)
            cameraBuilder.
                    setRequestedPreviewSize(optimalCameraSize.width, optimalCameraSize.height);
        mCameraSource = cameraBuilder.build();

        // start the camera source
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.e("Game", "camera permission not granted");
                stopSelf();
            }
            mCameraSource.start();
            Log.e("Game", "camera source started");
        } catch (Exception e) {
            Log.e("Game", "error starting camera source " + e.getMessage());
            Crashlytics.getInstance().logException(e);
        }
    }

    /**
     * @return the size with the biggest width*height
     */
    private Camera.Size getMaxCameraSize(List<Camera.Size> sizes) {
        long maxSize = 0;
        Camera.Size max = null;
        for (Camera.Size size : sizes)
            if (max == null
                    || 1l * size.width * size.height > maxSize) {
                max = size;
                maxSize = 1l * size.width * size.height;
            }
        return max;
    }

    /**
     * @return the id of the front facing camera, the id is the same as its position
     */
    public int getFrontFacingCameraId() {
        int numberOfCameras = getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                return i;
        }
        return -1;
    }


    /**
     * releases the face detector and the camera
     */
    public void stopTracking() {
        Log.e("Game", "stop tracking");

        if (mCameraSource != null) {
            mCameraSource.release();
            mDetector.release();
            mCameraSource = null;
        }
    }

    /**
     * takes a picture and schedule another one after that delay
     */
    private void takePicture(final int faceId) {
        // check if stopped tracking
        if (mCameraSource == null)
            return;

        // take a picture
        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {
                savePicture(bytes, faceId);
            }
        });

    }

    /**
     * saves to a file(you'll find it in internal storage/pictures
     * the file name will be something like FaceTracker 03-18-00 (hour-minutes-seconds)
     */
    private void savePicture(byte[] bytes, int faceId) {
        // create a file for the image
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh-mm-ss");
        String currentDate = simpleDateFormat.format(Calendar.getInstance().getTime());
        String fileName = "FaceTracker " + faceId + " " + currentDate + ".png";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.getInstance().logException(e);
        }
        Log.e("Game", "saving picture at " + file.getPath());

        // write the image to the file
        OutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file.getAbsolutePath());
            fOut.write(bytes);

            // ask the media scanner to include this file so we can see it in the file explorer
            MediaScannerConnection.scanFile(this,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });

        } catch (FileNotFoundException e) {
            Log.e("Game", "error saving picture " + e.getMessage());
            Crashlytics.getInstance().logException(e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Game", "error saving picture " + e.getMessage());
            Crashlytics.getInstance().logException(e);
            e.printStackTrace();
        } finally {
            try {
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
                Crashlytics.getInstance().logException(e);
            }
        }
    }


    /**
     * registers a receiver that stops tracking if the device is idle
     */
    public void addDeviceIdleMonitor() {
        // create the intent filter
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);

        // create and registers the receiver
        receiver = new DeviceIdleReceiver();
        getApplicationContext().registerReceiver(receiver, filter);
    }


    @Override
    public void onUpdate(int faceId) {
        takePicture(faceId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * starts tracking if the screen became active
     * stops tracking if the screen goes idle
     */
    class DeviceIdleReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("Game", "device idle monitor" + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
                startTracking();
            else
                stopTracking();
        }
    }

}
