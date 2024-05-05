//
// Name                 Victory Friday
// Student ID           S2110999
// Programme of Study   Computing

package org.me.gcu.friday_victory_s2110999;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Map<String, LatLng> cityCoordinates;
    private Map<String, String> cityCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Find the BottomNavigationView in the layout
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the item selected listener
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    startActivity(new Intent(MapActivity.this, MainActivity.class));
                    return true; // Indicates that the item is selected
                } else if (id == R.id.navigation_map) {
                    // Navigate to map activity
                    startActivity(new Intent(MapActivity.this, MapActivity.class));
                    return true;
                } else if (id == R.id.navigation_settings) {
                    // Navigate to settings activity
                    startActivity(new Intent(MapActivity.this, SettingsActivity.class));
                    return true;
                }
                return false; // Indicates that the item is not handled
            }
        });

        // Initialize city coordinates
        initializeCityCoordinates();

        // In your onCreate() method or wherever appropriate
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mapContainer, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    private void initializeCityCodes() {
        cityCodes = new HashMap<>();
        cityCodes.put("Glasgow", "2648579");
        cityCodes.put("London", "2643743");
        cityCodes.put("New York", "5128581");
        cityCodes.put("Oman", "287286");
        cityCodes.put("Mauritius", "934154");
        cityCodes.put("Bangladesh", "1185241");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        initializeCityCodes();

        // Add markers for each city
        for (Map.Entry<String, LatLng> entry : cityCoordinates.entrySet()) {
            String cityName = entry.getKey();
            LatLng cityLatLng = entry.getValue();
            mMap.addMarker(new MarkerOptions()
                    .position(cityLatLng)
                    .title(cityName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }

        // Set click listener for markers
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String cityName = marker.getTitle();
                String cityCode = cityCodes.get(cityName);

                if (cityCode != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String[] weatherData = fetchData(cityCode);
                            if (weatherData != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(MapActivity.this, CityDetailsActivity.class);
                                        intent.putExtra("cityName", cityName);
                                        intent.putExtra("titleTag", weatherData[1]);
                                        Log.d("titleBug", weatherData[1]);
                                        intent.putExtra("description", weatherData[2]);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }
                    }).start();
                }
                return true;
            }
        });

        // Move camera to a default location
        LatLng defaultLocation = new LatLng(55.8642, -4.2518); // Glasgow coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 6));
    }

    // Initialize city coordinates
    private void initializeCityCoordinates() {
        cityCoordinates = new HashMap<>();
        cityCoordinates.put("Glasgow", new LatLng(55.8642, -4.2518));
        cityCoordinates.put("London", new LatLng(51.5074, -0.1278));
        cityCoordinates.put("New York", new LatLng(40.7128, -74.0060));
        cityCoordinates.put("Oman", new LatLng(21.4735, 55.9754));
        cityCoordinates.put("Mauritius", new LatLng(-20.3484, 57.5522));
        cityCoordinates.put("Bangladesh", new LatLng(23.685, 90.3563));
    }

    private String[] fetchData(String cityCode) {
        String observationRssFeedUrl = "https://weather-broker-cdn.api.bbci.co.uk/en/observation/rss/" + cityCode;
        try {
            URL url = new URL(observationRssFeedUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = connection.getInputStream();
            return parseRssFeed(inputStream);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String[] parseRssFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, null);

        String[] weatherData = new String[3]; // cityName, title, description
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

        // Extract city name from title
        String cityName = extractCityName(title);
        // Extract relevant information from title and description
        String[] titleAndDescription = extractTitleAndDescription(title, description);

        weatherData[0] = cityName;
        weatherData[1] = title;
        weatherData[2] = description;

        return weatherData;
    }

    private String extractCityName(String title) {
        // Split title to extract city name
        String[] parts = title.split(":");
        if (parts.length > 0) {
            return parts[0].trim();
        }
        return null;
    }

    private String[] extractTitleAndDescription(String title, String description) {
        // Split title to extract relevant information
        String[] parts = title.split(":");
        String titlePart = "";
        String descriptionPart = "";
        if (parts.length > 1) {
            titlePart = parts[1].trim();
        }

        // Split description to extract relevant information
        parts = description.split(",");
        if (parts.length > 1) {
            descriptionPart = parts[0].trim() + ", " + parts[1].trim();
        }

        return new String[]{titlePart, descriptionPart};
    }

}
