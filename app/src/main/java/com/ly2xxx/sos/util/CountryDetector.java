package com.ly2xxx.sos.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class CountryDetector {
    
    private static final String TAG = "CountryDetector";
    
    // Country boundaries (simplified for major countries)
    // Format: [minLat, maxLat, minLng, maxLng]
    private static final Map<String, double[]> COUNTRY_BOUNDARIES = new HashMap<>();
    
    static {
        // North America
        COUNTRY_BOUNDARIES.put("United_States", new double[]{24.396308, 49.384358, -125.0, -66.93457});
        COUNTRY_BOUNDARIES.put("Canada", new double[]{41.6751, 83.23324, -141.0, -52.6481});
        COUNTRY_BOUNDARIES.put("Mexico", new double[]{14.5388, 32.72083, -118.453, -86.7104});
        
        // Europe
        COUNTRY_BOUNDARIES.put("United_Kingdom", new double[]{49.959999905, 58.6350001085, -7.57216793459, 1.68153079591});
        COUNTRY_BOUNDARIES.put("France", new double[]{41.333, 51.089, -5.225, 9.662});
        COUNTRY_BOUNDARIES.put("Germany", new double[]{47.270, 55.099, 5.866, 15.042});
        COUNTRY_BOUNDARIES.put("Italy", new double[]{35.493, 47.092, 6.627, 18.521});
        COUNTRY_BOUNDARIES.put("Spain", new double[]{27.638, 43.792, -18.161, 4.327});
        COUNTRY_BOUNDARIES.put("Poland", new double[]{49.002, 54.836, 14.123, 24.150});
        COUNTRY_BOUNDARIES.put("Netherlands", new double[]{50.750, 53.555, 3.314, 7.092});
        COUNTRY_BOUNDARIES.put("Belgium", new double[]{49.497, 51.505, 2.546, 6.408});
        COUNTRY_BOUNDARIES.put("Sweden", new double[]{55.337, 69.061, 11.119, 24.167});
        COUNTRY_BOUNDARIES.put("Norway", new double[]{57.977, 71.185, 4.650, 31.294});
        COUNTRY_BOUNDARIES.put("Denmark", new double[]{54.559, 57.749, 8.075, 15.158});
        COUNTRY_BOUNDARIES.put("Finland", new double[]{59.808, 70.092, 20.456, 31.587});
        COUNTRY_BOUNDARIES.put("Switzerland", new double[]{45.818, 47.808, 5.956, 10.492});
        COUNTRY_BOUNDARIES.put("Austria", new double[]{46.372, 49.021, 9.531, 17.160});
        COUNTRY_BOUNDARIES.put("Portugal", new double[]{36.961, 42.154, -9.500, -6.190});
        COUNTRY_BOUNDARIES.put("Czech_Republic", new double[]{48.552, 51.055, 12.096, 18.877});
        COUNTRY_BOUNDARIES.put("Hungary", new double[]{45.737, 48.585, 16.114, 22.710});
        COUNTRY_BOUNDARIES.put("Romania", new double[]{43.619, 48.265, 20.220, 29.663});
        COUNTRY_BOUNDARIES.put("Greece", new double[]{34.802, 41.749, 19.374, 28.247});
        
        // Asia
        COUNTRY_BOUNDARIES.put("China", new double[]{18.197, 53.561, 73.557, 134.773});
        COUNTRY_BOUNDARIES.put("Japan", new double[]{24.045, 45.523, 122.934, 145.817});
        COUNTRY_BOUNDARIES.put("India", new double[]{6.754, 35.513, 68.033, 97.395});
        COUNTRY_BOUNDARIES.put("Russia", new double[]{41.185, 81.857, 19.638, -169.05});
        COUNTRY_BOUNDARIES.put("South_Korea", new double[]{33.190, 38.612, 125.887, 129.584});
        COUNTRY_BOUNDARIES.put("Indonesia", new double[]{-10.360, 5.904, 95.009, 141.021});
        COUNTRY_BOUNDARIES.put("Thailand", new double[]{5.613, 20.464, 97.343, 105.639});
        COUNTRY_BOUNDARIES.put("Vietnam", new double[]{8.560, 23.393, 102.145, 109.464});
        COUNTRY_BOUNDARIES.put("Malaysia", new double[]{0.855, 7.363, 99.644, 119.267});
        COUNTRY_BOUNDARIES.put("Singapore", new double[]{1.158, 1.470, 103.594, 104.007});
        COUNTRY_BOUNDARIES.put("Philippines", new double[]{4.613, 21.121, 116.931, 126.537});
        COUNTRY_BOUNDARIES.put("Turkey", new double[]{35.808, 42.108, 25.668, 44.835});
        COUNTRY_BOUNDARIES.put("Iran", new double[]{25.064, 39.782, 44.033, 63.333});
        COUNTRY_BOUNDARIES.put("Iraq", new double[]{29.061, 37.379, 38.795, 48.567});
        COUNTRY_BOUNDARIES.put("Pakistan", new double[]{23.693, 37.097, 60.878, 77.840});
        COUNTRY_BOUNDARIES.put("Bangladesh", new double[]{20.670, 26.632, 88.028, 92.673});
        COUNTRY_BOUNDARIES.put("Sri_Lanka", new double[]{5.917, 9.832, 79.652, 81.879});
        
        // Oceania  
        COUNTRY_BOUNDARIES.put("Australia", new double[]{-43.634, -10.668, 113.338, 153.569});
        COUNTRY_BOUNDARIES.put("New_Zealand", new double[]{-46.641, -34.131, 166.509, 178.517});
        
        // Africa
        COUNTRY_BOUNDARIES.put("South_Africa", new double[]{-34.834, -22.126, 16.344, 32.830});
        COUNTRY_BOUNDARIES.put("Egypt", new double[]{22.000, 31.667, 24.698, 36.898});
        COUNTRY_BOUNDARIES.put("Nigeria", new double[]{4.240, 13.892, 2.668, 14.678});
        COUNTRY_BOUNDARIES.put("Kenya", new double[]{-4.678, 5.506, 33.893, 41.899});
        COUNTRY_BOUNDARIES.put("Ghana", new double[]{4.736, 11.174, -3.260, 1.191});
        COUNTRY_BOUNDARIES.put("Morocco", new double[]{27.662, 35.922, -17.020, -0.997});
        COUNTRY_BOUNDARIES.put("Algeria", new double[]{18.968, 37.093, -8.673, 11.979});
        
        // South America
        COUNTRY_BOUNDARIES.put("Brazil", new double[]{-33.751, 5.272, -73.985, -28.848});
        COUNTRY_BOUNDARIES.put("Argentina", new double[]{-55.061, -21.781, -73.560, -53.591});
        COUNTRY_BOUNDARIES.put("Chile", new double[]{-55.916, -17.507, -109.455, -66.417});
        COUNTRY_BOUNDARIES.put("Colombia", new double[]{-4.227, 12.462, -81.728, -66.869});
        COUNTRY_BOUNDARIES.put("Venezuela", new double[]{0.724, 12.201, -73.354, -59.758});
        COUNTRY_BOUNDARIES.put("Peru", new double[]{-18.350, -0.038, -81.328, -68.677});
        COUNTRY_BOUNDARIES.put("Ecuador", new double[]{-4.998, 1.680, -91.661, -75.192});
        COUNTRY_BOUNDARIES.put("Bolivia", new double[]{-22.896, -9.680, -69.641, -57.453});
        COUNTRY_BOUNDARIES.put("Uruguay", new double[]{-34.980, -30.109, -58.443, -53.209});
        
        // Middle East
        COUNTRY_BOUNDARIES.put("Saudi_Arabia", new double[]{16.002, 32.158, 34.496, 55.666});
        COUNTRY_BOUNDARIES.put("United_Arab_Emirates", new double[]{22.633, 26.084, 51.583, 56.397});
        COUNTRY_BOUNDARIES.put("Israel", new double[]{29.497, 33.341, 34.266, 35.836});
        COUNTRY_BOUNDARIES.put("Jordan", new double[]{29.185, 33.367, 34.959, 39.301});
        COUNTRY_BOUNDARIES.put("Lebanon", new double[]{33.054, 34.691, 35.114, 36.625});
        COUNTRY_BOUNDARIES.put("Kuwait", new double[]{28.524, 30.095, 46.555, 48.431});
        COUNTRY_BOUNDARIES.put("Qatar", new double[]{24.482, 26.154, 50.757, 51.636});
        COUNTRY_BOUNDARIES.put("Bahrain", new double[]{25.796, 26.282, 50.450, 50.664});
    }
    
    public String getCountryFromCoordinates(double latitude, double longitude) {
        Log.d(TAG, "Detecting country for coordinates: " + latitude + ", " + longitude);
        
        // Check against all country boundaries
        for (Map.Entry<String, double[]> entry : COUNTRY_BOUNDARIES.entrySet()) {
            String country = entry.getKey();
            double[] bounds = entry.getValue();
            
            double minLat = bounds[0];
            double maxLat = bounds[1];
            double minLng = bounds[2];
            double maxLng = bounds[3];
            
            // Handle longitude wrap-around (e.g., for Russia)
            boolean longitudeMatch;
            if (minLng > maxLng) {
                // Crosses 180° meridian
                longitudeMatch = (longitude >= minLng || longitude <= maxLng);
            } else {
                longitudeMatch = (longitude >= minLng && longitude <= maxLng);
            }
            
            if (latitude >= minLat && latitude <= maxLat && longitudeMatch) {
                Log.d(TAG, "Country detected: " + country);
                return country;
            }
        }
        
        // If no exact match found, try regional fallbacks
        String region = getRegionFallback(latitude, longitude);
        Log.d(TAG, "No exact match found, using regional fallback: " + region);
        return region;
    }
    
    private String getRegionFallback(double latitude, double longitude) {
        // European region fallback
        if (latitude >= 35.0 && latitude <= 71.0 && longitude >= -10.0 && longitude <= 40.0) {
            return "Germany"; // Most central European country
        }
        
        // North American region fallback
        if (latitude >= 14.0 && latitude <= 83.0 && longitude >= -168.0 && longitude <= -52.0) {
            return "United_States";
        }
        
        // Asian region fallback
        if (latitude >= -10.0 && latitude <= 81.0 && longitude >= 60.0 && longitude <= 180.0) {
            return "China";
        }
        
        // African region fallback
        if (latitude >= -35.0 && latitude <= 37.0 && longitude >= -18.0 && longitude <= 51.0) {
            return "South_Africa";
        }
        
        // South American region fallback
        if (latitude >= -56.0 && latitude <= 13.0 && longitude >= -82.0 && longitude <= -28.0) {
            return "Brazil";
        }
        
        // Oceania region fallback
        if (latitude >= -47.0 && latitude <= -10.0 && longitude >= 113.0 && longitude <= 179.0) {
            return "Australia";
        }
        
        // Default fallback - use universal emergency number
        return "Unknown";
    }
    
    public boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90.0 && latitude <= 90.0 && 
               longitude >= -180.0 && longitude <= 180.0;
    }
}