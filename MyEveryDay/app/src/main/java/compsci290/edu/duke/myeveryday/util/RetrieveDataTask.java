package compsci290.edu.duke.myeveryday.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Divya on 4/20/17.
 *
 * RetrieveDataTask is an AsyncTask called by the WeatherService to make an API call not on the main UI thread
 */

public class RetrieveDataTask extends AsyncTask<Void, Void, String> {

    private Exception exception;
    private String mRequestURL;


    public RetrieveDataTask(String requestURL) {
        mRequestURL = requestURL;
    }

    protected void onPreExecute() {

    }

    protected String doInBackground(Void... urls) {

        try {
            URL url = new URL(mRequestURL);
            InputStream inputStream;
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int status = urlConnection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK)
                inputStream = urlConnection.getErrorStream();
            else
                inputStream = urlConnection.getInputStream();

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    protected void onPostExecute(String response) {
        if(response == null) {
            response = "THERE WAS AN ERROR";
        }
        Log.i("INFO", response);
    }



}
