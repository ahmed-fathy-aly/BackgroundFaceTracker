package com.enterprises.wayne.simplefacedetectorexample;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;

import butterknife.internal.DebouncingOnClickListener;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
{
    /* constants */
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    /* UI */
    private Button mButtonStart;
    private Button mButtonStop;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        // reference views
        mButtonStart = (Button) findViewById(R.id.buttonStart);
        mButtonStop = (Button) findViewById(R.id.buttonStop);


        // add listener, starts or stops the tracker in the FaceTrackingSerivice
        mButtonStart.setOnClickListener(new DebouncingOnClickListener()
        {
            @Override
            public void doClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, FaceTrackingService.class);
                getApplicationContext().startService(intent);
                mButtonStart.setEnabled(false);
                mButtonStop.setEnabled(true);
                PreferencesUtils.setServiceStarted(getApplicationContext(), true);
            }
        });
        mButtonStop.setOnClickListener(new DebouncingOnClickListener()
        {
            @Override
            public void doClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, FaceTrackingService.class);
                getApplicationContext().stopService(intent);
                mButtonStart.setEnabled(true);
                mButtonStop.setEnabled(false);
                PreferencesUtils.setServiceStarted(getApplicationContext(), false);
            }
        });

        // check if the service is already running
        if (isServiceRunning(FaceTrackingService.class))
            mButtonStop.setEnabled(true);
        else
        {
            // check if the camera permission is granted, if not then request it
            int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (rc == PackageManager.PERMISSION_GRANTED)
                mButtonStart.setEnabled(true);
            else
                requestCameraPermission();

        }

    }

    /**
     * checks if a specific service is running now or not
     */
    private boolean isServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (serviceClass.getName().equals(service.service.getClassName()))
                return true;
        return false;
    }


    /**
     * Handles the requesting of the camera permission.
     */
    private void requestCameraPermission()
    {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA))
        {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

    }

    /**
     * Callback for the result from requesting permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode != RC_HANDLE_CAMERA_PERM)
        {
            Log.e("Game", "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            mButtonStart.setEnabled(true);
            return;
        }
    }


}
