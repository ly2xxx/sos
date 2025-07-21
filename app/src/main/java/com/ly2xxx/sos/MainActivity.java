package com.ly2xxx.sos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ly2xxx.sos.model.EmergencyContact;
import com.ly2xxx.sos.service.LocationService;
import com.ly2xxx.sos.util.CountryDetector;
import com.ly2xxx.sos.util.EmergencyContactsManager;

public class MainActivity extends AppCompatActivity implements LocationService.LocationListener {
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CALL_PHONE
    };

    private TextView tvLocation;
    private TextView tvCountry;
    private Button btnPolice;
    private Button btnAmbulance;
    private Button btnFire;
    private Button btnGeneral;
    private TextView tvStatus;

    private LocationService locationService;
    private EmergencyContactsManager contactsManager;
    private CountryDetector countryDetector;
    
    private String currentCountry = "Unknown";
    private EmergencyContact currentEmergencyContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initServices();
        
        if (checkPermissions()) {
            startLocationService();
        } else {
            requestPermissions();
        }
    }

    private void initViews() {
        tvLocation = findViewById(R.id.tv_location);
        tvCountry = findViewById(R.id.tv_country);
        btnPolice = findViewById(R.id.btn_police);
        btnAmbulance = findViewById(R.id.btn_ambulance);
        btnFire = findViewById(R.id.btn_fire);
        btnGeneral = findViewById(R.id.btn_general);
        tvStatus = findViewById(R.id.tv_status);

        btnPolice.setOnClickListener(v -> makeEmergencyCall("police"));
        btnAmbulance.setOnClickListener(v -> makeEmergencyCall("ambulance"));
        btnFire.setOnClickListener(v -> makeEmergencyCall("fire"));
        btnGeneral.setOnClickListener(v -> makeEmergencyCall("general"));
    }

    private void initServices() {
        locationService = new LocationService(this);
        contactsManager = new EmergencyContactsManager(this);
        countryDetector = new CountryDetector();
        locationService.setLocationListener(this);
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                startLocationService();
            } else {
                Toast.makeText(this, "All permissions are required for emergency functionality", 
                        Toast.LENGTH_LONG).show();
                tvStatus.setText("⚠️ Permissions required");
            }
        }
    }

    private void startLocationService() {
        tvStatus.setText("📍 Getting location...");
        locationService.startLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocationDisplay(location);
        
        // Detect country from coordinates
        new Thread(() -> {
            String country = countryDetector.getCountryFromCoordinates(
                    location.getLatitude(), location.getLongitude());
            
            runOnUiThread(() -> {
                currentCountry = country;
                updateCountryDisplay(country);
                loadEmergencyContacts(country);
            });
        }).start();
    }

    @Override
    public void onLocationError(String error) {
        tvStatus.setText("❌ " + error);
        Toast.makeText(this, "Location error: " + error, Toast.LENGTH_LONG).show();
    }

    private void updateLocationDisplay(Location location) {
        String locationText = String.format("📍 %.4f, %.4f", 
                location.getLatitude(), location.getLongitude());
        tvLocation.setText(locationText);
    }

    private void updateCountryDisplay(String country) {
        String countryText = "🌍 Country: " + country;
        tvCountry.setText(countryText);
        tvStatus.setText("✅ Ready for emergency calls");
    }

    private void loadEmergencyContacts(String country) {
        currentEmergencyContact = contactsManager.getEmergencyContact(country);
        
        if (currentEmergencyContact != null) {
            updateEmergencyButtons(currentEmergencyContact);
        } else {
            // Fallback to general emergency numbers
            showFallbackNumbers();
        }
    }

    private void updateEmergencyButtons(EmergencyContact contact) {
        btnPolice.setText(String.format("🚔 Police\n%s", contact.getPolice()));
        btnAmbulance.setText(String.format("🚑 Ambulance\n%s", contact.getAmbulance()));
        btnFire.setText(String.format("🚒 Fire\n%s", contact.getFire()));
        btnGeneral.setText(String.format("🆘 Emergency\n%s", contact.getGeneral()));
        
        btnPolice.setEnabled(true);
        btnAmbulance.setEnabled(true);
        btnFire.setEnabled(true);
        btnGeneral.setEnabled(true);
    }

    private void showFallbackNumbers() {
        btnPolice.setText("🚔 Police\n112");
        btnAmbulance.setText("🚑 Ambulance\n112");
        btnFire.setText("🚒 Fire\n112");
        btnGeneral.setText("🆘 Emergency\n112");
        
        // Create fallback contact
        currentEmergencyContact = new EmergencyContact("112", "112", "112", "112");
    }

    private void makeEmergencyCall(String type) {
        if (currentEmergencyContact == null) {
            Toast.makeText(this, "Emergency contacts not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String number = "";
        switch (type) {
            case "police":
                number = currentEmergencyContact.getPolice();
                break;
            case "ambulance":
                number = currentEmergencyContact.getAmbulance();
                break;
            case "fire":
                number = currentEmergencyContact.getFire();
                break;
            case "general":
                number = currentEmergencyContact.getGeneral();
                break;
        }

        if (number != null && !number.isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + number));
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
                    == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent);
                
                // Log emergency call
                tvStatus.setText(String.format("📞 Calling %s emergency: %s", type, number));
            } else {
                Toast.makeText(this, "Call permission required", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Emergency number not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationService != null) {
            locationService.stopLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermissions() && locationService != null) {
            locationService.startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationService != null) {
            locationService.stopLocationUpdates();
        }
    }
}