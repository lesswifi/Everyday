package compsci290.edu.duke.myeveryday.Services;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.concurrent.ExecutionException;

import compsci290.edu.duke.myeveryday.util.RetrieveDataTask;

/**
 * Created by Divya on 4/20/17.
 */

public class WeatherService {

    private String weatherRequestURL = "http://api.openweathermap.org/data/2.5/weather?lat=";
    private String weatherIconRequestURL = "http://openweathermap.org/img/w/";
    private Location mLastLocation;
    private String mResult;

    // Default no argument instructor
    public WeatherService() {

    }

    public WeatherService(Location location) {
        // request weather from api
        mLastLocation = location;

    }


    public String getWeatherIcon() throws ExecutionException, InterruptedException {
        weatherRequestURL = weatherRequestURL + ((int) mLastLocation.getLatitude()) + "&lon=" + ((int) mLastLocation.getLongitude()) + "&APPID=b3024c1cc8918c53bcc2403f42e34473";
        Log.d("Weather Service", weatherRequestURL);
        RetrieveDataTask rd = new RetrieveDataTask(weatherRequestURL);
        rd.execute();
        mResult = rd.get();
        Log.d("Weather Service", mResult);

        try {
            JSONObject object = (JSONObject) new JSONTokener(mResult).nextValue();

            JSONObject weather = object.getJSONArray("weather").getJSONObject(0);
            String icon = weather.getString("icon");
            Log.d("Weather Service", icon);
            return weatherIconRequestURL + icon + ".png";
        } catch (JSONException e) {
            // Appropriate error handling code
        }
        return null;
    }

}
