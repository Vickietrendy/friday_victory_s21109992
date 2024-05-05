

/*  Starter project for Mobile Platform Development in main diet 2023/2024
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

//
// Name                 _________________
// Student ID           _________________
// Programme of Study   _________________
//

// UPDATE THE PACKAGE NAME to include your Student Identifier
package org.me.gcu.friday_victory_s2110999;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private LinearLayout cityContainer;

    // Map to store city names and their corresponding location IDs
    private Map<String, String> cityCodes;
    private SharedPreferences sharedPreferences;

    // Define constants for the refresh times
    private static final int MORNING_REFRESH_HOUR = 8;
    private static final int MORNING_REFRESH_MINUTE = 0;
    private static final int EVENING_REFRESH_HOUR = 20;
    private static final int EVENING_REFRESH_MINUTE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Schedule automatic refresh
        scheduleAutomaticRefresh();

        // Find the BottomNavigationView in the layout
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the item selected listener
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                    return true; // Indicates that the item is selected
                } else if (id == R.id.navigation_map) {
                    // Navigate to map activity
                    startActivity(new Intent(MainActivity.this, MapActivity.class));
                    return true;
                } else if (id == R.id.navigation_settings) {
                    // Navigate to settings activity
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    return true;
                }
                return false; // Indicates that the item is not handled
            }
        });

//        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        cityContainer = findViewById(R.id.cityContainer);

        // Initialize city codes map
        initializeCityCodes();

        // Fetch weather data for each city
        new FetchWeatherDataTask().execute();
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

    // AsyncTask to fetch weather data for each city
    private class FetchWeatherDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            for (Map.Entry<String, String> entry : cityCodes.entrySet()) {
                String cityName = entry.getKey();
                String cityCode = entry.getValue();

                // Fetch and parse observation RSS feed
                fetchAndParseObservationRss(cityName, cityCode);
            }
            return null;
        }

        // Fetch and parse observation RSS feed
        private void fetchAndParseObservationRss(String cityName, String cityCode) {
            String observationRssFeedUrl = "https://weather-broker-cdn.api.bbci.co.uk/en/observation/rss/" + cityCode;
            try {
                URL observationUrl = new URL(observationRssFeedUrl);
                HttpURLConnection observationConnection = (HttpURLConnection) observationUrl.openConnection();
                observationConnection.setRequestMethod("GET");

                InputStream observationInputStream = observationConnection.getInputStream();

                // Parse observation RSS feed data
                parseRssFeed(observationInputStream, cityName);

                observationInputStream.close();
                observationConnection.disconnect();
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }

        // Parse RSS feed data
        private void parseRssFeed(InputStream inputStream, String cityName) throws XmlPullParserException, IOException {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            String title = null;
            String description = null;

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if ("title".equals(tagName)) {
                        title = parser.nextText();
                    }
                    if ("description".equals(tagName)) {
                        description = parser.nextText();
                    }
                }
                eventType = parser.next();
            }

            // Update UI with city information
            updateCityInfo(cityName, title, description);
            Log.d("city", title.toString());
        }

        // Update city information in UI
        private void updateCityInfo(final String cityName, final String title, final String description) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Create a LinearLayout to hold city information
                    LinearLayout cityLayout = new LinearLayout(MainActivity.this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    layoutParams.setMargins(0, 16, 0, 16); // Add padding between cityLayouts
                    cityLayout.setLayoutParams(layoutParams);
                    cityLayout.setOrientation(LinearLayout.VERTICAL);
                    cityLayout.setBackgroundResource(R.drawable.rounded_rectangle);
                    cityLayout.setPadding(16, 16, 16, 16);

                    // Create a LinearLayout to hold cityName and title components vertically
                    LinearLayout verticalLayout = new LinearLayout(MainActivity.this);
                    verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    verticalLayout.setOrientation(LinearLayout.VERTICAL);

                    // Create TextView for cityName
                    TextView cityNameTextView = new TextView(MainActivity.this);
                    cityNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    cityNameTextView.setText(cityName);
                    cityNameTextView.setTextColor(getResources().getColor(android.R.color.white));
                    cityNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24); // Adjust text size
                    cityNameTextView.setGravity(Gravity.CENTER_HORIZONTAL); // Align horizontally
                    verticalLayout.addView(cityNameTextView);

                    // Extract title components
                    String[] titleComponents = title.split(":", 3);
                    String dayAndTime = titleComponents[0].trim() + titleComponents[1].trim();
                    String dayTime = dayAndTime.split("-")[0];

                    // Split day and time into weather and temperature based on comma
                    String[] dayAndTimeComponents = titleComponents[2].split(",");
                    String weather = dayAndTimeComponents[0].trim();
                    String temperature = dayAndTimeComponents[1].trim();

                    // Create a LinearLayout to hold title components horizontally
                    LinearLayout horizontalLayout = new LinearLayout(MainActivity.this);
                    LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    horizontalLayout.setLayoutParams(horizontalParams);
                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                    // Create TextViews for dayTime, temperature, and weather
                    TextView dayTimeTextView = new TextView(MainActivity.this);
                    LinearLayout.LayoutParams dayTimeParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    dayTimeParams.setMargins(0, 0, 50, 0); // Add padding between TextViews
                    dayTimeTextView.setLayoutParams(dayTimeParams);
                    dayTimeTextView.setText(dayTime);
                    dayTimeTextView.setTextColor(getResources().getColor(android.R.color.black));
                    dayTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Adjust text size
                    horizontalLayout.addView(dayTimeTextView);

                    TextView temperatureTextView = new TextView(MainActivity.this);
                    LinearLayout.LayoutParams temperatureParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    temperatureParams.setMargins(0, 0, 50, 0); // Add padding between TextViews
                    temperatureTextView.setLayoutParams(temperatureParams);
                    temperatureTextView.setText(temperature);
                    temperatureTextView.setTextColor(getResources().getColor(android.R.color.black));
                    temperatureTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Adjust text size
                    horizontalLayout.addView(temperatureTextView);

                    // Create an ImageView to display the weather icon
                    ImageView weatherIconImageView = new ImageView(MainActivity.this);
                    LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            75 // Set height to 75dp
                    );
                    iconParams.setMargins(0, 16, 0, 0); // Add margin top
                    weatherIconImageView.setLayoutParams(iconParams);

                    // Get the resource ID for the weather icon
                    int iconResourceId = getWeatherIcon(weather);
                    weatherIconImageView.setImageResource(iconResourceId);

                    // Set gravity to center
                    //weatherIconImageView.setGravity(Gravity.CENTER);

                    // Add the weather icon to the horizontal layout
                    horizontalLayout.addView(weatherIconImageView);

                    // Add the horizontalLayout to verticalLayout
                    verticalLayout.addView(horizontalLayout);

                    // Add the verticalLayout to cityLayout
                    cityLayout.addView(verticalLayout);

                    // Add click listener to the cityLayout
                    cityLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Create intent to navigate to new activity
                            Intent intent = new Intent(MainActivity.this, CityDetailsActivity.class);
                            // Pass data to the new activity
                            intent.putExtra("cityName", cityName);
                            intent.putExtra("titleTag", title);
                            intent.putExtra("description", description);
                            startActivity(intent);
                        }
                    });

                    // Add the cityLayout to the cityContainer
                    cityContainer.addView(cityLayout);
                }
            });
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

    // Function to schedule automatic refresh at specified times
    private void scheduleAutomaticRefresh() {
        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Create intents for morning and evening refresh
        Intent morningIntent = new Intent(this, RefreshReceiver.class);
        morningIntent.putExtra("type", "morning");
        PendingIntent morningPendingIntent = PendingIntent.getBroadcast(this, 0, morningIntent, PendingIntent.FLAG_MUTABLE);

        Intent eveningIntent = new Intent(this, RefreshReceiver.class);
        eveningIntent.putExtra("type", "evening");
        PendingIntent eveningPendingIntent = PendingIntent.getBroadcast(this, 1, eveningIntent, PendingIntent.FLAG_MUTABLE);

        // Get the default calendar instances for morning and evening refresh times
        Calendar defaultMorningCalendar = Calendar.getInstance();
        defaultMorningCalendar.set(Calendar.HOUR_OF_DAY, MORNING_REFRESH_HOUR);
        defaultMorningCalendar.set(Calendar.MINUTE, MORNING_REFRESH_MINUTE);
        defaultMorningCalendar.set(Calendar.SECOND, 0);

        Calendar defaultEveningCalendar = Calendar.getInstance();
        defaultEveningCalendar.set(Calendar.HOUR_OF_DAY, EVENING_REFRESH_HOUR);
        defaultEveningCalendar.set(Calendar.MINUTE, EVENING_REFRESH_MINUTE);
        defaultEveningCalendar.set(Calendar.SECOND, 0);

        // Get the user-set calendar instances for morning and evening refresh times
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int userMorningHour = sharedPreferences.getInt("morning_hour", -1);
        int userMorningMinute = sharedPreferences.getInt("morning_minute", -1);
        int userEveningHour = sharedPreferences.getInt("evening_hour", -1);
        int userEveningMinute = sharedPreferences.getInt("evening_minute", -1);

        // Set the user-set refresh times if available, otherwise use default times
        Calendar morningCalendar = userMorningHour != -1 && userMorningMinute != -1 ?
                Calendar.getInstance() : defaultMorningCalendar;
        if (userMorningHour != -1 && userMorningMinute != -1) {
            morningCalendar.set(Calendar.HOUR_OF_DAY, userMorningHour);
            morningCalendar.set(Calendar.MINUTE, userMorningMinute);
            morningCalendar.set(Calendar.SECOND, 0);
        }

        Calendar eveningCalendar = userEveningHour != -1 && userEveningMinute != -1 ?
                Calendar.getInstance() : defaultEveningCalendar;
        if (userEveningHour != -1 && userEveningMinute != -1) {
            eveningCalendar.set(Calendar.HOUR_OF_DAY, userEveningHour);
            eveningCalendar.set(Calendar.MINUTE, userEveningMinute);
            eveningCalendar.set(Calendar.SECOND, 0);
        }

        // Check if the scheduled times are in the past, if so, schedule for the next day
        if (morningCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            morningCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (eveningCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            eveningCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Set up alarms for morning and evening refresh
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, morningCalendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, morningPendingIntent);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, eveningCalendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, eveningPendingIntent);

        // Toast.makeText(this, "Automatic refresh scheduled", Toast.LENGTH_SHORT).show();
    }

    public class RefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Retrieve the type of refresh from the intent
            String refreshType = intent.getStringExtra("type");

            // Perform the appropriate action based on the refresh type
            if ("morning".equals(refreshType)) {
                // Perform morning refresh
                performMorningRefresh(context);
            } else if ("evening".equals(refreshType)) {
                // Perform evening refresh
                performEveningRefresh(context);
            }
        }

        private void performMorningRefresh(Context context) {
            new FetchWeatherDataTask().execute();
        }

        private void performEveningRefresh(Context context) {
            new FetchWeatherDataTask().execute();
        }
    }

}
