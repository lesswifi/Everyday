package compsci290.edu.duke.everyday;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by Divya on 4/20/17.
 */

public class WeatherService {

    private String weatherRequestURL = "api.openweathermap.org/data/2.5/weather?lat={";

    public WeatherService() {
        // request weather from api

        // weatherRequestURL = weatherRequestURL + mLastLocation.getLatitude() + "}&lon={" + mLastLocation.getLongitude() + "}";

    }


    public String getWeather(String response) {
        try {
            JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
            String requestID = object.getString("requestId");
            int likelihood = object.getInt("likelihood");
            JSONArray photos = object.getJSONArray("photos");

        } catch (JSONException e) {
            // Appropriate error handling code
        }
        return null;
    }


}
