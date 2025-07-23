package com.ly2xxx.sos;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ly2xxx.sos.model.EmergencyContact;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class EmergencyContactsActivity extends AppCompatActivity {
    
    private static final String TAG = "EmergencyContactsActivity";
    private static final int MAX_SEARCH_LENGTH = 100; // Prevent excessive search queries
    private static final Pattern SAFE_SEARCH_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_]+$");
    
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private EmergencyContactAdapter adapter;
    private List<CountryEmergencyContact> allContacts;
    private List<CountryEmergencyContact> filteredContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);
        
        setupToolbar();
        initViews();
        loadEmergencyContacts();
        setupRecyclerView();
        setupSearch();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Emergency Contacts");
            }
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        searchEditText = findViewById(R.id.search_edit_text);
        
        if (recyclerView == null || searchEditText == null) {
            Log.e(TAG, "Critical views not found in layout");
            finish();
            return;
        }
        
        allContacts = new ArrayList<>();
        filteredContacts = new ArrayList<>();
    }

    private void loadEmergencyContacts() {
        try {
            AssetManager assetManager = getAssets();
            
            // Use try-with-resources for proper resource management
            try (InputStream inputStream = assetManager.open("emergency_contacts.json")) {
                
                byte[] buffer = new byte[inputStream.available()];
                int bytesRead = inputStream.read(buffer);
                
                if (bytesRead == -1) {
                    throw new IOException("Failed to read emergency contacts file");
                }
                
                String jsonString = new String(buffer, "UTF-8");
                
                // Validate JSON is not empty or null
                if (jsonString.trim().isEmpty()) {
                    throw new JSONException("Emergency contacts file is empty");
                }
                
                JSONObject jsonObject = new JSONObject(jsonString);
                
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String countryKey = keys.next();
                    
                    // Validate country key
                    if (countryKey == null || countryKey.trim().isEmpty()) {
                        Log.w(TAG, "Skipping empty country key");
                        continue;
                    }
                    
                    JSONObject contactData = jsonObject.getJSONObject(countryKey);
                    
                    // Sanitize and validate country name
                    String countryName = sanitizeCountryName(countryKey);
                    if (countryName == null) {
                        Log.w(TAG, "Skipping invalid country: " + countryKey);
                        continue;
                    }
                    
                    // Validate and sanitize emergency numbers
                    String police = sanitizePhoneNumber(contactData.optString("police", ""));
                    String ambulance = sanitizePhoneNumber(contactData.optString("ambulance", ""));
                    String fire = sanitizePhoneNumber(contactData.optString("fire", ""));
                    String general = sanitizePhoneNumber(contactData.optString("general", ""));
                    
                    // Only add if at least one emergency number is valid
                    if (police != null || ambulance != null || fire != null || general != null) {
                        EmergencyContact contact = new EmergencyContact(
                            police != null ? police : "N/A", 
                            ambulance != null ? ambulance : "N/A", 
                            fire != null ? fire : "N/A", 
                            general != null ? general : "N/A"
                        );
                        CountryEmergencyContact countryContact = new CountryEmergencyContact(countryName, contact);
                        allContacts.add(countryContact);
                    } else {
                        Log.w(TAG, "No valid emergency numbers for country: " + countryName);
                    }
                }
                
                // Sort countries alphabetically
                allContacts.sort((c1, c2) -> c1.getCountryName().compareToIgnoreCase(c2.getCountryName()));
                filteredContacts.addAll(allContacts);
                
                Log.i(TAG, "Loaded " + allContacts.size() + " countries with emergency contacts");
                
            }
            
        } catch (IOException e) {
            Log.e(TAG, "IO Error loading emergency contacts", e);
            Toast.makeText(this, "Error reading emergency contacts file", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error parsing emergency contacts", e);
            Toast.makeText(this, "Error parsing emergency contacts data", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error loading emergency contacts", e);
            Toast.makeText(this, "Unexpected error loading emergency contacts", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Sanitize country name to prevent injection attacks
     */
    private String sanitizeCountryName(String countryKey) {
        if (countryKey == null || countryKey.trim().isEmpty()) {
            return null;
        }
        
        // Replace underscores with spaces and validate characters
        String countryName = countryKey.replace("_", " ").trim();
        
        // Check for valid characters only (letters, spaces, hyphens)
        if (!Pattern.matches("^[a-zA-Z\\s\\-]+$", countryName)) {
            return null;
        }
        
        // Limit length to prevent excessive memory usage
        if (countryName.length() > 50) {
            return countryName.substring(0, 50);
        }
        
        return countryName;
    }

    /**
     * Sanitize and validate phone numbers
     */
    private String sanitizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }
        
        String cleaned = phoneNumber.trim();
        
        // Allow only digits, spaces, +, -, (, )
        if (!Pattern.matches("^[\\d\\s\\+\\-\\(\\)]+$", cleaned)) {
            return null;
        }
        
        // Remove all non-digit characters except +
        String digitsOnly = cleaned.replaceAll("[^\\d\\+]", "");
        
        // Validate phone number format (3-15 digits, optionally starting with +)
        if (!Pattern.matches("^\\+?\\d{3,15}$", digitsOnly)) {
            return null;
        }
        
        return cleaned; // Return original format for display
    }

    private void setupRecyclerView() {
        if (recyclerView != null && filteredContacts != null) {
            adapter = new EmergencyContactAdapter(filteredContacts);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupSearch() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s != null ? s.toString() : "";
                    filterContacts(query);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterContacts(String query) {
        if (filteredContacts == null || allContacts == null) {
            return;
        }
        
        filteredContacts.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredContacts.addAll(allContacts);
        } else {
            // Sanitize search query
            String sanitizedQuery = sanitizeSearchQuery(query);
            if (sanitizedQuery != null) {
                String lowerCaseQuery = sanitizedQuery.toLowerCase().trim();
                for (CountryEmergencyContact contact : allContacts) {
                    if (contact != null && contact.getCountryName() != null &&
                        contact.getCountryName().toLowerCase().contains(lowerCaseQuery)) {
                        filteredContacts.add(contact);
                    }
                }
            }
        }
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Sanitize search query to prevent injection attacks
     */
    private String sanitizeSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = query.trim();
        
        // Limit search query length
        if (trimmed.length() > MAX_SEARCH_LENGTH) {
            trimmed = trimmed.substring(0, MAX_SEARCH_LENGTH);
        }
        
        // Allow only safe characters for search
        if (!SAFE_SEARCH_PATTERN.matcher(trimmed).matches()) {
            Log.w(TAG, "Invalid characters in search query, filtering them out");
            trimmed = trimmed.replaceAll("[^a-zA-Z0-9\\s\\-_]", "");
        }
        
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Inner class to hold country name and emergency contact
    public static class CountryEmergencyContact {
        private final String countryName;
        private final EmergencyContact emergencyContact;

        public CountryEmergencyContact(String countryName, EmergencyContact emergencyContact) {
            this.countryName = countryName;
            this.emergencyContact = emergencyContact;
        }

        public String getCountryName() {
            return countryName;
        }

        public EmergencyContact getEmergencyContact() {
            return emergencyContact;
        }
    }
}
