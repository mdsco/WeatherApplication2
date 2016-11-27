package com.example.mike.weatherapplication;

import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mike on 11/25/16.
 */

public class ForecastFragment extends Fragment {

    public ForecastFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_refresh){

            FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
            fetchWeatherTask.execute("48103");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        String[] forecastArray = {
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy- 70/40",
                "Weds - Cloudy - 72/63",
                "Thurs - Asteroids - 75/65",
                "Fri - Heavy Rain - 65/56",
                "Sat - Shnowy - 60/51",
                "Sun - Sunny - 80/68"
        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_view,
                R.id.list_item_forecast_textview,
                weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);

        return rootView;

    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... paramses) {

            HttpURLConnection httpURLConnection = null;
            BufferedReader reader = null;
            String forecastJson = null;

            String postalCode = paramses[0];
            String format = "json";
            String units = "metric";
            int numDays = 7;
            String apiKey = BuildConfig.OPEN_WEATHER_MAP_API_KEY;

            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String MODE = "mode";
            final String UNITS = "units";
            final String COUNT = "cnt";
            final String APPID_PARAM = "APPID";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, postalCode)
                    .appendQueryParameter(MODE, format)
                    .appendQueryParameter(UNITS, units)
                    .appendQueryParameter(COUNT, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, apiKey)
                    .build();

            Log.v("uri", builtUri.toString());

            try {

                String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&APPID=";

                URL url = new URL(builtUri.toString());

                httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if(inputStream == null){
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null){

                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){

                    return null;
                }

                forecastJson = buffer.toString();
                Log.v("JSON", forecastJson);

            } catch(IOException e){

                Log.e("IOException ", e.toString() );
                return null;

            } finally {

                if(httpURLConnection != null){
                    httpURLConnection.disconnect();
                }
                if(reader != null){
                    try{
                        reader.close();
                    } catch (IOException e){
                        Log.e("ForecastFragment", "Error Closing Stream" + e);
                    }
                }

            }

            return null;
        }

    }
}