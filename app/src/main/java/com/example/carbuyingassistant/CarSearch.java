package com.example.carbuyingassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CarSearch extends AppCompatActivity {

    private static final String TAG = "CarSearchActivity";

    // --- UI Elements ---
    private EditText locationInput;
    private EditText keywordInput;
    private EditText budgetInput;
    private TextView locationLabel; // This will be our status display

    // --- Services ---
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;

    // --- State ---
    private Location selectedLocation;

    // ActivityResultLauncher for handling the location permission request.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Location permission granted!", Toast.LENGTH_SHORT).show();
                    // Try getting location again after permission is granted
                    useCurrentLocation();
                } else {
                    Toast.makeText(this, "Permission denied. Location feature is unavailable.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_search);

        // --- Initialize UI and Services ---
        locationInput = findViewById(R.id.location_input);
        keywordInput = findViewById(R.id.keywordInput);
        budgetInput = findViewById(R.id.budgetInput);
        locationLabel = findViewById(R.id.location_label); // Find the label
        Button manualSearchButton = findViewById(R.id.manual_search_button);
        Button currentLocationButton = findViewById(R.id.current_location_button);
        Button findCarsButton = findViewById(R.id.searchButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        // --- Set Click Listeners ---
        manualSearchButton.setOnClickListener(v -> handleManualSearch());
        currentLocationButton.setOnClickListener(v -> checkLocationPermissionAndGetLocation());
        findCarsButton.setOnClickListener(v -> startCarListActivity());
    }

    /**
     * Updates the location_label TextView to show the currently set location.
     * @param locationName The name of the location to display. If null, resets the label.
     */
    private void updateLocationLabel(String locationName) {
        if (locationName != null && !locationName.isEmpty()) {
            locationLabel.setText("Current Location: " + locationName);
        } else {
            // Reset to default text if location is cleared or invalid
            locationLabel.setText("Current Location: unknown");
        }
    }

    /**
     * Handles the click of the "Set Location" button.
     */
    private void handleManualSearch() {
        String addressString = locationInput.getText().toString();
        if (addressString.isEmpty()) {
            Toast.makeText(this, "Please enter a location to search.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedLocation = new Location("geocoder");
                selectedLocation.setLatitude(address.getLatitude());
                selectedLocation.setLongitude(address.getLongitude());

                // Use a simple format like "City, State" for the label
                String city = address.getLocality();
                String state = address.getAdminArea();
                String displayLocation = (city != null && state != null) ? city + ", " + state : address.getAddressLine(0);

                updateLocationLabel(displayLocation); // Update the label with the found location
                locationInput.setText(""); // Clear the input box after successful search
                locationInput.setHint("Location set to: " + displayLocation); // Set a helpful hint
                Log.i(TAG, "Location found via geocoder: " + displayLocation);

                Toast.makeText(this, "Location set to: " + displayLocation, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Could not find location. Please try a different search.", Toast.LENGTH_LONG).show();
                selectedLocation = null; // Clear location if search fails
                updateLocationLabel(null); // Reset the label
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder service not available or failed.", e);
            Toast.makeText(this, "Network error. Unable to verify location.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks for permission before getting the device's location.
     */
    private void checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            useCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }

    /**
     * Gets the last known location and updates the UI.
     */
    private void useCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            selectedLocation = location;
                            getAddressFromLocation(location); // This will update the label
                        } else {
                            Log.w(TAG, "FusedLocationProvider returned a null location.");
                            Toast.makeText(this, "Could not retrieve current location. Please ensure location is enabled.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(this, e -> Log.e(TAG, "Error getting location", e));
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission check failed unexpectedly.", e);
        }
    }

    /**
     * Uses Geocoder to get an address from a Location object and update the label.
     */
    private void getAddressFromLocation(Location location) {
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                String state = address.getAdminArea();
                String displayLocation = (city != null && state != null) ? city + ", " + state : address.getAddressLine(0);

                updateLocationLabel(displayLocation); // Update the label with the current location
                locationInput.setText(""); // Also clear the input box
                locationInput.setHint("Using your current location");

                Toast.makeText(this, "Location set to: " + displayLocation, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder service not available.", e);
            Toast.makeText(this, "Could not determine address from location.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Starts the CarList activity.
     */
    private void startCarListActivity() {
        if (selectedLocation == null) {
            Toast.makeText(this, "Please set a location first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String budgetString = budgetInput.getText().toString();
        if (budgetString.isEmpty()) {
            Toast.makeText(this, "Please enter a budget.", Toast.LENGTH_SHORT).show();
            return;
        }

        String keywords = keywordInput.getText().toString();
        Intent intent = new Intent(this, CarList.class);
        intent.putExtra("EXTRA_LATITUDE", selectedLocation.getLatitude());
        intent.putExtra("EXTRA_LONGITUDE", selectedLocation.getLongitude());
        intent.putExtra("EXTRA_KEYWORDS", keywords);
        intent.putExtra("EXTRA_BUDGET", budgetString);
        startActivity(intent);
    }
}
