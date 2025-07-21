package com.ly2xxx.sos.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class LocationService extends Service implements LocationListener {
    
    private static final String TAG = "LocationService";
    private static final long MIN_TIME_BETWEEN_UPDATES = 10000; // 10 seconds
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; // 50 meters
    
    private Context context;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    
    private Location location;
    private double latitude;
    private double longitude;
    
    private LocationListener externalLocationListener;
    
    public interface LocationListener {
        void onLocationChanged(Location location);
        void onLocationError(String error);
    }
    
    public LocationService(Context context) {
        this.context = context;
        getLocation();
    }
    
    public LocationService() {
        // Default constructor for service
    }
    
    public void setLocationListener(LocationListener listener) {
        this.externalLocationListener = listener;
    }
    
    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            
            // Check if GPS is enabled
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            
            // Check if Network is enabled
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            
            if (!isGPSEnabled && !isNetworkEnabled) {
                if (externalLocationListener != null) {
                    externalLocationListener.onLocationError("No location providers available");
                }
                return null;
            }
            
            this.canGetLocation = true;
            
            // Try Network provider first (faster)
            if (isNetworkEnabled) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BETWEEN_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );
                    
                    Log.d(TAG, "Network location provider enabled");
                    
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
            
            // Try GPS provider for more accuracy
            if (isGPSEnabled) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BETWEEN_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                this
                        );
                        
                        Log.d(TAG, "GPS location provider enabled");
                        
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting location", e);
            if (externalLocationListener != null) {
                externalLocationListener.onLocationError("Error getting location: " + e.getMessage());
            }
        }
        
        return location;
    }
    
    public void startLocationUpdates() {
        getLocation();
    }
    
    public void stopLocationUpdates() {
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(this);
            }
        }
    }
    
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }
    
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }
    
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
    
    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        
        Log.d(TAG, "Location changed: " + latitude + ", " + longitude);
        
        if (externalLocationListener != null) {
            externalLocationListener.onLocationChanged(location);
        }
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider enabled: " + provider);
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Provider disabled: " + provider);
        if (externalLocationListener != null) {
            externalLocationListener.onLocationError("Location provider disabled: " + provider);
        }
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Provider status changed: " + provider + " status: " + status);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public static class LocationException extends Exception {
        public LocationException(String message) {
            super(message);
        }
    }
}