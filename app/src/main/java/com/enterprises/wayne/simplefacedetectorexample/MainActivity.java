package com.enterprises.wayne.simplefacedetectorexample;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.internal.DebouncingOnClickListener;

public class MainActivity extends AppCompatActivity
{

    /* UI */
    private Button mButtonStart;
    private Button mButtonStop;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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

        // check which button to enable
        if (isServiceRunning(FaceTrackingService.class))
            mButtonStart.setEnabled(false);
        else
            mButtonStop.setEnabled(false);
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


}
