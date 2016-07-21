package com.enterprises.wayne.simplefacedetectorexample;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * Face tracker for each detected individual. This maintains a face graphic within the app's
 * associated face overlay.
 */
public class FaceTracker extends Tracker<Face>
{

    private final TrackerListener listener;

    FaceTracker(TrackerListener listener)
    {
        super();
        this.listener = listener;
    }

    /**
     * Start tracking the detected face instance within the face overlay.
     */
    @Override
    public void onNewItem(int faceId, Face item)
    {
        Log.e("Game", "new face " + faceId);
    }

    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face)
    {
        Log.e("Game", "update on face " + face.getId());

        // invoke the listener
        if (listener != null)
            listener.onUpdate(face.getId());
    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults)
    {

        Log.e("Game", "missing");
    }

    /**
     * Called when the face is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone()
    {
        Log.e("Game", "Done");
    }
}
