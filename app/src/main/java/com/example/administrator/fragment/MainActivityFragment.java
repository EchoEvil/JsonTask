package com.example.administrator.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivityFragment extends Fragment {
    private boolean flag = false;
    ArrayAdapter<String> myForecastArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_activity, container, false);

        String[] forecastArray = {"Today - Sunny - 55/ 63", "Tomorrow - Foggy - 70/46", "Saturday - Cloudy - 72 / 63", "Sunday - Rainy - 64 / 51", "Monday - Foggy - 70 / 46", "Tuesday - Sunny - 76 / 68"};

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));
        myForecastArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.layout_each_item, R.id.tv_element_list, weekForecast);

        ListView myListView = (ListView)rootView.findViewById(R.id.listView_forecast);


        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        fetchWeatherTask.execute("http://mpianatra.com/Courses/files/data.json");

        myListView.setAdapter(myForecastArrayAdapter);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = com.example.administrator.fragment.MainActivityFragment.FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {

            String link = params[0];

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                link = "http://mpianatra.com/Courses/files/data.json";
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //JSON response as a string.
            String cleanNewsJsonStr = null;


            try {

                final String BASE_URL = link;


                URL url = new URL(BASE_URL);


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();

                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"));


                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                cleanNewsJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e("TAG", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("TAG", "Error closing stream", e);
                    }
                }
            }

            String[] newsTwentyDaysArray = new String[0];

            newsTwentyDaysArray = getNewsTitlesDataFromJson(cleanNewsJsonStr, 20);

            if(newsTwentyDaysArray == null)
            {
                return null;
            }
            else
            {
                return newsTwentyDaysArray;
            }
        }


        @Override
        protected void onPostExecute(String[] newsTwentyDaysArray) {
            if(newsTwentyDaysArray != null)
            {
                List<String> stringList = new ArrayList<String>(Arrays.asList(newsTwentyDaysArray));
                myForecastArrayAdapter.clear();
                myForecastArrayAdapter.addAll(stringList);

                super.onPostExecute(newsTwentyDaysArray);
            }
        }

        private String[] getNewsTitlesDataFromJson(String newsJsonStr, int numNews) {

            try{
                final String OWM_LIST = "allNews";
                final String OWM_LINK = "link";
                final String OWM_TITLE = "title";

                JSONObject newsJsonObject = new JSONObject(newsJsonStr);
                JSONArray linkAndTitleArray = newsJsonObject.getJSONArray(OWM_LIST);


                String[] resultStrs = new String[numNews];
                for (int i = 0; i < linkAndTitleArray.length(); i++) {

                    JSONObject news = linkAndTitleArray.getJSONObject(i);


                    String link = (String) news.get(OWM_LINK);
                    String title = (String) news.get(OWM_TITLE);

                    resultStrs[i] = (i + 1) + " - " + title;
                }

                return resultStrs;
            }catch(Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }
    }

}
