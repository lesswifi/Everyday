package compsci290.edu.duke.myeveryday.Services;

/**
 * Created by Divya on 4/23/17.
 */

public class LatLng {
    private Double mLatitude;
    private Double mLongitude;


    public LatLng() {
        this(0.0,0.0);
    }

    public LatLng(Double latitude, Double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }
    public void setLatitude(Double latitude) {
        this.mLatitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.mLongitude = longitude;
    }


}