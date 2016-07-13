package com.enterprises.wayne.simplefacedetectorexample;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.vision.CameraSource;

import butterknife.internal.DebouncingOnClickListener;

public class MainActivity extends AppCompatActivity implements ServiceConnection
{

    /* fields */
    private CameraSource mCameraSource;
    private FaceTrackingService mfaceTrackingService;
    private boolean mBoundToService;

    /* UI */
    private Button mButtonStartOrStop;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // reference views
        mButtonStartOrStop = (Button) findViewById(R.id.buttonStartOrStop);

        // add listener, starts or stops the tracker in the FaceTrackingSerivice
        mButtonStartOrStop.setOnClickListener(new DebouncingOnClickListener()
        {
            @Override
            public void doClick(View v)
            {
                if (!mBoundToService)
                    return;

                if (mButtonStartOrStop.getText().equals(getString(R.string.start)))
                {
                    mfaceTrackingService.startTracking();
                    mButtonStartOrStop.setText(R.string.stop);
                } else
                {
                    mfaceTrackingService.stopTracking();
                    mButtonStartOrStop.setText(R.string.start);
                }
            }
        });


    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // bind to the facetracking service
        Intent intent = new Intent(this, FaceTrackingService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder)
    {
        Log.e("Game", "service connected");
        mBoundToService = true;
        FaceTrackingService.LocalBinder localBinder = (FaceTrackingService.LocalBinder) binder;
        mfaceTrackingService = localBinder.getService();
        mButtonStartOrStop.setText(mfaceTrackingService.isTracking() ? R.string.stop : R.string.start);
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        Log.e("Game", "service disconnected");
        mBoundToService = false;
    }
}
