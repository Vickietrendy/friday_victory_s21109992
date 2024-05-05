//
// Name                 Victory Friday
// Student ID           S2110999
// Programme of Study   Computing

package org.me.gcu.friday_victory_s2110999;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DetailedLatestObservationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_latest_observation);

        // Find the BottomNavigationView in the layout
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the item selected listener
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    startActivity(new Intent(DetailedLatestObservationActivity.this, MainActivity.class));
                    return true; // Indicates that the item is selected
                } else if (id == R.id.navigation_map) {
                    // Navigate to map activity
                    startActivity(new Intent(DetailedLatestObservationActivity.this, MapActivity.class));
                    return true;
                } else if (id == R.id.navigation_settings) {
                    // Navigate to settings activity
                    startActivity(new Intent(DetailedLatestObservationActivity.this, SettingsActivity.class));
                    return true;
                }
                return false; // Indicates that the item is not handled
            }
        });

        // Retrieve data passed from previous activity
        Intent intent = getIntent();
        String cityName = intent.getStringExtra("cityName");
        String titleTag = intent.getStringExtra("titleTag");
        String description = intent.getStringExtra("description");

        // Update UI elements with received data
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


        TableLayout descriptionTableLayout = findViewById(R.id.descriptionTableLayout);

        cityNameTextView.setText(cityName);
        dayTimeTextView.setText(dayAndTime);
        temperatureTextView.setText(temperature);

        int iconResourceId = getWeatherIcon(weather);

        weatherIconImageView.setImageResource(iconResourceId);

        // Display description in a scrollable layout
        displayDescription(description, descriptionTableLayout);
    }

    // Method to display description in key-value pairs using a TableLayout
    private void displayDescription(String description, TableLayout descriptionTableLayout) {
        String[] keyValuePairs = description.split(",");
        for (String pair : keyValuePairs) {
            try {
                String[] keyValue = pair.split(":");
                if (keyValue.length >= 2) {
                    TableRow tableRow = new TableRow(this);
                    tableRow.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    ));

                    TextView keyTextView = new TextView(this);
                    keyTextView.setText(keyValue[0]);
                    TableRow.LayoutParams keyParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    keyParams.setMargins(200, 0, 50, 0); // Add padding between key and value
                    keyTextView.setLayoutParams(keyParams);

                    TextView valueTextView = new TextView(this);
                    valueTextView.setText(keyValue[1]);

                    tableRow.addView(keyTextView);
                    tableRow.addView(valueTextView);

                    descriptionTableLayout.addView(tableRow);

                    // Add padding between pairs
                    TableRow paddingRow = new TableRow(this);
                    paddingRow.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            200
                    ));
                    descriptionTableLayout.addView(paddingRow);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
}
