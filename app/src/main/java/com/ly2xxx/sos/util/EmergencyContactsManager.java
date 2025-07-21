package com.ly2xxx.sos.util;

import android.content.Context;
import android.util.Log;

import com.ly2xxx.sos.model.EmergencyContact;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EmergencyContactsManager {
    
    private static final String TAG = "EmergencyContactsManager";
    private static final String ASSETS_FILE = "emergency_contacts.json";
    
    private Context context;
    private Map<String, EmergencyContact> emergencyContacts;
    private boolean isLoaded = false;
    
    public EmergencyContactsManager(Context context) {
        this.context = context;
        this.emergencyContacts = new HashMap<>();
        loadEmergencyContacts();
    }
    
    private void loadEmergencyContacts() {
        try {
            String jsonString = loadJSONFromAsset();
            if (jsonString != null) {
                parseEmergencyContacts(jsonString);
                isLoaded = true;
                Log.d(TAG, "Emergency contacts loaded successfully. Total countries: " + emergencyContacts.size());
            } else {
                Log.e(TAG, "Failed to load emergency contacts JSON");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading emergency contacts", e);
        }
    }
    
    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = context.getAssets().open(ASSETS_FILE);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, "Error reading JSON file from assets", e);
            return null;
        }
        return json;
    }
    
    private void parseEmergencyContacts(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> keys = jsonObject.keys();
            
            while (keys.hasNext()) {
                String country = keys.next();
                JSONObject countryData = jsonObject.getJSONObject(country);
                
                String police = countryData.optString("police", "112");
                String ambulance = countryData.optString("ambulance", "112");
                String fire = countryData.optString("fire", "112");
                String general = countryData.optString("general", "112");
                
                EmergencyContact contact = new EmergencyContact(police, ambulance, fire, general);
                emergencyContacts.put(country, contact);
                
                Log.d(TAG, "Loaded contacts for " + country + ": " + contact.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing emergency contacts JSON", e);
        }
    }
    
    public EmergencyContact getEmergencyContact(String country) {
        if (!isLoaded) {
            Log.w(TAG, "Emergency contacts not loaded yet");
            return getDefaultEmergencyContact();
        }
        
        if (country == null || country.trim().isEmpty()) {
            Log.w(TAG, "Country is null or empty, returning default");
            return getDefaultEmergencyContact();
        }
        
        // Try exact match first
        EmergencyContact contact = emergencyContacts.get(country);
        if (contact != null) {
            Log.d(TAG, "Found emergency contact for: " + country);
            return contact;
        }
        
        // Try with underscores replaced with spaces
        String countryWithSpaces = country.replace("_", " ");
        contact = emergencyContacts.get(countryWithSpaces);
        if (contact != null) {
            Log.d(TAG, "Found emergency contact for: " + countryWithSpaces);
            return contact;
        }
        
        // Try with spaces replaced with underscores
        String countryWithUnderscores = country.replace(" ", "_");
        contact = emergencyContacts.get(countryWithUnderscores);
        if (contact != null) {
            Log.d(TAG, "Found emergency contact for: " + countryWithUnderscores);
            return contact;
        }
        
        // Try case-insensitive search
        for (Map.Entry<String, EmergencyContact> entry : emergencyContacts.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(country) || 
                entry.getKey().equalsIgnoreCase(countryWithSpaces) ||
                entry.getKey().equalsIgnoreCase(countryWithUnderscores)) {
                Log.d(TAG, "Found emergency contact with case-insensitive match: " + entry.getKey());
                return entry.getValue();
            }
        }
        
        Log.w(TAG, "No emergency contact found for country: " + country + ", returning default");
        return getDefaultEmergencyContact();
    }
    
    private EmergencyContact getDefaultEmergencyContact() {
        // Universal emergency number used by many countries
        return new EmergencyContact("112", "112", "112", "112");
    }
    
    public boolean isLoaded() {
        return isLoaded;
    }
    
    public int getLoadedCountriesCount() {
        return emergencyContacts.size();
    }
    
    public boolean hasCountry(String country) {
        if (!isLoaded || country == null) {
            return false;
        }
        
        return emergencyContacts.containsKey(country) ||
               emergencyContacts.containsKey(country.replace("_", " ")) ||
               emergencyContacts.containsKey(country.replace(" ", "_"));
    }
    
    // For debugging purposes
    public void logAllCountries() {
        if (!isLoaded) {
            Log.d(TAG, "Emergency contacts not loaded");
            return;
        }
        
        Log.d(TAG, "All loaded countries:");
        for (String country : emergencyContacts.keySet()) {
            EmergencyContact contact = emergencyContacts.get(country);
            Log.d(TAG, country + ": " + contact.toString());
        }
    }
    
    // Get regional emergency numbers for specific regions
    public EmergencyContact getRegionalEmergencyContact(String region) {
        switch (region.toLowerCase()) {
            case "europe":
                return new EmergencyContact("112", "112", "112", "112");
            case "north_america":
                return new EmergencyContact("911", "911", "911", "911");
            case "asia":
                return new EmergencyContact("110", "119", "119", "110");
            case "oceania":
                return new EmergencyContact("000", "000", "000", "000");
            default:
                return getDefaultEmergencyContact();
        }
    }
}