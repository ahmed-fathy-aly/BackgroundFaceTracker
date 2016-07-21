package com.enterprises.wayne.simplefacedetectorexample;

/**
 * Created by ahmed on 7/21/2016.
 * allows the FaceTracker to notify the service onUpdate
 */
public interface  TrackerListener
{
    void onUpdate(int faceId);

}
