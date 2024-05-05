package org.me.gcu.friday_victory_s2110999;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private Button saveButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Find the BottomNavigationView in the layout
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the item selected listener
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                    return true; // Indicates that the item is selected
                } else if (id == R.id.navigation_map) {
                    // Navigate to map activity
                    startActivity(new Intent(SettingsActivity.this, MapActivity.class));
                    return true;
                } else if (id == R.id.navigation_settings) {
                    // Navigate to settings activity
                    startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                    return true;
                }
                return false; // Indicates that the item is not handled
            }
        });

        // Initialize views
        timePicker = findViewById(R.id.timePicker);
        saveButton = findViewById(R.id.saveButton);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Load saved time from SharedPreferences and set it to the TimePicker
        int savedHour = sharedPreferences.getInt("hour", 0);
        int savedMinute = sharedPreferences.getInt("minute", 0);
        timePicker.setHour(savedHour);
        timePicker.setMinute(savedMinute);

        // Save button click listener
        saveButton.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            // Save selected time to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("hour", hour);
            editor.putInt("minute", minute);
            editor.apply();

            Toast.makeText(SettingsActivity.this, "Refresh time saved", Toast.LENGTH_SHORT).show();
        });
    }
}
