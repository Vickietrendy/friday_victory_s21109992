//
// Name                 Victory Friday
// Student ID           S2110999
// Programme of Study   Computing

package org.me.gcu.friday_victory_s2110999;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityDetailsActivity<WeatherForecast> extends AppCompatActivity {

    private Map<String, String> cityCodes;

    private WeatherForecast weatherForecast;

    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_details);

        // Find the BottomNavigationView in the layout
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the item selected listener
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    startActivity(new Intent(CityDetailsActivity.this, MainActivity.class));
                    return true; // Indicates that the item is selected
                } else if (id == R.id.navigation_map) {
                    // Navigate to map activity
                    startActivity(new Intent(CityDetailsActivity.this, MapActivity.class));
                    return true;
                } else if (id == R.id.navigation_settings) {
                    // Navigate to settings activity
                    startActivity(new Intent(CityDetailsActivity.this, SettingsActivity.class));
                    return true;
                }
                return false; // Indicates that the item is not handled
            }
        });

        // Retrieve data passed from previous activity
        Intent intent = getIntent();
        cityName = intent.getStringExtra("cityName");
        Log.d("cityName", cityName);
        String titleTag = intent.getStringExtra("titleTag");
        Log.d("title", titleTag);
        String description = intent.getStringExtra("description");
        Log.d("description", description);

        // Split the titleTag string into its components based on the second colon
        String[] titleComponents = titleTag.split(":", 3);
        String dayAndTime = titleComponents[0].trim() + titleComponents[1].trim();

        // Split the day and time into weather and temperature based on the comma
        String[] dayAndTimeComponents = titleComponents[2].split(",");
        String weather = dayAndTimeComponents[0].trim();
        String temperature = dayAndTimeComponents[1].trim();

        // Update UI elements with received data
        TextView cityNameTextView = findViewById(R.id.cityNameTextView);
        TextView dayTimeTextView = findViewById(R.id.dayTimeTextView);
        TextView temperatureTextView = findViewById(R.id.temperatureTextView);
        ImageView weatherIconImageView = findViewById(R.id.weatherIconImageView);

        Button seeMoreButton = findViewById(R.id.seeMoreButton);

        // Fetch three-day weather forecast data
        new FetchThreeDayForecastTask().execute(cityName);

        cityNameTextView.setText(cityName);
        dayTimeTextView.setText(dayAndTime);
        temperatureTextView.setText(temperature);

        int iconResourceId = getWeatherIcon(weather);

        weatherIconImageView.setImageResource(iconResourceId);

        // Set click listener for the "See More" button
        seeMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to navigate to the detailed latest observation activity
                Intent detailedLatestObservationIntent = new Intent(CityDetailsActivity.this, DetailedLatestObservationActivity.class);
                // Pass data to the new activity
                detailedLatestObservationIntent.putExtra("cityName", cityName);
                detailedLatestObservationIntent.putExtra("titleTag", titleTag);
                detailedLatestObservationIntent.putExtra("description", description);
                startActivity(detailedLatestObservationIntent);
            }
        });
    }

    // Initialize city codes map
    private void initializeCityCodes() {
        cityCodes = new HashMap<>();
        cityCodes.put("Glasgow", "2648579");
        cityCodes.put("London", "2643743");
        cityCodes.put("NewYork", "5128581");
        cityCodes.put("Oman", "287286");
        cityCodes.put("Mauritius", "934154");
        cityCodes.put("Bangladesh", "1185241");
    }

    // AsyncTask to fetch three-day weather forecast data
    private class FetchThreeDayForecastTask extends AsyncTask<String, Void, List<WeatherForecast>> {

        @Override
        protected List<WeatherForecast> doInBackground(String... params) {
            String cityName = params[0];
            String cityCode = getCityCode(cityName);

            List<WeatherForecast> weatherForecasts = new ArrayList<>();

            // Fetch and parse three-day forecast RSS feed
            try {
                URL forecastUrl = new URL("https://weather-broker-cdn.api.bbci.co.uk/en/forecast/rss/3day/" + cityCode);
                HttpURLConnection connection = (HttpURLConnection) forecastUrl.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();

                // Parse RSS feed data for three-day forecast
                weatherForecasts = parseThreeDayForecastRss(inputStream);

                inputStream.close();
                connection.disconnect();
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            return weatherForecasts;
        }

        @Override
        protected void onPostExecute(List<WeatherForecast> weatherForecasts) {
            // Display three-day weather forecast in UI
            displayWeatherForecast(weatherForecasts, cityName);
        }
    }

    // Method to parse three-day forecast RSS feed data
    private List<WeatherForecast> parseThreeDayForecastRss(InputStream inputStream) throws XmlPullParserException, IOException {
        List<WeatherForecast> weatherForecasts = new ArrayList<>();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, null);

        int eventType = parser.getEventType();
        WeatherForecast weatherForecast = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = null;
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    tagName = parser.getName();
                    if ("item".equals(tagName)) {
                        weatherForecast = new WeatherForecast();
                    } else if (weatherForecast != null) {
                        if ("title".equals(tagName)) {
                            weatherForecast.setTitle(parser.nextText());
                        } else if ("description".equals(tagName)) {
                            weatherForecast.setDescription(parser.nextText());
                        } else if ("pubDate".equals(tagName)) {
                            weatherForecast.setPubDate(parser.nextText());
                        } else if ("georss:point".equals(tagName)) {
                            weatherForecast.setGeoRSSPoint(parser.nextText());
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    tagName = parser.getName();
                    if ("item".equals(tagName) && weatherForecast != null) {
                        weatherForecasts.add(weatherForecast);
                    }
                    break;
            }
            eventType = parser.next();
        }

        return weatherForecasts;
    }

    // Method to display three-day weather forecast in UI
    private void displayWeatherForecast(final List<WeatherForecast> weatherForecasts, String cityName) {
        // Find the parent layout to add forecast items
        LinearLayout forecastContainer = findViewById(R.id.forecastContainer);

        // Clear existing views in the forecast container
        forecastContainer.removeAllViews();

        // Iterate over the list of weather forecasts
        for (final WeatherForecast forecast : weatherForecasts) {
            // Create a TextView to display the forecast title
            TextView forecastTextView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 16, 0, 16); // Add margin between forecast items
            forecastTextView.setLayoutParams(params);
            forecastTextView.setBackgroundResource(R.drawable.rounded_rectangle);
            forecastTextView.setPadding(16, 16, 16, 16);
            forecastTextView.setTextColor(getResources().getColor(android.R.color.white));
            forecastTextView.setText(forecast.getTitle());
            forecastTextView.setTextSize(18); // Set the text size
            forecastTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create intent to navigate to DetailedForecastActivity
                    Intent intent = new Intent(CityDetailsActivity.this, DetailedForecastActivity.class);
                    // Pass data to the DetailedForecastActivity
                    intent.putExtra("cityName", cityName);
                    intent.putExtra("title", forecast.getTitle());
                    intent.putExtra("description", forecast.getDescription());
                    startActivity(intent);
                }
            });

            // Add TextView to the forecast container
            forecastContainer.addView(forecastTextView);
        }
    }


    // Method to get city code based on city name
    private String getCityCode(String cityName) {
        // Initialize city codes map
        initializeCityCodes();

        // Retrieve city code based on city name
        String cityCode = cityCodes.get(cityName);

        // Check if city code exists for the provided city name
        if (cityCode != null) {
            return cityCode;
        } else {
            // Handle case where city code is not found for the provided city name
            return "City code not found";
        }
    }

    public int getWeatherIcon(String condition) {
        condition = condition.toLowerCase(); // Normalize the string to lower case to simplify comparisons

        switch (condition) {
            case "light cloud":
                return R.drawable.cloudy;
            case "clear":
                return R.drawable.day_clear;
            case "sunny":
                return R.drawable.day_clear;
            case "sunny intervals":
                return R.drawable.day_partial_cloud;
            case "light rain":
                return R.drawable.day_rain;
            case "heavy rain":
                return R.drawable.rain;
            case "light rain showers":
                return R.drawable.day_rain;
            case "thundery showers":
                return R.drawable.day_rain_thunder;
            case "sleet":
                return R.drawable.day_sleet;
            case "snow":
                return R.drawable.day_snow;
            case "snow and thunderstorms":
                return R.drawable.day_snow_thunder;
            case "thunderstorms":
                return R.drawable.thunder;
            case "tornado":
                return R.drawable.tornado;
            case "wind":
                return R.drawable.wind;
            case "fog":
                return R.drawable.fog;
            case "mist":
                return R.drawable.mist;
            case "overcast":
                return R.drawable.overcast;
            case "night clear":
                return R.drawable.night_clear;
            // Add more cases as needed for other conditions
            default:
                return R.drawable.day_clear; // Default case, if no match found
        }
    }

    public class WeatherForecast {
        private String title;
        private String description;
        private String pubDate;
        private String geoRSSPoint;

        public WeatherForecast() {
            // Default constructor
            this.title = null;
            this.description = null;
            this.pubDate = null;
            this.geoRSSPoint = null;
        }

        // Getters and setters for title
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        // Getters and setters for description
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        // Getters and setters for pubDate
        public String getPubDate() {
            return pubDate;
        }

        public void setPubDate(String pubDate) {
            this.pubDate = pubDate;
        }

        // Getters and setters for geoRSSPoint
        public String getGeoRSSPoint() {
            return geoRSSPoint;
        }

        public void setGeoRSSPoint(String geoRSSPoint) {
            this.geoRSSPoint = geoRSSPoint;
        }
    }
}
